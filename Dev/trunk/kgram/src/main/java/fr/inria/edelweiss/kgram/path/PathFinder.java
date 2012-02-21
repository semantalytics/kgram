package fr.inria.edelweiss.kgram.path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventManager;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.Visit.Table;
import fr.inria.edelweiss.kgram.tool.EdgeInv;
import fr.inria.edelweiss.kgram.tool.EntityImpl;


/**********************************************************
 * 
 * ?x rdf:resf * /rdf:first
 * 
 * write paths in a synchronized buffer
 * 
 * ?x ^(p/q) ?y ::= ?y p/q ?x
 * ->
 * ?x inv(q)/inv(p) ?y
 * 
 * 
 * ?x p/q uri
 * path is computed backward from uri to ?x with index=1 other=0 isReverse=true
 * sequence is eval as q/p, all other exp are the same
 * 
 * 
 * ?x ^(p/q) uri ::= uri p/q ?x
 * -> ?x inv(q)/inv(p) uri
 * 
 * PP Extensions
 * 
 * Path Variable:
 * ?x exp :: $path ?y
 * 
 * Path Length:
 * pathLength($path)
 * 
 * Path Enumeration:
 * ?x exp :: $path ?y
 * graph $path {?a ?p ?b}
 * 
 * Shortest path: 
 * ?x distinct short exp ?y ; ?x short exp ?y
 * -- TODO: complete short to get only shortest
 * 
 * Path weight:
 * ?x (rdf:first@2 / rdf:rest@1* / ^rdf:first@2) * ?y
 *   
 * Constaint:
 * ?x exp @{?this a foaf:Person} ?y
 * ?x exp @[a foaf:Person] ?y
 * 
 * Parallel Path:
 * ?x (foaf:knows || ^rdfs:seeAlso) + ?y
 * 
 * 
 * Check Loop:
 * pf.setCheckLoop(true) => exp+ exp{n,m} without loop
 * exec.setPathLoop(false)
 * pragma {kg:path kg:loop false}
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 * @thanx  Corentin Follenfant for the idea of property weight into the regex 
 * 
 *********************************************************/

public class PathFinder 
{	
	
	private static Logger logger = Logger.getLogger(PathFinder.class);
	
	public static long cedge = 0, cresult = 0, ctest = 0;

	// thread that enumerates the path
	private GraphPath path;
	// synchronized buffer between this and projection
	private Buffer mbuffer;
	private Environment memory;
	private EventManager manager;
	private ResultListener listener;
	private Producer producer;
	private Matcher matcher;
	private Evaluator evaluator;
	private Query query;
	private Mappings lMap ;
	private Filter filter;
	private Memory mem;
	private Edge edge;
	private Node gNode, targetNode, regexNode, varNode;
	private List<Node> from;
	private Node[] qNodes;
	// index of node in edge that is the start of the path
	private int index = 0;


	// the inverse of the index (i.e. the other arg)
	private int other;
	private boolean 
	isStop = false,
	hasEvent = false,
	hasListener = false,
	// true if breadth first (else depth first)
	isBreadth,
	isDistinct = false,
	defaultBreadth = !true,
	// true if accept subproperty in regexp
	isSubProperty,
	isReverse,
	isShort,
	isOne, 
	// if true: return list of path instead of thread buffer: 50% faster but enumerate all path
	isList = false,
	checkLoop = false,
	trace = true;
	
	private int  maxLength = Integer.MAX_VALUE, 
	min = 0, max = maxLength,
	userMin = -1,
	userMax = -1;
	private int count = 0;
	
	private Regex  regexp1, regexp;
	// depth or width first 
	private String mode = "";
	
	private final static String DISTINCT="distinct";
	private final static String DEPTH	="d";
	private final static String BREADTH	="b";

	private final static String PROPERTY="p";
	// heuristic to eliminate path of length > path already found to *same* target
	private final static String SHORT	="s";
	// very short: eliminate any path >= best current path ('vs' = previous 's') to *any* target
	private final static String ALL		="a";
	public final static String INVERSE	="i";
	

	
	class ITable extends Hashtable<Node, Integer> {
		
		void setDistance(Node c, int i){
			put(c, i); //adistance[i]);
		}
		
