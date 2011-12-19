package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventManager;

/*
 * Manage list of Mapping, result of a query
 * 
 * process select distinct
 * process group by, order by, limit offset, aggregates, having(?count>50)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */
public class Mappings extends ArrayList<Mapping> 
implements Comparator<Mapping>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int SELECT = -1;
	private static int HAVING  = -2;



	List<Node> select;
	boolean isDistinct = false,
	isValid = true,
	hasEvent = false,
	// if true, store all Mapping of the group
	isListGroup = false;
	Query query;
	Group group, distinct;
	Node fake;
	Object object;
	private Object graph;

	EventManager manager;
	
	// SPARQL: -1 (unbound first)
	// Corese order: 1 (unbound last)
	int unbound = -1;
	


	public Mappings(){
	}
	
	Mappings(Mapping map){
		add(map);
	}

	Mappings(Query q){
		query = q;
	}

	void setEventManager(EventManager man){
		manager = man;
		hasEvent = true;
	}
	
	public static Mappings create(Query q){
		Mappings lMap = new Mappings(q); 
		lMap.init(q);
		return lMap;
	}
	
	void init(Query q){
		isDistinct  = q.isDistinct();
		isListGroup = q.isListGroup();
		setSelect(q.getSelect());
		if (isDistinct){
			distinct = group(q.getSelectFun());
			distinct.setDistinct(true);
			distinct.setDuplicate(q.isDistribute());
		}
	}
	
	public Query getQuery(){
		return query;
	}
	
	public void setObject(Object o){
		object = o;
	}
	
	public Object getObject(){
		return object;
	}

	public String toString(){
		if (select == null) return super.toString();
		
		String str = "";
		int i = 1;
		for (Mapping map : this){
			str += ((i<10)?"0":"") + i + " ";
			for (Node qNode : select){
				Node node = map.getNode(qNode);
				if (node!=null){
					str += qNode + " = " + node + "; ";
				}
			}
			i++;
			str += "\n";
		}
		return str;
	}

	public List<Node> getSelect(){
		return select;
	}
	
	public Object getValue(Node qNode){
		return getValue(qNode.getLabel());
	}
	
	public Object getValue(String var){
		if (size() == 0) return null;
		Mapping map = get(0);
		Node node = map.getNode(var);
		if (node == null) return null;
		return node.getValue();
	}

	void setSelect(List<Node> nodes){
		select = nodes;
	}

	/**
	 * select distinct 
	 * in case of aggregates, accept Mapping now, distinct will be computed below
	 */
	void submit(Mapping a){
		if (query.isAggregate() || accept(a)){
			add(a);
		}
	}
	
	/**
	 * Used for distinct on aggregates
	 */
	void submit2(Mapping a){
		if (query.isAggregate()){
			 if (accept(a)){
				 add(a);
			 }
		}
		else {
			add(a);
		}
	}

	boolean accept(Node node){
		return distinct.accept(node);
	}

	boolean accept(Mapping r){
		if (select.size()==0) return true;

		if (isDistinct){
			return distinct.isDistinct(r);
		}
		return true;
	}

	void setValid(boolean b){
		isValid = b;
	}

	boolean isValid(){
		return isValid;
	}


	boolean same(Node n1, Node n2){
		if (n1 == null){
			return n2 == null;
		}
		else if (n2 == null){
			return false;
		}
		else return n1.same(n2);
	}


	void sort(){
		Collections.sort(this, this);
	}


	public int compare(Mapping r1, Mapping r2) {
		Node[] order1 = r1.getOrderBy();
		Node[] order2 = r2.getOrderBy();

		//boolean reverse[]=query.getReverse(); // sort in reverse order
		List<Exp> orderBy = query.getOrderBy();

		int res = 0;
		
		for (int i = 0; i < order1.length && i < order2.length && res == 0; i++) {

			if (order1[i] != null && order2[i] != null) { // sort ?x
				res = order1[i].compare(order2[i]);
			}
			//      unbound 
			else if (order1[i] == null) { // unbound var
				if (order2[i] == null){
					res = 0;
				}
				else {
					res = unbound;
				}
			}
			else if (order2[i] == null){
				res = - unbound;
			}
			else {
				res = 0;
			}
			
			if (orderBy.get(i).status()){ 
				res = desc(res);
			}

		}
		return res;
	}

	int desc(int i){
		if (i < 0) return +1;
		else return -1;
	}
	
	/***********************************************************
	 * 
	 * 	Aggregates
	 * 
	 * 1. select [distinct] var where
	 * 2. group by
	 * 3. count/min/max as var
	 * 4. order by 
	 * 5. limit offset
	 * 
	 * group by with aggregate return one Mapping per group 
	 * where the mapping hold the result of the aggregate
	 * 
	 */



	/**
	 *  order by
	 *  offset
	 */
	void complete(){
		if (query.getOrderBy().size()>0){
			sort();
		}
		if (query.getOffset() > 0){
			// skip offset
			// TODO: optimize this
			for (int i=0; i<query.getOffset() && size()>0; i++){
				remove(0);
			}
		}
		while (size() > query.getLimit()){
			remove(size()-1);
		}
	}




	/** 
	 * select count(?doc) as ?count
	 * group by ?person ?date
	 * order by ?count
	 * having(?count > 100)
	 * TODO:
	 * optimize this because we enumerate all Mappings for each kind of aggregate
	 * we could enumerate Mappings once and compute all aggregates for each map
	 */
	void aggregate(Evaluator evaluator, Memory memory){
//		System.out.println("** M: aggregate " + size());
//		long t1 = new Date().getTime();
		aggregate(query, evaluator, memory, true);
//		long t2 = new Date().getTime();
//		System.out.println((t2-t1)/1000.0);
	}
	
	public void aggregate(Query qq, Evaluator evaluator, Memory memory){
		aggregate(qq, evaluator, memory, false);
	}
	
	void aggregate(Query qq, Evaluator evaluator, Memory memory, boolean isFinish){
		if (size() == 0) return ;
		boolean isEvent = hasEvent;

		// select (count(?n) as ?count)
		aggregate(evaluator, memory, qq.getSelectFun(), true);
		
		// order by count(?n)
		aggregate(evaluator, memory, qq.getOrderBy(), false);
	
		if (qq.getHaving() != null){
			if (isEvent){
				Event event = EventImpl.create(Event.AGG, query.getHaving());
				manager.send(event);
			}
			eval(evaluator, qq.getHaving(), memory, HAVING);
		}

		finish(qq);
	}
		
	
	void finish(Query qq){
		if (qq.hasGroupBy() && ! qq.isConstruct()){ 
			// after group by (and aggregate), leave one Mapping for each group
			// with result of the group
			groupBy();
		}
		else if (qq.getHaving() != null){
			// clause 'having' with no group by
			// select (max(?x) as ?max where {}
			// having(?max > 100)
			having();
		}
		else if (qq.isAggregate() && ! qq.isConstruct()){
			clean();
		}
	}
	
	
	void having(){
		if (isValid()){
			clean();
		}
		else {
			clear();
		}
	}

	void clean(){
		if (size()>1){
			Mapping map = get(0);
			clear();
			add(map);
		}
	}
	
	void aggregate(Evaluator evaluator, Memory memory, List<Exp> list, boolean isSelect){
		int n = 0;
		for (Exp exp : list){
			if (exp.isAggregate()){
				// perform group by and then aggregate
				if (hasEvent){
					Event event = EventImpl.create(Event.AGG, exp);
					manager.send(event);
				}
				eval(evaluator, exp, memory, (isSelect)?SELECT:n++);
			}
		}
	}
	

	/** 
	 * select count(?doc) as ?count
	 * group by ?person ?date
	 * order by ?count
	 */

	private	void eval(Evaluator eval, Exp exp, Memory mem, int n){
		if (exp.isExpGroupBy()){
			// min(?l, groupBy(?x, ?y)) as ?min
			Group g = createGroup(exp);
			aggregate(g, eval, exp, mem, n);
			if (exp.isHaving()){
				// min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min
				having(eval, exp, mem, g);
				// remove global group if any 
				// may be recomputed with new Mapping list
				setGroup(null);
			}
		}
		else if (query.hasGroupBy()){
			// perform group by and then aggregate
			aggregate(getCreateGroup(), eval, exp, mem, n);
		}
		else {
			apply(eval, exp, mem, n);
		}
	}

	
	
	/**
	 * exp : min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min)
	 * test the filter, remove Mappping that fail
	 */
	void having(Evaluator eval, Exp exp, Memory mem, Group g){
		Filter f = exp.getHavingFilter();
		clear();
		for (Mappings lm : g.getValues()){
			for (Mapping map : lm){
				mem.push(map, -1);
				if (eval.test(f, mem)){
					add(map);
				}
				mem.pop(map);
			}
		}
	}
	
	

	/**
	 * Compute aggregate (e.g. count() max()) and having
	 * on one group or on whole result (in both case: this Mappings)
	 * in order to be able to compute both count(?doc) and ?count
	 * we bind Mapping into memory
	 */
	private	boolean apply(Evaluator eval, Exp exp, Memory memory, int n){
		int select = SELECT;
		// get first Mapping in current group
		Mapping firstMap = get(0);
		// bind the Mapping in memory to retrieve group by variables
		//if (n != select) 
			memory.push(firstMap, -1);
		boolean res = true;

		if (n == HAVING){
			res = eval.test(exp.getFilter(), memory);
			
			if (hasEvent){
				Event event = EventImpl.create(Event.FILTER, exp, res);
				manager.send(event);
			}
			setValid(res);
		}
		else {
			Node node = null;
			
			if (exp.getFilter() == null){
				// order by ?count
				node = memory.getNode(exp.getNode());			
			}
			else {
				node = eval.eval(exp.getFilter(), memory);
			}
			
			if (hasEvent){
				Event event = EventImpl.create(Event.FILTER, exp, node);
				manager.send(event);
			}
			
			for (Mapping map : this){

				if (n == select){
					map.setNode(exp.getNode(), node);
				}
				else {
					map.setOrderBy(n,  node);
				}
			}
		}

		//if (n != SELECT) 
			memory.pop(firstMap);
		return res;
	}


	/**
	 * Process aggregate for each group
	 * select, order by, having
	 */
	private	void aggregate(Group group, Evaluator eval, Exp exp, Memory mem, int n){
		//if (group == null) group = createGroup();

		for (Mappings maps : group.getValues()){
			// eval aggregate filter for each group 
			// set memory current group
			// filter (e.g. count()) will consider this group
			if (hasEvent) maps.setEventManager(manager);
			mem.setGroup(maps);
			maps.apply(eval, exp, mem, n);
			mem.setGroup(null);
		}
	}
	



	/** 
	 * process group by
	 * leave one Mapping within each group
	 */
	public	void groupBy(){
		// clear the current list
		groupBy(getCreateGroup());
	}
		
	
	public Mappings groupBy(List<Exp> list){
		Group group = createGroup(list);
		groupBy(group);
		return this;
	}
	
	
	/**
	 * Generate the Mapping list according to the group
	 * PRAGMA: replace the original list by the group list
	 */
	public void groupBy(Group group){
		clear();
		for (Mappings lMap : group.getValues()){
			int start = 0;
			if (lMap.isValid()){
				// clause 'having' may have tagged first mapping as not valid
				start = 1;
				Mapping map = lMap.get(0);
				if (isListGroup && map != null){
					map.setMappings(lMap);
				}
				// add one element for current group
				// check distinct if any
				submit2(map);
			}
		}
	}
	

	
	/**
	 * Project on select variables of query 
	 * Modify all the Mapping
	 */
	public Mappings project(){
		for (Mapping map : this){
			map.project(query);
		}
		return this;
	}



	/**
	 * for group by ?o1 .. ?on
	 */
	private	Group createGroup(){
		if (query.isConnect()){
			// group by any
			Merge group = new Merge(this);
			group.merge();
			return group;
		}
		else {
			Group group = createGroup(query.getGroupBy());
			return group;
		}
	}
	
	private Group getCreateGroup(){
		if (group == null){
			 group = createGroup();
		}
		return group;
	}
	
	private Group getGroup(){
		return group;
	}
	
	private void setGroup(Group g){
		group = g;
	}
	
	/**
	 * Generate a group by list of variables
	 */
	public Group defineGroup(List<String> list){
		ArrayList<Exp> el = new ArrayList<Exp>();
		for (String name : list){
			el.add(query.getSelectExp(name));
		}
		return createGroup(el);
	}
	
	
	/**
	 * group by
	 */
	Group createGroup(List<Exp> list){
		return createGroup(list, false);
	}
	
	Group createGroup(Exp exp){
		return createGroup(exp.getExpGroupBy(), true);
	}
	
	Group createGroup(List<Exp> list, boolean extend){
		Group group = new Group(list);
		group.setDuplicate(query.isDistribute());
		group.setExtend(extend);

		for (Mapping map : this){
			group.add(map);
		}
		return group;
	}

	
	/**
	 * 	for select distinct
	 */
	Group group(List<Exp> list){
		Group group = new Group(list);
		return group;
	}

	public Node max(Node qNode){
		Node node = minmax(qNode, true);
		return node;
	}

	public Node min(Node qNode){
		return minmax(qNode, false);
	}


	private	Node minmax(Node qNode, boolean isMax){
		Node res = null ;
		for (Mapping map : this){
			Node node = map.getNode(qNode);
			if (res == null){
				res = node;
			}
			else if (node != null){
				if (isMax){
					if (node.compare(res) > 0){
						res = node;
					}
				}
				else if (node.compare(res) < 0){
					res = node;
				}
			}

		}
		return res;	
	}

	/**
	 * Generic aggregate
	 * eval is Walker
	 * it applies the aggregate f (e.g. sum(?x)) on the list of Mapping
	 * with Mapping as environment to get variable binding
	 */
	void process(Evaluator eval, Filter f){
		for (Mapping map : this){
			eval.eval(f, map);
		}
	}

	/*********************************************************************
	 * 
	 * Pipeline Solutions implementation
	 * These operations use the select nodes if any and otherwise the query nodes
	 * 
	 * 
	 *********************************************************************/

	public Mappings union(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m : this){
			res.add(m);
		}
		for (Mapping m : lm){
			res.add(m);
		}
		return res;
	}
	
	public Mappings and(Mappings lm){
		return join(lm);
	}
	
	public Mappings join(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			for (Mapping m2 : lm){
				Mapping map = m1.join(m2);
				if (map != null){
					res.add(map);
				}
			}
		}

		return res;
	}

	public Mappings minus(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			boolean ok = true;
			for (Mapping m2 : lm){
				if (m1.compatible(m2)){
					ok = false;
					break;
				}
			}
			if (ok){
				res.add(m1);
			}
		}
		return res;
	}

	public Mappings optional(Mappings lm){
		return option(lm);
	}

	public Mappings option(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			boolean ok = false;
			for (Mapping m2 : lm){
				Mapping map = m1.join(m2);
				if (map != null){
					ok = true;
					res.add(map);
				}
			}
			if (! ok){
				res.add(m1);
			}
		}

		return res;
	}

	public Mappings project(List<Exp> lExp){
		Mappings res = new Mappings();

		return res;
	}

	public Mappings rename(List<Exp> lExp){
		Mappings res = new Mappings();
		for (Mapping m : this){
			res.add(m.rename(lExp));
		}

		return res;
	}

	/**
	 * Assign select nodes to all Mapping
	 */
	public void finalize(){
		if (getSelect() != null){
			Node[] nodes = new Node[getSelect().size()];
			int i = 0;
			for (Node node : getSelect()){
				nodes[i++] = node;
			}

			for (Mapping map : this){
				map.setSelect(nodes);
			}
		}
	}

	public void setGraph(Object graph) {
		this.graph = graph;
	}

	public Object getGraph() {
		return graph;
	}


