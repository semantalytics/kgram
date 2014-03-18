package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.triple.api.ASTVisitor;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The root class of the statements of the query language: 
 * And, BasicGraphPattern, Score, Source, Option, Or, Triple
 * <br>
 * @author Olivier Corby
 */

public abstract class Exp extends Statement {
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Exp.class); 
	private static String SUBSTATEOF = RDFS.COSSUBSTATEOF ;
	private static final String LEAF = "leaf_";
	
	private Vector<Exp> body;
	
	public Exp() {
		body = new Vector<Exp>();
	}
	
	public  boolean add(Exp exp){
		if (exp.isBinary() && exp.size() == 1){
			BasicGraphPattern bgp = BasicGraphPattern.create();
			for (Exp e : body){
				bgp.add(e);
			}
			exp.add(0, bgp);
			body.clear();
			return add(exp);
		}
		return body.add(exp);
	}
	
	boolean isBinary(){
		return isMinus() || isOptional();
	}
	
	public  void add(int n, Exp exp){
		 body.add(n, exp);
	}
	
	public void addAll(Exp exp){
		body.addAll(exp.getBody());
	}
	
	public Vector<Exp> getBody(){
		return body;
	}
	
	public ASTQuery getQuery(){
		return null;
	}
	
	public Exp remove(int n){
		return body.remove(n);
	}
	
	public Exp get(int n){
		return body.get(n);
	}
	
	public void set(int n, Exp exp){
		body.set(n, exp);
	}
	
	public Triple getTriple(){
		return null;
	}
	
	public Expression getFilter(){
		return null;
	}
	
	public void setAST(ASTQuery ast){
		
	}
	
	public ASTQuery getAST(){
		return null;
	}
	
	public int size(){
		return body.size();
	}
	
	public boolean validateBlank(ASTQuery ast){
		return true;
	}
	

	
	Bind validate(Bind env, int n) throws QuerySemanticException {
		   return env;
	   }

	
	public void append(Exp e){
		add(e);
	}
	
	public void append(Expression e){
		add(Triple.create(e));
	}
	

	boolean isRegexp(String uri){
		return uri.indexOf(".*")!=-1;
	}
	
	void process(ASTQuery aq){
		aq.setQuery(this);
	}
	
	
	
	Exp copy(){
		return this;
	}
	
	void setScore(Vector<String> names){
		Exp exp;
		for (int i=0;  i<size(); i++){
			exp = eget(i);
			exp.setScore(names);
		}
	}
	

	
	void setNegation(boolean b) {
	}
	
	void setCard(String card){
	}
	
	public void setRec(boolean b){
	  }
	
	
	public StringBuffer toString(StringBuffer sb) {
		if (size() == 0) return sb;

		get(0).toString(sb);

		if (size() > 1) {
			for (int i=1;i<size();i++) {
				sb.append(ASTQuery.NL);
				get(i).toString(sb);
			}
		}
		return sb;
	}
	
	
	public Exp eget(int i){
		if (this.size() > i) return (Exp)get(i);
		else return null;
	}
	
	/**
	 * If the triples are all filter
	 * @return
	 */
	boolean isExp(){
		for (int i=0; i<size(); i++){
			if (! eget(i).isExp()) return false;
		}
		return true;
	}

	public boolean isTriple(){
		return false;
	}
	
	public boolean isRelation(){
		return false;
	}
	
	public boolean isFilter(){
		return false;
	}
	
	public boolean isOption(){
		return false;
	}
        
        public boolean isOptional(){
		return false;
	}
				
	public boolean isAnd(){
		return false;
	}
	
	public boolean isValues(){
		return false;
	}
	
	public boolean isBGP(){
		return false;
	}
	
	public boolean isRDFList(){
		return false;
	}
	
	public boolean isUnion(){
		return false;
	}
	
	public boolean isJoin(){
		return false;
	}
	
	public boolean isMinus(){
		return false;
	}
	
	public boolean isGraph(){
		return false;
	}
	
	public boolean isService(){
		return false;
	}
	
	public boolean isScore(){
		  return false;
	  }
	
	public boolean isQuery(){
		return false;
	}
	
	public boolean isBind(){
		return false;
	}
        
        public boolean isScope(){
		return false;
	}
	
	public boolean isNegation(){
		return false;
	}
	
	public boolean isForall(){
		return false;
	}
	
	public boolean isIfThenElse(){
		return false;
	}
	
	public boolean isExist(){
		return false;
	}
	
	/**
	 * This Exp is an option pattern : option (t1 t2 t3)
	 * tag t1 as first option triple and t3 as last
	 * projection will generate index for these first and last triples for
	 * appropriate backtracking
	 */
	void setOption(boolean b){
		Exp exp;
		for (int i=0;  i<size(); i++){
			exp = eget(i);
			exp.setOption(b);
		}
	}
		
	public void setFirst(boolean b){
		if (size() > 0)
			eget(0).setFirst(b);
	}
	
	public void setLast(boolean b){
		if (size() > 0)
			eget(size() - 1).setLast(b);
	}

	
	/**
	 * validate an AST
	 * - collect var for select *
	 * - check bind(EXP, VAR) : var is not in scope
	 */
	boolean validate(ASTQuery ast){
		return validate(ast, false);
	}
	
	/**
	 * exist = true means we are in a exists {} or in minus {}
	 * In this case, do not collect var for select *
	 */
	boolean validate(ASTQuery ast, boolean exist){
		return true;
	}
	
	public boolean validateData(ASTQuery ast){
		for (Exp exp : getBody()){
			if (! exp.validateData(ast)){
				return false;
			}
		}
		return true;
	}

	public boolean validateDelete(){
		for (Exp exp : getBody()){
			if (! exp.validateDelete()){
				return false;
			}
		}
		return true;
	}
        
        
        @Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
        
	
}