		int getDistance(Node c){
			Integer i =  get(c);
			if (i == null) return -1;
			else return i;
		}
	}
	
	public PathFinder(){}
	
	
	public PathFinder(Producer p, Matcher match, Evaluator eval, Query q) {
		query = q;
		producer = p;
		matcher = match;
		evaluator = eval;
		lMap = new Mappings();
	}
	
	public static PathFinder create(Producer p, Matcher match, Evaluator eval, Query q) {
		return new PathFinder(p, match, eval, q);
	}

	
	public void setDefaultBreadth(boolean b){
		defaultBreadth = b;
	}
	
	public void set(EventManager man){
		manager = man;
		hasEvent = true;
	}
	
	public void set(ResultListener rl){
		listener = rl;
		hasListener = rl!=null;
		
	}
	
	public void setCheckLoop(boolean b){
		checkLoop = b;
	}
	
	public void setList(boolean b){
		isList = b;
	}
	
	
	/**
	 * Start/init computation of a new list of path
	 * 
	 */
	public void start(Edge edge, Node node, Memory memo, Filter f){
		regexNode = node;
		List<String> lVar = null;
		if (f != null) lVar = f.getVariables();
		lMap.clear();
		this.edge = edge;
		int n = index(edge, memo, lVar);
		start(n);
		index = n;
		targetNode = memo.getNode(edge.getNode(other));
		varNode = edge.getEdgeVariable();
		//producer.initPath(edge, index);
		
		if (f != null){
			if (match(edge, lVar, index)){
				filter = f;
				init(memo);
			}
		}
		if (mem == null && node!=null){
			init(memo);
		}
	}
	
	void init(Memory memo){
		mem = new Memory(matcher, evaluator);
		mem.init(memo.getQuery());
		mem.init(memo);
		mem.setFake(true);
	}
	
	
	boolean match(Edge edge, List<String> lVar, int index){
		return lVar.size() == 1 && 
			edge.getNode(index).isVariable() &&
			edge.getNode(index).getLabel().equals(lVar.get(0));
	}
	
	/**
	 * Compute the index of node of edge which will be start of the path
	 * If one of the nodes is bound, this is it
	 * If one of the nodes is a constant, this is it
	 * If there is a filter on a node, this is it
	 * Otherwise it is node at index 0
	 */
	int index(Edge edge, Environment mem, List<String> lVar){
		int n = -1; // index;
		// which arg is bound if any ?
		for (int i=0; i<2; i++){
			if (mem.isBound(edge.getNode(i))){
				n = i;
				break;
			}
		}
		if (n == -1){
			for (int i=0; i<2; i++){
				if (edge.getNode(i).isConstant()){
					n = i;
					break;
				}
			}
		}
		if (n == -1 && lVar != null){
			for (int i=0; i<2; i++){
				if (match(edge, lVar, i)){
					n = i;
					break;
				}
			}
		}
		if (n == -1){
			n = 0;
		}
		return n;
	}
	
	
	void setPathLength(int n){
		maxLength = n;
	}

	/**
	 * Enumerate all path, return a list of path
	 * 50% faster that thread but enumerate *all* path
	 */
	public Iterable<Mapping> candidate2(Node gNode, List<Node> from, Environment mem) {
		this.gNode = gNode;
		this.from = from;
		this.memory = mem;
		lMap.clear();
		process(memory);
		return lMap;
	}
	
	/**
	 * Enumerate path in a parallel thread, return a synchronised buffer
	 * Useful if  backjump or have a limit in sparql query
	 */
	public Iterable<Mapping> candidate(Node gNode, List<Node> from, Environment mem) {
		isStop = false;
		if (isList){
			return candidate2(gNode, from, mem);
		}
		this.gNode = gNode;
		this.from = from;
		this.memory = mem;
		mstart(mem);
		// return path enumeration (read the synchronized buffer)
		return mbuffer;
	}
	
	
	void mstart(Environment mem) {
		//isStop = false;
		// buffer store path enumeration
		mbuffer = new Buffer();
		// path enumeration in a thread 
		path = new GraphPath(this, mem, mbuffer);
		// launch path computing (one by one) eg launch process() below
		path.start();
	}
	
	public void run(){
		process(memory);
	}
	
