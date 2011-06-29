package fr.inria.edelweiss.kgenv.parser;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Forall;
import fr.inria.acacia.corese.triple.parser.IfThenElse;
import fr.inria.acacia.corese.triple.parser.Parser;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;


/**
 * Compiler of SPARQL AST to KGRAM Exp Query
 * Use Corese SPARQL parser
 * Use an abstract compiler to generate target edge/node/filter implementations
 * 
 * sub query compiled as distinct edge/node to avoid inappropriate 
 * type inference on nodes
 * 
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class Transformer implements ExpType {	
	public static final String ROOT = "?_kgram_";
	public static final String THIS = "?this";

	int count = 0;

	CompilerFactory fac;
	Compiler compiler;
	Sorter sort;
	//Table table;
	ASTQuery ast;

	int ncount = 0, rcount = 0;
	boolean fail = false,
	isEdgeForType = true,
	isSPARQL1 = true;
	String namespaces, base;
	List<String> from, named;

	Transformer(){
	}

	Transformer(CompilerFactory f){
		fac = f;
	}

	public static Transformer create(CompilerFactory f){
		return new Transformer(f);
	}
	
	public static Transformer create(){
		return new Transformer();
	}
	

	public void setFrom(List<String> list){
		from = list;
	}
	public void setNamed(List<String> list){
		named = list;
	}
	
	public void set(Sorter s){
		sort = s;
	}

	public Query transform(String squery) throws EngineException{
		
		ast = ASTQuery.create(squery);
		ast.setDefaultNamespaces(namespaces);
		ast.setDefaultBase(base);
		ast.setEdgeForType(isEdgeForType);
		ast.setKgram(true);
		ast.setSPARQL1(isSPARQL1);

		if (from!=null) ast.setDefaultFrom(from);
		if (named!=null) ast.setDefaultNamed(named);

		ParserSparql1.create(ast).parse();

		//System.out.println(graph.getInference().display());		
		
		//bind(ast);
		
		//Parser.create().ncompile(ast);
		
		Query q = transform(ast);
		
		return q;
		
	}
	
	public Query transform (ASTQuery ast){
		this.ast = ast;
		Parser.create().ncompile(ast);

		if (fac == null) fac = new CompilerFacKgram();			
		ast.setKgram(true);

		compiler = fac.newInstance(); 
		compiler.setAST(ast);
		// generate Exp from AST body
		//Exp exp = compile(ast);
		
		Exp exp = compile(ast.getBody(), false);
	
		Query q =  create(exp);
		
		if (ast.isConstruct() || ast.isDescribe()){
			// use case: kgraph only
			Exp cons = construct(ast);
			q.setConstruct(cons);
			q.setConstruct(true);
		}
		if (ast.isDelete()){
			Exp del = delete(ast);
			q.setDelete(del);
			q.setDelete(true);
		}
		if (ast.isUpdate()){
			q.setUpdate(true);
		}

		path(q, ast);
		
		// retrieve select nodes for query:
		complete(q, ast);

		q.setAST(ast);


		having(q, ast);
		
		bindings(q, ast);


		if (compiler.isFail() || fail){
			q.setFail(true);
		}	

		q.setSort(ast.isSorted());
		q.setDebug(ast.isDebug());
		q.setRelax(ast.isMore());

		return q;
	}
	
	Query create(Exp exp){
		Query q = Query.create(exp);
		if (sort!=null){
			q.set(sort);
		}
		return q;
	}


	public boolean isEdgeForType(){
		return isEdgeForType;
	}

	public void setEdgeForType(boolean b){
		isEdgeForType = b;
	}

	public void setNamespaces(String ns){
		namespaces = ns;
	}
	
	public void setBase(String ns){
		base = ns;
	}

	public void setSPARQL1(boolean b){
		isSPARQL1 = b;
	}
	
	void bind(ASTQuery ast){
		if (ast.getVariableBindings()!=null){
			Expression exp = ast.bind();
			if (exp == null) return;
			Triple triple = Triple.create(exp);
			ast.getBody().add(triple);
		}
	}
	
	
	void bindings(Query q, ASTQuery ast){
		   if (ast.getValueBindings() == null) return ;
		   
		   Node[] nodes = bind(q, ast);
		   
		   List<Mapping> lMap = new ArrayList<Mapping>();
		   
		   for (List<Constant> lVal :  ast.getValueBindings()){
			   if (ast.getVariableBindings().size() != lVal.size()){
				   q.setCorrect(false);
			   }
			   else {
				   List<Node> list = bind(lVal);
				   Mapping map = create(nodes, list);
				   lMap.add(map);
			   }
		   }
		   
		   q.setMapping(lMap);
	   }

	
	Node[] bind(Query q, ASTQuery ast){

		List<Node> lNode = new ArrayList<Node>();

		for (Variable var : ast.getVariableBindings()){
			Node qNode = q.getProperAndSubSelectNode(var.getLabel());
			if (qNode == null){
				// TODO: select !!!
				qNode = compiler.createNode(var);
				q.index(qNode);
			}
			lNode.add(qNode);
		}

		Node[] nodes = new Node[lNode.size()];
		int i = 0;
		for (Node node : lNode){
			nodes[i++] = node;
		}
		return nodes;

	}
	
	List<Node> bind(List<Constant> lVal){
		List<Node> lNode = new ArrayList<Node>();
		for (Constant val : lVal){
			Node node = null;
			if (val != null) {
				node = compiler.createNode(val);
			}
			lNode.add(node);
		}
		return lNode;
	}

	Mapping create(Node[] lNode, List<Node> lVal){
		Node[] nodes = new Node[lVal.size()];
		int i = 0;
		for (Node node : lVal){
			nodes[i++] = node;
		}
		return Mapping.create(lNode, nodes);
	}
	

	/**
	 * Generate a new compiler for each (sub) query in order to get fresh new nodes
	 */
	Exp  compile(ASTQuery ast){
		return compile(ast, ast.getExtBody());
	}
	
	Exp  construct(ASTQuery ast){
		return compile(ast, ast.getConst());
	}
	
	Exp  delete(ASTQuery ast){
		return compile(ast, ast.getDelete());
	}
	
	Exp  compile(ASTQuery ast, fr.inria.acacia.corese.triple.parser.Exp exp){
		Compiler save = compiler;
		compiler = fac.newInstance();
		compiler.setAST(ast);
		Exp ee = compile(exp, false);
		compiler = save;
		return ee;
	}

	public ASTQuery getAST(){
		return ast;
	}

	public Compiler getCompiler(){
		return compiler;
	}


	void complete(Query qCurrent, ASTQuery ast){	
		qCurrent.collect();
		qCurrent.setSelectFun(select(qCurrent, ast));
		qCurrent.setOrderBy(orderBy(qCurrent, ast));
		qCurrent.setGroupBy(groupBy(qCurrent, ast));
		
		qCurrent.setDistinct(ast.isDistinct());
		qCurrent.distinct();
		qCurrent.setFrom (nodes(ast.getActualFrom()));
		qCurrent.setNamed(nodes(ast.getActualNamed()));

		qCurrent.setLimit(Math.min(ast.getMaxResult(), ast.getMaxProjection()));
		qCurrent.setOffset(ast.getOffset());

		qCurrent.setGraphNode(createNode());
		
		// check semantics of select vs aggregates and group by
		boolean correct = qCurrent.check();
		if (! correct){
			//System.out.println("** Query check fails");
			qCurrent.setCorrect(false);
		}
		else {
			qCurrent.setCorrect(ast.isCorrect());
		}

	}

	void path(Query q, ASTQuery ast){
		if (ast.getRegexTest().size()>0){
			Node node = compiler.createNode(Variable.create(THIS));
			q.setPathNode(node);
		}
		for (Expression test : ast.getRegexTest()){
			// ?x c:isMemberOf[?this != <inria>] + ?y
			Filter f = compile(test);
			q.addPathFilter(f);
		}
	}

	void having(Query q, ASTQuery ast){
		if (ast.getHaving()!=null){
			Filter having = compile(ast.getHaving());
			q.setHaving(Exp.create(FILTER, having));
		}
	}


	/**
	 * Retrieve/Compute the nodes for the select of qCurrent Query
	 * Nodes may be std Node or  select fun() as ?var node
	 * in this last case we may create a node from scratch for ?var
	 * This function is called 
	 * - once for each subquery 
	 * - once for the global query  
	 */
	List<Exp> select(Query qCurrent, ASTQuery ast){
		List<Exp> select = new ArrayList<Exp>();
		// list of query nodes created for variables in filter that need
		// an outer node value
		List<Node> lNodes = new ArrayList<Node>();

		if (ast.isSelectAll() || ast.isConstruct()){
			// select *
			// get nodes from query nodes and edges
			if (Query.test) select = qCurrent.getSelectNodesExp();
			else select = qCurrent.getNodesExp();
		}

		for (Variable var : ast.getSelectVar()){	
			// retrieve var node from query
			String varName = var.getName();
			Node node = getNode(qCurrent, var);
			// process filter if any
			Filter f = null;		
			Expression ee = ast.getExpression(varName);
			
			if (ee != null){
				// select fun() as var
				ee = ee.process(ast);
				if (ee != null){
					f = compile(ee);
					if (f.isAggregate()){
						// store select variable in aggregate expression
						// use case: sample(f(?x)) as ?y
						f.getExp().setArg(var);
					}
				}
			}

			Exp exp = Exp.create(NODE, node);
			// select fun() as var
			if (f != null){
				exp.setFilter(f);
				checkFilterVariables(qCurrent, f, select, lNodes);
				function(qCurrent, exp, var);
			}

			select.add(exp);
		}	

		for (Node node : lNodes){
			Exp exp = Exp.create(NODE, node);
			exp.status(true);
			select.add(exp);
		}
		return select;
	}

	Node getNode(Query qCurrent, Variable var){
		Node node = getProperAndSubSelectNode(qCurrent, var.getName());
		if (node == null){
			node = compiler.createNode(var);
		}
		return node;
	}

	Node getProperAndSubSelectNode(Query q, String name){
		Node node;
		if (Query.test) node = q.getSelectNodes(name);
		else node = q.getProperAndSubSelectNode(name);
		return node;
	}

	/**
	 * If filter isFunctionnal()
	 * create it's query node list
	 */
	void function(Query qCurrent, Exp exp, Variable var){
		if (exp.getFilter().isFunctional()){
			if (var.getVariableList()!=null){
				// sql() as (?x, ?y)
				for (Variable vv : var.getVariableList()){
					Node qNode = getNode(qCurrent, vv);
					exp.addNode(qNode);
				}
			}
			else {
				exp.addNode(exp.getNode());
			}
		}
	}

	/**
	 *  Check that variables in filter have corresponding proper node 
	 *  otherwise create a Node to import value from outer query
	 * @param query
	 * @param f
	 */
	void checkFilterVariables(Query query, Filter f, List<Exp> select, List<Node> lNodes){
		List<String> lVar = f.getVariables();
		for (String name : lVar){
			Node node = getProperAndSubSelectNode(query, name);
			if (node == null){
				if (! containsExp(select, name) && ! containsNode(lNodes, name)){
					node = compiler.createNode(name);
					lNodes.add(node);
				}
			}
		}
	}



	boolean containsExp(List<Exp> lExp, String name){
		for (Exp exp : lExp){
			if (exp.getNode().getLabel().equals(name)){
				return true;
			}		
		}
		return false;
	}

	boolean containsNode(List<Node> lNode, String name){
		for (Node node : lNode){
			if (node.getLabel().equals(name)){
				return true;
			}		
		}
		return false;
	}


	List<Exp> orderBy(Query qCurrent, ASTQuery ast){
		List<Exp> order = orderBy(qCurrent, ast.getSort(), ast);
		if (order.size()>0){
			//boolean[] desc = new boolean [order.size()];
			int n = 0;
			for (boolean b : ast.getReverse()){
				//desc[n] = b;
				order.get(n).status(b);
				n++;
			}
			//qCurrent.setReverse(desc);
		}

		return order;
	}


	List<Exp> groupBy(Query qCurrent, ASTQuery ast){
		List<Exp> list = orderBy(qCurrent,  ast.getGroupBy(), ast);
		qCurrent.setConnect(ast.isConnex());
		return list;
	}


	List<Exp> orderBy(Query qCurrent,  List<Expression> input, ASTQuery ast){
		List<Exp> list = new ArrayList<Exp>();
		for (Expression ee : input){
			if (ee.isVariable()){
				Exp exp = qCurrent.getSelectExp(ee.getName());
				if (exp != null){
					// select var fun() as var
					list.add(exp);
				}
				else {
					Node node = getProperAndSubSelectNode(qCurrent, ee.getName());
					exp = Exp.create(NODE, node);
					list.add(exp);
				}
			}
			else {
				// rewrite select fun() as var in ee
				ee = ee.process(ast);
				if (ee != null){
					Filter f = compile(ee);
					Node node = createNode(); 
					Exp exp = Exp.create(NODE, node);
					exp.setFilter(f);
					list.add(exp);
				}
			}
		}
		return list;
	}

	/**
	 * Create a fake query node 
	 */
	Node createNode(){
		String name = ROOT + count++;
		Node node = compiler.createNode(name);
		return node;
	}

	List<Node> nodes(List<String> from){
		List<Node> nodes = new ArrayList<Node>();
		for (String uri : from){
			Constant cst = ast.createConstant(uri);
			nodes.add(new NodeImpl(cst));
		}
		return nodes;
	}


	/**
	 * Compile AST statements into KGRAM statements
	 * Compile triple into QueryRelation, filter into QueryMarker
	 */
	Exp compile(
			fr.inria.acacia.corese.triple.parser.Exp query, boolean opt){
		return compile(query, opt, 0);
	}
	
	Exp compile(
			fr.inria.acacia.corese.triple.parser.Exp query, boolean opt, int level){

		List<Filter> qvec;
		Exp exp = null;

		query.setAST(ast);

		int type = getType(query);
		opt = opt || type == OPTION || type == OPTIONAL || 
		type == UNION || type == MINUS;

		//System.out.println("** T compile: "+ ExpType.TITLE[type] + " " + query + " " + opt);

		switch(type){


		case FILTER:

			qvec = compiler.compileFilter((Triple)query);

			if (qvec.size()==1){
				exp = Exp.create(FILTER, qvec.get(0));
				compileExist(qvec.get(0).getExp(), opt);
			}
			else {
				exp =  Exp.create(AND);
				for (Filter qm : qvec){
					Exp f = Exp.create(FILTER, qm);
					compileExist(qm.getExp(), opt);
					exp.add(f);
				}
			}


			break;



		case EDGE:
			// match Edge and Node (?x rdf:type c:Person will be a Node)

			Triple tt = (Triple) query ;

			if (opt){
				// union/option: no type inference
				tt.setOption(true);
			}

			compiler.compile(tt);

			Edge r  = compiler.getEdge();
			Node  c = compiler.getNode();

			Exp t = Exp.create(EMPTY);

			if (r != null){
				//src = r.getNode(IRelation.ISOURCE);
				t = Exp.create(EDGE, r);

				if (tt.isXPath()){
					t.setType(EVAL);
					//t.setRegex(tt.getRegex());
					Filter xpath = compiler.compile(tt.getXPath());
					t.setFilter(xpath);
				}
				else if (tt.isPath()){
					t.setType(PATH);
					Expression regex = tt.getRegex();
					if (regex == null){
						// there may be a match($path, regex)
					}
					else {
						regex.compile(ast);
						t.setRegex(regex);
					}
					t.setObject(tt.getMode());
				}
			}
			else if (c != null)  {
				t = Exp.create(NODE, c);
			}

			// type checking may be compiled as filters (in union or option)
			qvec = compiler.getFilters();

			if (qvec.size()>0){
				Exp a = Exp.create(AND, t);

				for (Filter qm : qvec){
					Exp f = Exp.create(FILTER, qm);
					compileExist(qm.getExp(), opt);
					a.add(f);
				}
				exp = a;
			}
			else {
				exp = t;
			}

			break;




		case QUERY:

			ASTQuery ast = query.getQuery();

			// subquery is compiled using a new compiler to get fresh new nodes
			// to prevent type inference on nodes between outer and sub queries
			exp = compile(ast);

			// complete select, order by, group by
			//complete(exp);

			Query q =  create(exp);

			complete(q, ast);

			having(q, ast);
			
			q.setBind(ast.isBind());

			exp = q;
			

			break;




		default:

			//			if (!opt && type == AND && ! query.isScope() && query.size() == 1){
			//				return compile(query.get(0), opt);
			//			}

			exp = Exp.create(cpType(type));

		boolean hasBind = false;
		// compile body		
		for (fr.inria.acacia.corese.triple.parser.Exp ee : query.getBody()){

			Exp tmp = compile(ee, opt, level+1);
			
			if (tmp.isQuery() && tmp.getQuery().isBind()){
				hasBind = true;
			}
			exp.insert(tmp);

		}
		
		if (hasBind && level>0){
			// pop bind(f(?x) as ?y) at the end of group pattern
			// unless body pattern which keep binding for select
			pop(exp);
		}

		path(exp);

		if (query.isMinus()){
			// add a fake graph node 
			// use case:
			// graph ?g {PAT minus {PAT}}
			exp.setNode(createNode());
		}
		else if (query.isScope()){
			// place holder to process scoped expression
			// exp is an AND

			//exp = Exp.create(IF, exp.first(), exp.rest(), exp.last());				

			Exp tmp = Exp.create(EXTERN);
			tmp.setObject("?x");
			//exp.add(tmp);

			//exp.add(Exp.create(SCAN));

			//exp = Exp.create(SCAN, exp);

			//Exp option = Exp.create(OPTION2, Exp.create(AND, exp.get(0), exp.get(1)), exp.get(2).first());
			//System.out.println(option);
			//exp = option;
		}

		else if (query.isNegation()){
			exp = Exp.create(NOT, exp);
		}

		else if (query.isForall()){
			Exp first = Exp.create(AND);
			Forall fa = (Forall) query;

			for (fr.inria.acacia.corese.triple.parser.Exp ee : fa.getFirst().getBody()){
				Exp tmp = compile(ee, opt);
				first.add(tmp);
			}

			exp = Exp.create(FORALL, first, exp);
		}

		else if (query.isIfThenElse()){
			IfThenElse ee = (IfThenElse) query;
			Exp e1 = compile(ee.getIf(), opt);
			Exp e2 = compile(ee.getThen(), opt);
			Exp e3 = null;
			if (ee.getElse()!=null){
				e3 = compile(ee.getElse(), opt);
			}
			exp.add(e1);
			exp.add(e2);
			if (e3 !=null){
				exp.add(e3);
			}
		}
		else if (type == GRAPH){
			// bind the graph variable/uri

				// use case: graph ?g {}
				// no edge in graph pattern
				
			Source srcexp = (Source) query;
			Node src = compile(srcexp.getSource());

			// create a NODE kgram expression for graph ?g
			Exp node = Exp.create(NODE, src);
			Exp gnode = Exp.create(GRAPHNODE, node);

			exp.add(0, gnode);

			src = null;


		}
		else if (type == SERVICE){
			Service service = (Service) query;
			Node src = compile(service.getService());
			Exp node = Exp.create(NODE, src);
			exp.add(0, node);
		}

		}

		return exp;

	}
	
	Node compile(Atom at){
		// create triple(?g rdf:type rdfs:Resource)
		Triple triple = Triple.create(at, Constant.create(RDFS.RDFTYPE), Constant.create(RDFS.RDFSRESOURCE));
		//triple.setType(true);
		compiler.compile(triple);
		// src is the query concept for ?g 
		Node src = compiler.getNode();
		if (src == null){
			Edge ee = compiler.getEdge();
			if (ee!=null){
				src = ee.getNode(0);
			}
		}
		return src;
	}
	
	
	
	void pop(Exp exp){
		List<Exp> list = new ArrayList<Exp>();
		for (Exp ee : exp){
			if (ee.isQuery() && ee.getQuery().isBind()){
				list.add(Exp.create(POP, ee));
			}
		}
		for (Exp ee : list){
			exp.insert(ee);
		}
	}

	
	
	/**
	 * Rewrite fun() as ?var in exp
	 * Compile exists {}
	 */
	Filter compile(Expression exp){
		Filter f = compiler.compile(exp);
		compileExist(f.getExp(), false);
		return f;
	}
	
	
	/**
	 * Rewrite exp if it is a composite expression over aggregates:
	 * 
	 * select (sum(exp1) / count(exp2)  as ?a) 
	 * compiled as :
	 * select (sum(exp1) as ?e1) (count(exp2) as ?e2) (agg(?e1 / ?e2) as ?a)
	 */
	void compileAggregate(Expression exp){
		
	}


	/**
	 * filter(exist {PAT})
	 */
	void compileExist(Expr exp, boolean opt){
		if (exp.oper() == ExprType.EXIST){
			Term term = (Term) exp;
			Exp pat = compile(term.getExist(), opt);
			term.setPattern(pat);
		}
		else {
			for (Expr ee : exp.getExpList()){
				compileExist(ee, opt);
			}
		}
	}


	void path(Exp exp){		
		for (Exp ee : exp){
			if (ee.isPath()){
				for (Exp ff : exp){
					if (ff.isFilter()){
						processPath(ee, ff);
					}
				}
				if (ee.getRegex() == null){
					String name = ee.getEdge().getLabel();
					Term star = Term.function(Term.STAR, Constant.create(name));
					star.compile(ast);
					ee.setRegex(star);
				}
			}
		}
	}




	/**
	 * Check if filter f concerns path e
	 * for regex, mode, min, max
	 * store them in Exp e
	 */
	void processPath(Exp exp, Exp ef){
		//System.out.println(f + " " + e );
		Filter f = ef.getFilter();
		Edge e = exp.getEdge();
		Node n = e.getEdgeVariable();

		List<String> lVar = f.getVariables();
		if (lVar.size()==0) return ;
		if (n==null) return;
		if (! n.getLabel().equals(lVar.get(0))) return ;

		//System.out.println(f + " " + e );
		Regex regex = compiler.getRegex(f);

		if (regex != null && exp.getRegex()==null){
			// mode: i d s
			String mode = compiler.getMode(f);
			if (mode != null){
				exp.setObject(mode);
				if (mode.indexOf("i")!=-1){
					regex = Term.function(Term.SEINV, ((Expression)regex));
				}
			}
			((Expression)regex).compile(ast);
			exp.setRegex(regex);
		}
		else {
			if (compiler.getMin(f) != -1){
				exp.setMin(compiler.getMin(f));
			}
			if (compiler.getMax(f) != -1){
				exp.setMax(compiler.getMax(f));
			}
		}
	}


	int getType(fr.inria.acacia.corese.triple.parser.Exp query){
		if (query.isFilter()){
			return FILTER;
		}
		else if (query.isTriple()){
			return EDGE;
		}
		else if (query.isUnion()){
			return UNION;
		}
		else if (query.isOptional()){
			if (query.isSPARQL()) return OPTIONAL;
			return OPTION;
		} 
		else if (query.isMinus()){
			return MINUS;
		} 
		else if (query.isGraph()){
			return GRAPH;
		} 
		else if (query.isService()){
			return SERVICE;
		} 
		else if (query.isQuery()){
			return QUERY;
		} 
		else if (query.isExist()){
			return EXIST;
		} 
		else if (query.isForall()){
			return FORALL;
		} 
		else if (query.isIfThenElse()){
			return IF;
		} 
		else if (query.isNegation()){
			return NOT;
		} 
		else if (query.isAnd()){
			return AND;
		} 
		else return EMPTY;
	}

	int cpType(int type){
		switch (type){
		case FORALL:
		case NOT: return AND;
		default: return  type;
		}
	}


	/***************************************/















}
