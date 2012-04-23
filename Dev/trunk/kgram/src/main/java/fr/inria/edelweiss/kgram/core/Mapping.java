package fr.inria.edelweiss.kgram.core;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Result;
import fr.inria.edelweiss.kgram.path.Path;
import fr.inria.edelweiss.kgram.tool.EnvironmentImpl;

/*
 * An elementary result of a query or a subquery
 * Store query/target nodes and edges 
 * Store path edges in case of path node
 * Store order by nodes
 * Store nodes for select fun() as ?var
 * 
 * Implements Environment to enable evaluate having (?count>50)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */

public class Mapping  
	extends EnvironmentImpl 
	implements Result, Environment
{
		
	Edge[] qEdges, edges;

	// path edges when Mapping has a path result
	Path[] lPath;
	
	Node[] qNodes, nodes, 
	// select nodes
	sNodes,
	// order by
	oNodes, 
	// group by
	gNodes;
	
	Node[] distinct, group;
	
	Mappings lMap;
	Hashtable<String, Node> table;
	
	boolean read = false;
	
	Mapping(){
		this.qEdges = new Edge[0];
		this.edges = qEdges;
		this.qNodes = new Node[0];
		this.nodes = qNodes;
	}
	
	Mapping(Edge[] query, Edge[] result, Node[] qnodes, Node[] nodes){
		this.qEdges = query;
		this.edges = result;
		this.qNodes = qnodes;
		this.nodes = nodes;
	}
	
	Mapping(Node[] qnodes, Node[] nodes){
		this.qEdges = new Edge[0];
		this.edges = qEdges;
		init(qnodes, nodes);
	}
	
	public Mapping(List<Node> q, List<Node> t){
		this();
		init(q, t);
	}
	
	public static Mapping create(List<Node> q, List<Node> t){
		return new Mapping(q, t);
	}

	
	public static Mapping create(){
		return new Mapping();
	}
	
	public static Mapping create(Node[] qnodes, Node[] nodes){
		return new Mapping(qnodes, nodes);
	}
	
	public static Mapping create(Node qnode, Node node){
		Node[] qnodes = new Node[1], 
		nodes = new Node[1];
		qnodes[0] = qnode;
		nodes[0] = node;
		return new Mapping(qnodes, nodes);
	}
	
	public static Mapping create(Edge query, Edge result){
		ArrayList<Node> 
		qNodes = new ArrayList<Node>(), 
		tNodes= new ArrayList<Node>();
		for (int i=0; i<query.nbNode(); i++){
			Node node = query.getNode(i);
			if (node.isVariable()){
				qNodes.add(node);
				tNodes.add(result.getNode(i));
			}
		}
		if (query.getEdgeVariable()!=null){
			qNodes.add(query.getEdgeVariable());
			tNodes.add(result.getEdgeNode());
		}
		return new Mapping(qNodes, tNodes);
	}

	
	public static Mapping create(Edge[] query, Edge[] result, 
			Node[] qnodes, Node[] nodes){
		return new Mapping(query, result, qnodes, nodes);
	}

	
	void init(List<Node> q, List<Node> t){
		Node[] qn = new Node[q.size()];
		Node[] tn = new Node[t.size()];
		qn = q.toArray(qn);
		tn = t.toArray(tn);
		init(qn, tn);
	}
	
	
	void init(List<Path> lp){
		lPath = new Path[lp.size()]; 
		lPath = lp.toArray(lPath);
	}

	void init(Node[] qnodes, Node[] nodes){
		this.qNodes = qnodes;
		this.nodes = nodes;
	}
	
	public void bind(Node qNode, Node tNode){
		Node[] qq = new Node[qNodes.length+1];
		Node[] tt = new Node[nodes.length+1];
		int i = 0;
		for (Node q : qNodes){
			qq[i] = q;
			tt[i] = nodes[i];
			i++;
		}
		qq[i] = qNode;
		tt[i] = tNode;
		qNodes = qq;
		nodes = tt;
	}
	
	/**
	 * Project on select variables of query 
	 * Modify this Mapping
	 */
	public void project(Query q){
		ArrayList<Node> lqNodes = new ArrayList<Node>();
		ArrayList<Node> ltNodes = new ArrayList<Node>();
		ArrayList<Path> paths   = new ArrayList<Path>();

		for (Node qNode : q.getSelect()){
			Node tNode = getNode(qNode);
			if (tNode != null){
				lqNodes.add(qNode);
				ltNodes.add(tNode);
				if (isPath()) {
					paths.add(getPath(qNode));
				}
			}
		}
		
		init(lqNodes, ltNodes);
		
		if (isPath()){
			init(paths);
		}
	}
	
	
	public void setRead(boolean b){
		read = b;
	}
	
	public boolean isRead(){
		return read;
	}
	
	void setOrderBy(Node[] nodes){
		oNodes = nodes;
	}
	
	void setGroupBy(Node[] nodes){
		gNodes = nodes;
	}
	
	public Mappings getMappings(){
		return lMap;
	}
	
	void setMappings(Mappings l){
		lMap = l;
	}
	
	Node[] getOrderBy(){
		return oNodes;
	}
	
	Node[] getGroupBy(){
		return gNodes;
	}
	
	public void setSelect(Node[] nodes){
		sNodes = nodes;
	}
	
	public Node[] getSelect(){
		return sNodes;
	}
	
	public void setPath(Node qNode, Path path){
		setPath(getIndex(qNode), path);
	}
	
	
	public void rename(Node oName, Node nName){
		int i = 0;
		for (Node qn : qNodes){
			if (qn!=null && qn.getLabel().equals(oName.getLabel())){
				qNodes[i] = nName;
				return;
			}
			i++;
		}
	}
	
	
	public Path getPath(Node qNode){
		return getPath(getIndex(qNode));
	}
	
	public Path getPath(String name){
		Node qNode = getQueryNode(name);
		if (qNode == null) return null;
		return getPath(getIndex(qNode));
	}
	
	/**
	 * Index of qNode in mapping (not in stack)
	 */
	int getIndex(Node qNode){
		int i = 0;
		for (Node node : qNodes){
			if (qNode == node){
				return i;
			}
			i++;
		}
		return i;
	}
	
	
	public int pathLength(Node qNode){
		if (lPath == null){
			return -1;
		}
		return getPath(qNode).length();
	}

	public int pathWeight(Node qNode){
		if (lPath == null){
			return -1;
		}
		return getPath(qNode).weight();
	}
	
	boolean isPath(){
		return lPath != null;
	}
	
	boolean isPath(int n){
		return lPath != null && lPath[n] != null;
	}
	
	Path getPath(int n){
		if (lPath == null) return null;
		return lPath[n];
	}
	
	public Node getQueryPathNode(){
		if (! isPath()) return null;
		
		int n = 0;
		for (Path pp : lPath){
			if (pp != null){
				return qNodes[n];
			}
			n++;
		}
		return null;
	}
	
	boolean isPath(Node qNode){
		return isPath(getIndex(qNode));
	}
	
	void setPath(int n, Path p){
		if (lPath == null) lPath = new Path[qNodes.length];
		lPath[n] = p;
	}
	
	public void setPath(Path[] lp){
		lPath = lp;
	}
		
	public String toString(){
		String str = "";
		for (Edge e : edges){
			str += e + "\n";
		}
		int i = 0;
		for (Node e : nodes){
			str += qNodes[i] + " " + e + "\n";
			if (isPath(qNodes[i])){
				str += qNodes[i] + " " + lPath[i] + "\n";

			}
			i++;
		}
		
		return str;
	}
	
	public List<Node> getNodes(Node var){
		return getNodes(var.getLabel());
	}
	
	public List<Node> getNodes(String var){
		return getNodes(var, false);
	}
	
	public List<Node> getNodes(String var, boolean distinct){
		List<Node> list = new ArrayList<Node>();
		if (getMappings()!=null){
			for (Mapping map: getMappings()){
				Node n = map.getNode(var);
				if (n != null){
					if (distinct && list.contains(n)){}
					else list.add(n);
				}
			}
		}
		return list;
	}
	
	
	public Node getNode(Node node){
		int n = 0;
		for (Node qnode : qNodes){
			if (node.same(qnode)){
				return nodes[n];
			}
			n++;
		}
		return null;
	}
	
	void init(){
	}
	
	/**
	 * min(?l, groupBy(?x, ?y))
	 * store value of ?x ?y in an array 
	 */
	void setGroup(List<Node> list){
		group = new Node[list.size()];
		set(list, group);
	}
	
	void setDistinct(List<Node> list){
		distinct = new Node[list.size()];
		set(list, distinct);
	}
	
	void set(List<Node> list, Node[] array){
		int i = 0;
		for (Node qNode : list){
			Node node = getNode(qNode);
			array[i++] = node;
		}
	}
	
	/**
	 * min(?l, groupBy(?x, ?y))
	 * retrieve value of ?x ?y in an array 
	 */
	Node getGroupNode(int n){
		return group[n];
	}
	
	Node[] getGroupNodes(){
		return group;
	}
	
	Node getDistinctNode(int n){
		return distinct[n];
	}
	
	public Node getTNode(Node node){
		return getNode(node);
	}
	
	public Node getGroupBy(int n){
		return gNodes[n];
	}
	
	public Node getGroupBy(Node qNode, int n){
		if (gNodes.length == 0){
			return getNode(qNode);
		}
		return gNodes[n];
	}
	
	public void setNode(Node qNode, Node node){
		int n = 0;
		for (Node qrNode : qNodes){
			if (qNode.same(qrNode)){
				 nodes[n] = node;
				 return;
			}
			n++;
		}
		addNode(qNode, node);
	}
	
	/**
	 * rename query nodes
	 * Used by Producer.map() to return Mappings 
	 */
	public void setNodes(List<Node> lNodes){
		int n = 0;
		for (Node qNode : lNodes){
			if (n<qNodes.length){
				qNodes[n++] = qNode;
			}
		}
	}
	
	public void addNode(Node qNode, Node node){
		Node[] q = new Node[qNodes.length+1];
		Node[] t = new Node[nodes.length+1];
		System.arraycopy(qNodes, 0, q, 0, qNodes.length);
		System.arraycopy(nodes, 0, t, 0, nodes.length);
		q[q.length-1] = qNode;
		t[t.length-1] = node;
		qNodes = q;
		nodes = t;
	}
	
	public void setOrderBy(int n, Node node){
		oNodes[n] = node;
	}
	
	public void setGroupBy(int n, Node node){
		gNodes[n] = node;
	}
	
	public Node getNode(int n){
		return nodes[n];
	}
	
	public Node getQueryNode(int n){
		return qNodes[n];
	}
	
	public Object getValue(String name){
		Node n = getNode(name);
		if (n == null) return null;
		return n.getValue();
	}
	
	public Object getValue(Node qn){
		Node n = getNode(qn);
		if (n == null) return null;
		return n.getValue();
	}
	
	public Node getNode(String label){
		int n = 0;
		for (Node qnode : qNodes){
			if (qnode.getLabel().equals(label)){
				return nodes[n];
			}
			n++;
		}
		return null;
	}
	
	public Node[] getQueryNodes(){
		return qNodes;
	}
	
	public Node[] getNodes(){
		return nodes;
	}
	
	public Edge[] getQueryEdges(){
		return qEdges;
	}
	
	public Edge[] getEdges(){
		return edges;
	}
	
	Edge getEdge(int n){
		return edges[n];
	}
	
	Edge getQueryEdge(int n){
		return qEdges[n];
	}
	
	
	/**
	 * Compatible imply remove minus
	 * if all shared variables have same value return true
	 * if no shared variable return false
	 */
	boolean compatible(Mapping minus){
		return compatible(minus, false);
	}

	

	boolean compatible(Mapping minus, boolean defValue){
		boolean sameVarValue = defValue;
		for (Node qNode : minus.getSelectQueryNodes()){
			if (qNode.isVariable()){
				Node qqNode = getSelectQueryNode(qNode.getLabel());
				if (qqNode != null){
					Node n1 = getNode(qqNode);
					Node n2 = minus.getNode(qNode);
					if (n1 == null || n2 == null){
						// do nothing as if variable were not in Mapping
						// use case: select count(*) as ?c
						// ?c is in QueryNodes but has no value
						// use case: minus {option{}}
					}
					else if (! n1.same(n2)){
						return false;
					}
					else {
						sameVarValue = true;
					}
				}
			}
		}
		return sameVarValue;
	}
		

/**
 * Environment
 */
	/**
	 *  Warning: do not cache this index
	 *  because index may vary between mappings 
	 */
	int getIndex(String label) {
		// TODO Auto-generated method stub
		int n = 0;	
		for (Node qNode : qNodes){
			if (qNode.getLabel().equals(label)){
				return n;
			}
			n++;
		}
		return -1;
	}
	
	public Node getNode(Expr var){
		int i = getIndex(var.getLabel());
		if (i == -1) return null;
		return nodes[i];
	}

	@Override
	public Node getQueryNode(String label) {
		// TODO Auto-generated method stub
		for (Node qNode : qNodes){
			if (qNode.getLabel().equals(label)){
				return qNode;
			}
		}		
		return null;
	}
	
	public Node getSelectNode(String label) {
		if (sNodes == null) return null;
		for (Node qNode : sNodes){
			if (qNode.getLabel().equals(label)){
				return qNode;
			}
		}		
		return null;
	}
	
	public Node getSelectQueryNode(String label) {
		if (getSelect() != null)
			return getSelectNode(label);
		else return getQueryNode(label);
	}

	@Override
	public boolean isBound(Node qNode) {
		// TODO Auto-generated method stub
		int n = getIndex(qNode.getLabel());
		return n != -1 && nodes[n] != null;
	}

//	public boolean isSafeIndex() {
//		// TODO Auto-generated method stub
//		return false;
//	}
	
	
	
	/*********************************************************************
	 * 
	 * Pipeline Solutions implementation
	 * 
	 * 
	 *********************************************************************/
	
	Node[] getSelectQueryNodes(){
		if (getSelect() != null) return getSelect();
		else return getQueryNodes();
	}
	
	Mapping join(Mapping m){
		List<Node> qNodes = new ArrayList<Node>();
		List<Node> tNodes = new ArrayList<Node>();

		for (Node q1 : getSelectQueryNodes()){
			Node n1 = getNode(q1);
			Node q2 = m.getSelectQueryNode(q1.getLabel());
			if (q2 != null){
				Node n2 = m.getNode(q2);
				if (! same(n1, n2)){
					return null;
				}
			}
			qNodes.add(q1);
			tNodes.add(n1);
		}
		
		// nodes in m not in this
		for (Node q2 : m.getSelectQueryNodes()){
			Node q1 = getSelectQueryNode(q2.getLabel());
			if (q1 == null){
				Node n2 = m.getNode(q2);
				qNodes.add(q2);
				tNodes.add(n2);
			}
		}
		
		Mapping map = new Mapping(qNodes, tNodes);
		return map;
	}
	
	Mapping project(List<Exp> lExp){
		
		return this;
	}
	
	Mapping rename(List<Exp> lExp){
		if (getSelect() != null) rename(lExp, getSelect());
		rename(lExp, getQueryNodes());
		return this;
	}
	
	Node[] rename(List<Exp> lExp, Node[] qNodes){
		int i = 0;
		for (Node node : qNodes){
			Node tNode = get(lExp, node);
			if (tNode != null){
				qNodes[i] = tNode;
			}
			i++;
		}
		return qNodes;
	}
	
	Node get(List<Exp> lExp, Node node){
		for (Exp exp : lExp){
			Filter f = exp.getFilter();
			if (f != null && 
				f.getExp().type() == ExprType.VARIABLE &&
				f.getExp().getLabel().equals(node.getLabel())	){
				return exp.getNode();
			}
		}
		return null;
	}
	
	boolean same(Node n1, Node n2){
		return n1.same(n2);
	}
	
	
	/**
	 * Share one target node (independently of query node)
	 */
	boolean match(Mapping map){
		int i = 0;
		for (Node node : getNodes()){
			// skip path that cannot be shared
			if (! isPath(i++) && node != null && map.contains(node)){
				return true;
			}
		}
		return false;
	}
	
	boolean contains(Node node){
		for (Node n : getNodes()){
			if (n != null && node.same(n)){
				return true;
			}
		}
		return false;
	}
	
	
}