	public void stop(){
		isStop = true;
	}
	
	public void interrupt(){
		if (path != null){
			path.interrupt();
		}
	}
	
	/**
	 * init at creation time, no need to change.
	 * pmax comes from pathLength() <= pmax
	 */
	public void init(Regex exp, Object smode, int pmin, int pmax){
		cedge = 0;
		cresult = 0;
		ctest = 0;
		regexp1 = exp.transform();
		regexp = exp;

		mode = (String)smode;
		
		isBreadth = defaultBreadth;
		isSubProperty = true;
		isReverse = false;
		isShort = false;
		isOne = false;
		other = 1;

//		if (mode != null){			
//			if (mode.indexOf(DEPTH) >= 0)   isBreadth = false;
//			if (mode.indexOf(BREADTH) >= 0) isBreadth = true;
//			isSubProperty = mode.indexOf(PROPERTY) == -1;
//			isShort =       mode.indexOf(SHORT) != -1;
//			if (isShort && ! isBreadth){
//				isOne = mode.indexOf(ALL) == -1;
//			}
//		}
		
		if (exp.isShort()){
			isShort = true;
			if (exp.isDistinct()){
				isOne = true;
			}
		}
		else if (exp.isDistinct()){
			isDistinct = true;
			// to speed up, eliminate longer path to same target
			isShort = true;
		}

			
		userMin = pmin;
		userMax = pmax;
		
		// set min and max path length
		// filter pathLength($path) > val
		// the length of the minimal path that matches regex:
		int length = regexp.regLength();
		min = Math.max(min, length);
		max = Math.max(max, length);
		
		// user default values
		if (userMin != -1) min = Math.max(min, userMin);
		// either IntegerMax or pathLength($p) <= max
		if (userMax != -1) max = userMax;
			
	}
	
	public Edge getEdge(){
		return edge;
	}
	
	/**
	 * start at run time
	 * depends on index : which arg is bound or where we start
	 */
	void start(int n){
		index = n;
		if (index == 1){ 
			other = 0;
			isReverse = true;
		}
		else {
			other = 1;
			isReverse = false;
		}
	
	}
	
	
	// number of enumerated relations
	public int getCount(){
		return count;
	}
	
	Node get(Environment memory, int i){
		if (edge == null) return null;
		Node qc = edge.getNode(i);
		//if (qc == null) return null;
		Node node = memory.getNode(qc);
		return node;
	}
	
	
	void process(Environment memory){
		// Is the source of edge bound ?
		// In which case all path relations come from same source 
		Node csource = null;
		if (gNode!=null){
			csource = memory.getNode(gNode);
		}
		
		// the start concept for path
		Node cstart = get(memory, index);
		Path path = new Path(isReverse);
		path.setMax(max);
		path.setIsShort(isShort);
		
		if (isShort && cstart != null) {
			// if null, will be done later
			producer.initPath(edge, 0);
		}
		
		eval(regexp1, path, cstart, csource);

		// in order to stop enumeration, return null
		if (! isList){
			mbuffer.put(null, false);
		}
		
	}
	


	/**
	 * Path result as a Mapping
	 * start and last nodes
	 * the path variable whose index is used (e.g. pathLength) to retrieve the list of edges in the memory
	 * the list of edges
	 */
	private Mapping result(Path path, Node gNode, Node src, Node start, boolean isReverse){
		//if (edge.getIndex()==1)System.out.println(producer.getGraphNode(edge, edge) + " " + edge);
		Edge ee = edge;
		int length = 3;
		if (src!=null) length = 4;
		
		Node n1, n2;
		if (path.size() == 0){
			n1 = start;
			n2 = start;
		}
		else {
			n1 = path.firstNode();
			n2 = path.lastNode();
		}
		
		if (! check(n1, n2)){
			return null;
		}
		
		if (qNodes == null){
			// computed once and then shared by all Mapping
			qNodes = new Node[length];
			qNodes[0] = ee.getNode(0);
			qNodes[1] = ee.getNode(1);
			qNodes[2] = ee.getEdgeVariable();
			if (src!=null){
				qNodes[3] = gNode;
			}
		}
		
		Node[] tNodes = new Node[length];
		tNodes[0] = n1;
		tNodes[1] = n2;
		tNodes[2] = getPathNode();
		if (src!=null){
			tNodes[3] = src;
		}
		
		Path edges = path.copy();
		if (isReverse) edges.reverse();
		Path[] lp = new Path[length];
		lp[2] = edges;
		
		Mapping map =  Mapping.create(qNodes, tNodes);			
		map.setPath(lp);
		
		return map;
	}
	
