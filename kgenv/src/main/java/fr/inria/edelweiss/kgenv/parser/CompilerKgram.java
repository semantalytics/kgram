package fr.inria.edelweiss.kgenv.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.tool.Message;

/**
 * Proxy between KGRAM compiler and Corese compiler
 * Use KGRAM graph and filter
 * @author corby 
 *
 */
public class CompilerKgram implements ExpType, Compiler {
	static int count = 0;


	ASTQuery ast;
	EdgeImpl edge;
	Node node;
	//boolean test = false;
	
	Hashtable<String, Node> varTable, resTable;
	List<IDatatype> consList;

	
	public CompilerKgram(){
		varTable = new Hashtable<String, Node>();
		resTable = new Hashtable<String, Node>();
		consList = new ArrayList<IDatatype>();
	}
	
	
	public void setAST(ASTQuery ast){
		this.ast = ast;
	}
	
	
	public List<Filter> compileFilter(Triple t)  {
		Expression exp = t.getExp();
		ArrayList<Filter> list = new ArrayList<Filter>();
		compile(exp, list);
		return list;
	}
	
	void compile(Expression exp, List<Filter> list) {
		if (exp.isAnd()){
			for (Expression e : exp.getArgs()){
				compile(e, list);
			}
		}
		else {		
			Filter f = compile(exp);
			if (f!=null) list.add(f);
		}
	}

	
	/**
	 * Generate one filter
	 */
	public Filter compile(Expression exp) {
		Expression cpl = exp.process(ast);
		if (cpl == null){
			Message.log(Message.REWRITE, exp );
			Message.log(ast);
			cpl = exp;
		}
		cpl.compile(ast);
		return cpl;

	}
	
	Node getNode(Atom at){
		return getNode(at, true);
	}
	
	
	/**
	 * isReuse = true means reuse existing Node if any
	 */
	Node getNode(Atom at, boolean isReuse){
		if (at.isVariable()){
			Node node = varTable.get(at.getName());
			if (node == null){
				node = new NodeImpl(at);
				varTable.put(at.getName(), node);
			}
			return node;
		}
		else if (at.isResource() && isReuse){
			Node node = resTable.get(at.getName());
			if (node == null){
				node = new NodeImpl(at);
				resTable.put(at.getName(), node);
			}
			return node;
		}
		return new NodeImpl(at);
	}
	
	public void compile(Triple tt) {
		edge = new EdgeImpl(tt);
		Node subject = getNode(tt.getSubject());
		if (tt.getVariable()!=null){
			Node variable = getNode(tt.getVariable());
			edge.setEdgeVariable(variable);
		}
		Node predicate = getNode(tt.getProperty());
		// PRAGMA: 
		// ?x rdf:type c:Image
		// in this case we want each triple rdf:type c:Image to have its own c:Image Node
		// to accept type subsumption 
		// if it would be same Node, it would need to be bound to same value
		// cf Neurolog pb
		Node object = getNode(tt.getObject(), ! tt.isType());
		edge.add(subject);
		edge.add(object);
		edge.setEdgeNode(predicate);
	}
	

	
	public Node createNode(String name){
		return getNode(new Variable(name));
	}
	
	public Node createNode(Variable var){
		return getNode(var);
	}
	
	public Node createNode(Constant val){
		return getNode(val);
	}
	
	public List<Filter> getFilters(){ 
		return new ArrayList<Filter>();
	}
	
	public Edge getEdge(){
		return edge;
	}
	
	public Node getNode(){
		return null;
	}	
	
	public boolean isFail(){
		return false;
	}
	
	
	/*****************************
	 * 
	 * PATH 
	 * 
	 * **/
	
	public Regex getRegex(Filter f){
		Expression exp = (Expression) f;
		if (exp.isFunction(Processor.MATCH)){
			Expression regex = exp.getArg(1);
			regex.compile(ast);
			return regex;
		}
		return null;
	}
		
	public String getMode(Filter f){		
		Expression exp = (Expression) f;
		if (exp.isFunction(Processor.MATCH) && exp.getArity()==3){
			return exp.getArg(2).getLabel();
		}
		return null;
	}	

	public int getMin(Filter f){		
		Expression exp = (Expression) f;
		
		if (exp.getArity()==2 && exp.getArg(1).isConstant()){
			Constant cst = (Constant) exp.getArg(1);
			if (exp.getName().equals(">=") || exp.getName().equals("="))
				return cst.getDatatypeValue().getIntegerValue();
			else if (exp.getName().equals(">"))
				return cst.getDatatypeValue().getIntegerValue()+1;
		}
		return -1;
	}
	
	public int getMax(Filter f){
	
		Expression exp = (Expression) f;
		
		if (exp.getArity()==2 && exp.getArg(1).isConstant()){
			Constant cst = (Constant) exp.getArg(1);
			if (exp.getName().equals("<=") || exp.getName().equals("="))
				return cst.getDatatypeValue().getIntegerValue();
			else if (exp.getName().equals("<"))
				return cst.getDatatypeValue().getIntegerValue()-1;
		}
		return -1;	}
	

}