//
//	public Solutions union(Solutions s){
//		return union((Mappings) s);
//	}
//
//	public Solutions join(Solutions s){
//		return join((Mappings) s);
//	}
//
//	public Solutions minus(Solutions s){
//		return minus((Mappings) s);
//	}
//
//	public Solutions option(Solutions s){
//		return option((Mappings) s);
//	}



//	@Deprecated
//	private	void aggregate2(Group group, Evaluator eval, Exp exp, Memory mem, int n){
//		if (Group.test){
//			aggregate2(group, eval, exp, mem, n);
//			return;
//		}
//
//		for (List<Mappings> lGroup :  group.values()){
//			// lGroup is a list of groups (that share same value for first groupBy variable)
//
//			for (Mappings maps : lGroup){
//				// eval aggregate filter for each group 
//				// set memory current group
//				// filter (e.g. count()) will consider this group
//				if (hasEvent) maps.setEventManager(manager);
//				mem.setGroup(maps);
//				maps.apply(eval, exp, mem, n);
//				mem.setGroup(null);
//			}
//		}
//	}
	
//	@Deprecated
//	public void groupBy2(Group group){
//		if (Group.test){
//			groupBy2(group);
//			return;
//		}
//		
//		clear();
//		for (List<Mappings> ll :  group.values()){
//			for (Mappings lMap : ll){
//				int start = 0;
//				if (lMap.isValid()){
//					// clause 'having' may have tagged first mapping as not valid
//					start = 1;
//					Mapping map = lMap.get(0);
//					if (isListGroup && map != null){
//						map.setMappings(lMap);
//					}
//					// add one element for current group
//					add(map);
//				}
//			}
//		}
//	}

}