	void setLength(Node n, Regex exp, int l){
		Regex e = getRegex(n);
		if (e == null || e == exp){
			n.setProperty(Node.LENGTH, l);
			setRegex(n, exp);
		}
	}
	
	Integer getLength(Node n, Regex exp){
		Regex e = getRegex(n);
		if (e == null || e == exp){
			return (Integer) n.getProperty(Node.LENGTH);
		}
		return null;
	}
	
	void setRegex(Node n, Regex e){
		n.setProperty(Node.REGEX, e);
	}
	
	Regex getRegex(Node n){
		return (Regex) n.getProperty(Node.REGEX);
	}
	
	/**
	 * Generate a unique Blank Node wrt query that represents the path
	 * Use a predefined filter pathNode()
	 */
	Node getPathNode(){
		Filter f  = query.getGlobalFilter(Query.PATHNODE);
		Node node = evaluator.eval(f, memory);
		return node;
	}

	
	/**
	 * Check if target node match its query node and its binding
	 */
	private boolean check(Node n0, Node n1){
		if (index == 0){
			if (! matcher.match(edge.getNode(1), n1, memory)){ // n1.match(edge.getNode(1))){
				return false;
			}
			if (targetNode != null && ! targetNode.same(n1)){
				return false;
			}
		}
		else {
			if (! matcher.match(edge.getNode(0), n0, memory)){ //n0.match(edge.getNode(0))){
				return false;
			}
			if (targetNode != null && ! targetNode.same(n0)){
				return false;
			}
		}
		return true;
	}
	
	

	/**
	 * List of starting nodes for path
	 * TODO: check start memberOf src
	 */
	private Iterable<Entity> getStart(Node cstart, Node csrc){
		if (cstart != null){
			// start is bound
			ArrayList<Entity> vec = new ArrayList<Entity>();
			if (csrc != null){
				// check graph src contains start
				//System.out.println("** PF should check: " + csrc + " contains " + cstart);
			}
			vec.add(EntityImpl.create(csrc, cstart));
			return vec;
		}		
		else return getNodes(csrc, edge, from, null);
	}
	
	
	public Iterable<Entity> getNodes(Node csrc, final Edge edge, List<Node> from, List<Regex> list){	
		
		Iterable<Entity> iter = producer.getNodes(gNode, from, edge, memory, list, index);
		
		if (filter == null) return iter;
		
		final Iterator<Entity> it = iter.iterator();

		return new Iterable<Entity>(){
				
			public Iterator<Entity> iterator(){

				return new Iterator<Entity>(){

					public boolean hasNext() {
						return it.hasNext();
					}
					
					public Entity next() {
						while (hasNext()){
							Entity entity = it.next();
							if (entity == null) return null;
							Node node =  entity.getNode();
							if (test(node)){
								return entity;
							}
						}
						return null;
					}

					@Override
					public void remove() {
						// TODO Auto-generated method stub
						
					}
				};
			}
		};
	}
	
	/**
	 * exp @{?this rdf:type c:Person}
	 */
	boolean test(Filter filter, Path path, Node qNode, Node node){
		mem.push(qNode, node);
		if (varNode != null){
			// TODO: fix it
			mem.push(varNode, varNode);
			mem.pushPath(varNode, path);
		}
		boolean test = evaluator.test(filter, mem);
		mem.pop(qNode);
		if (varNode != null){
			mem.pop(varNode);
			mem.popPath(varNode);
		}
		return test;
	}
	
	boolean test(Node node){
		Node qNode = edge.getNode(index);
		mem.push(qNode, node);
		boolean test = evaluator.test(filter, mem);
		mem.pop(qNode);
		return test;
	}
	
	
/*********************************************************************************
 * 
 * New version interprets regex directly with a stack 
 * 
 ********************************************************************************/
	
	
	/**
	 *  rewrite   ! (^ p)   as   ^ (! p)
	 *  rewrite   ^(p/q)    as   ^q/^p
	 */
	void eval(Regex exp, Path path, Node start, Node src){
		Record stack = new Record(Visit.create(isReverse));
		stack.push(exp);
		try {
			eval(stack, path, start, src);
		}
		catch (StackOverflowError e){
			logger.error("** Property Path Size: " + path.size());
			logger.error("** Property Path Error: \n" + e);
		}
	}
	
	
	/**
	 * top of stack is current exp to eval
	 * rest of stack is in sequence
	 * path may be walked left to right if start is bound or right to left if end is bound
	 * in the later case,  index = 1
	 */
	void eval(Record stack, Path path, Node start, Node src){
		//trace(start.toString());

		if (isStop){
			return;
		}
		
		if (stack.isEmpty()){

			if (stack.getTarget() != null){
				// this is a parallel path check, path is finished: stop it
				if (start.same(stack.getTarget())){
					// it is successful 
					stack.setSuccess(true);
				}
				return;
			}
			
			result(stack, path, start, src);			
			return;
		}
			
		Regex exp = stack.pop();
		
		//System.out.println(exp.toString() + " " + start);
							
		switch (exp.retype()){
			
		case Regex.TEST: {
			// exp @[ ?this != <John> ]
			
			boolean b = true;
			
			if (start != null){
				//ctest ++;
				b = test(exp.getExpr().getFilter(), path, regexNode, start);
			}
			
			if (b){
				eval(stack, path, start, src);
			}
			stack.push(exp);
		}
		break;
		
			
		case Regex.LABEL:
		case Regex.NOT: {
						
			if (path.size() >= path.getMax()){
				stack.push(exp);
				return;
			}
			
			boolean inverse = exp.isInverse() || exp.isReverse();
			Producer pp = producer;
			List<Node> ff = from;
			Edge ee = edge;
			Environment env = memory;
			int ii = index, oo = other;
			int pweight = path.weight(), eweight = exp.getWeight();
			int size = path.size();

			boolean 
				hasFilter = filter!=null,
				isStart   = start == null,
				hasSource = size == 0 && src == null && gNode != null,
				hasHandler = hasListener,
				hasShort  = isShort,
				hasOne 	  = isOne;

			Visit visit = stack.getVisit();
			Node gg = gNode, previous = null;
			ResultListener handler = listener;
			
			for (Entity ent : pp.getEdges(gg, ff, ee, env, exp, src, start, ii)){
				
				if (isStop){
					stack.push(exp);
					return;
				}
				
				if (stack.isSuccess()){
					// parallel path has succeeded: stop it
					stack.push(exp);
					return;
				}

				if (ent == null){
					continue;
				}
				
				//cedge++;
				//trace(ent);

				Edge rel  = ent.getEdge();
				Node node = rel.getNode(ii);

				if (inverse){
					EdgeInv ei = new EdgeInv(ent);	
					rel = ei;
					ent = ei;
					node = rel.getNode(ii);
				}
				
				if (hasFilter && isStart){
					// test a filter on the index node
					boolean test = test(rel.getNode(ii));
					if (! test) continue;
				}
				
				if (hasSource){
					// first time: bind the common source of current path
					src = ent.getGraph();
				}
				else if (src != null && ! ent.getGraph().same(src) ){ 
					// all relations need same source in one path
					continue;
				}				
									
				if (isStart){
					// bind start node
					visit.start(node);
					// in case there is e1 || e2
					stack.pushStart(node);
					if (hasShort) {
						// reset node length to zero when start changes
						if (previous==null || ! previous.same(node)){
							pp.initPath(ee, 0);
						}
						previous = node;
					}
				}
				
				
				if (hasShort){
					// shortest path
					Node other 	= rel.getNode(oo);
					Integer l 	= getLength(other, exp);
					int length 	= pweight + eweight;
					
					if (l == null){
						setLength(other, exp, length);
					}
					else if (length > l){
						continue;
					}
					else if (hasOne && length == l){
						continue;
					}
					else {
						setLength(other, exp, length);
					}
				}

				if (hasHandler){
					handler.enter(ent, exp, size);
				}
				
				path.add(ent, eweight);
				
				eval(stack, path, rel.getNode(oo), src);
				
				path.remove(ent, eweight);
				
				if (hasHandler){
					handler.leave(ent, exp, size);
				}								
				if (isStart){
					visit.leave(node);
					stack.popStart();
				}
			
			}
			
			
			
			stack.push(exp);
		}
		break;
		
		case Regex.SEQ: {
			
			int fst = 0, rst = 1;
			if (isReverse){
				// path walk from right to left
				// index = 1
				// hence sequence walk from right to left
				// use case: ?x p/q <uri>
				fst=1;
				rst=0;
			}

			stack.push(exp.getArg(rst));
			stack.push(exp.getArg(fst));
			
			eval(stack, path, start, src);
			
			stack.pop();
			stack.pop();
			stack.push(exp);
			
		}
		break;
		
		
		case Regex.PARA: 
			
			// e1 || e2
			if (start!=null) stack.pushStart(start);
			// push check(e2) (para has a 3rd argument for check)
			stack.push(exp.getArg(2));
			// push e1
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);
			// pop e1
			stack.pop();
			// pop check(e2)
			stack.pop();

			if (start!=null) stack.popStart();
			// push para
			stack.push(exp);
			
		break;
			
			
		case Regex.CHECK: 
			
				Node target = start;
				// check(e2) from e1 || e2
				// e1 has computed a path from former start to this start (which is now target of e2)
				// check there is a parallel path e2 from start to target
				// create new Record to check loop specific to path e2 
				Record st = new Record(Visit.create(isReverse));
				// push e2
				st.push(exp.getArg(0));
				st.setTarget(target);
				// retrieve the common start of path e1 and e2
				Node prev = stack.getStart();
				
				eval(st, path, prev, src);

				if (st.isSuccess()){
					eval(stack, path, start, src);
				}
				
				stack.push(exp);

			
		break;

		
		
		case Regex.PLUS:
			// exp+
			if (start == null && stack.getVisit().knows(exp)){
				stack.push(exp);
				return;
			}
			plus(exp, stack, path, start, src);
		break;
		
		
		case Regex.COUNT:
			// exp{1,n}
			count(exp, stack, path, start, src);
			break;
		
			
		case Regex.STAR: 	
			// exp*
			if (start == null && stack.getVisit().knows(exp)){
				stack.push(exp);
				return;
			}
			star(exp, stack, path, start, src);			
		break;
		
		
		case Regex.ALT:
			
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);
			stack.pop();
			
			stack.push(exp.getArg(1));
			eval(stack, path, start, src);
			stack.pop();
			
			stack.push(exp);
		break;
		
		
		case Regex.OPTION:
			
			eval(stack, path, start, src);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);	
			
			stack.pop();
			stack.push(exp);
		break;
		
		}
		
	}
	
	
	
	Regex test(Regex exp){
		
		return exp;
	}
	
	
	int reverse(int i){
		switch (i){
		case 0: return 1;
		case 1: return 0;
		}
		return 0;
	}
	
	boolean isDistinct(Record stack, Node start, Node target){
		boolean b = stack.getVisit().isDistinct(start, target);
		if (b){
			stack.getVisit().addDistinct(start, target);
		}
		return b;
	}
	
	void result(Record stack, Path path, Node start, Node src){
		
		if (path.size()>0){
			if (isDistinct){
				// distinct (start,target)
				if (! isDistinct(stack, path.firstNode(), path.lastNode())){
					return;
				}
			}
			
			boolean store = true;
			if (hasListener){
				store = listener.process(path);
			}

			if (store){
				Mapping map = result(path, gNode, src, start, isReverse);
				//System.out.println("** PF: \n" + map);
				if (map != null){
					result(map);
				}
			}
		}
		else {
			for (Entity ent : getStart(start, src)){
				
				if (isStop) return;
				
				if (ent != null){
					Node node = ent.getNode();
					if (gNode != null){ 
						src = ent.getGraph();
					}
					Mapping m = result(path, gNode, src, node, isReverse);

					if (m != null){
						result(m);
					}
				}
			}
		}
	}
	
	void result(Mapping map){
		if (isList){
			lMap.add(map);
		}
		else {
			mbuffer.put(map, true);
		}
	}
	
	
	/**
	 * exp*
	 */
	void star(Regex exp, Record stack, Path path, Node start, Node src){
		
		if (stack.getVisit().loop(exp, start)){
			// start already met in exp path: stop
			stack.push(exp);
			return;
		}		
		
		// use case: (p*/q)*
		// we must save each visited of p*
		// because it expands to p*/q . p*/q ...
		// and each occurrence of p* must have its own visited 
		
//		 Table save = stack.getVisit().unset(exp);
//		 eval(stack, path, start, src);
//		 stack.getVisit().set(exp, save);

		// first step: zero length 
		eval(stack, path, start, src);

		// restore exp*
		stack.push(exp);
		// push exp
		stack.push(exp.getArg(0));
		// second step: eval exp once more
		eval(stack, path, start, src);
		// restore stack (exp* on top)
		stack.pop();

		stack.getVisit().remove(exp, start);	
	}
	
	
	
	/**
	 * exp+
	 */
	void plus(Regex exp, Record stack, Path path, Node start, Node src){

		if (stack.getVisit().count(exp) == 0){
			
			if (checkLoop){
				//trace("** Visit: " + start);
				if (stack.getVisit().loop(exp, start)){
					stack.push(exp);
					return;
				}
			}
						
			// first step
			stack.push(exp);

			stack.getVisit().count(exp, +1);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);
			stack.pop();
			stack.getVisit().count(exp, -1);
			
			if (checkLoop){
				stack.getVisit().remove(exp, start);		
			}
			
		}
		else {
			//trace("** Visit: " + start);
			if (stack.getVisit().loop(exp, start)){
				//trace("** Loop: " + start);
				stack.push(exp);
				return;
			}
			
			// (1) leave
			eval(stack, path, start, src);

			// (2) continue
			stack.push(exp);
			
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);
			stack.pop();

			stack.getVisit().remove(exp, start);		
		}
	}

	void trace(Object str){
		System.out.println("** PF: " + str);
	}
	
	/**
	 * exp{n,m}
	 * exp{n,}
	 */
	void count(Regex exp, Record stack, Path path, Node start, Node src){
		
		if (stack.getVisit().count(exp) >= exp.getMin()){			
			
			if (checkLoop(exp)){
				if (stack.getVisit().loop(exp, start)){
					stack.push(exp);
					return;
				}
			}
			
			// min length is reached, can leave
			int save = stack.getVisit().count(exp);
			stack.getVisit().set(exp, 0);
			eval(stack, path, start, src);
			stack.getVisit().set(exp, save);

			stack.push(exp);
			
			if (stack.getVisit().count(exp) < exp.getMax()){
				// max length not reached, can continue
				
				stack.getVisit().count(exp, +1);
				stack.push(exp.getArg(0));
				eval(stack, path, start, src);
				stack.pop();
				stack.getVisit().count(exp, -1);
			}
			
			if (checkLoop(exp)){
				stack.getVisit().remove(exp, start);		
			}
			
		}
		else {
			// count(exp) < exp.getMin()
			
			if (isReverse){ 
				if (checkLoop(exp)){
					// use case: ?x exp{2,} <uri>
					// path goes backward
					stack.getVisit().insert(exp, start);
				}
			}
			// TODO: draft
			else if (checkLoop){
				if (stack.getVisit().loop(exp, start)){
					stack.push(exp);
					return;
				}
			}

			
			stack.push(exp);

			stack.getVisit().count(exp, +1);
			stack.push(exp.getArg(0));
			eval(stack, path, start, src);
			stack.pop();
			stack.getVisit().count(exp, -1);
									
			if (isReverse){
				if (checkLoop(exp)){
					// use case: ?x exp{2,} <uri>
					// path goes backward
					stack.getVisit().remove(exp, start);
				}
			}
			// TODO: draft
			else if (checkLoop){
				stack.getVisit().remove(exp, start);		
			}
		}
		
	} 
	
	
	
	Regex star(Regex exp){
		return exp;
	}
	
	boolean hasMax(Regex exp){
		return exp.getMax()!=-1 && exp.getMax()!= Integer.MAX_VALUE;
	}
	
	// for count exp {n,m}
	boolean checkLoop(Regex exp){
		return checkLoop || ! hasMax(exp);
	}
	

	
}
