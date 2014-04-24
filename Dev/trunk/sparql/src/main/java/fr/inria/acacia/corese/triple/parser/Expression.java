package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.api.ASTVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Regex;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The root class of the expressions of the query language: Atom, Variable, Constant, Term
 * <br>
 * @author Olivier Corby
 */

public class Expression extends Statement 
implements Regex, Filter, Expr {
	public static final int STDFILTER = 0;
	public static final int ENDFILTER = 1;
	public static final int POSFILTER = 2;
	public static final int BOUND = 4;
	
	static ArrayList<Expr> empty = new ArrayList<Expr>();
	int type = -1, min = -1, max = -1, retype = Regex.UNDEF;
	
	boolean isQName = false;
	boolean isEget = false;
	boolean isSystem = false;
	boolean isInverse = false, isReverse = false;
	
	String name, longName;
	Expression exp;

	public Expression(){}
	
	public Expression(String str) {
		name=str;
	}
	
	public  int getArity(){
		return -1;
	}
	
	public ArrayList<Expression> getArgs(){
		return null;
	}
	
	public Expression getArg(int i){
		return null;
	}
	
	public Expression compile(ASTQuery ast){
		return this;
	}
	
	public Expression and(Expression e2){
		if (e2 == null){
			return this;
		}
		else {
			return  Term.create(Keyword.SEAND, this, e2);
		}
	}
	
	public Expression star(){
		return Term.function(Term.STAR, this);
	}
	
	public String getName(){
		return name;
	}
	
	public String getLongName(){
		return longName;
	}
	
	public void setLongName(String name){
		longName = name;
	}
	
	public void setExpr(Expression exp){
		this.exp = exp;
	}
	
	public Expression getExpr(){
		return exp;
	}
	
	public String getKey(){
		return toString();
	}
	
	public void setName(String str){
		name=str;
	}
	
	public boolean isSystem(){
		return isSystem;
	}
	
	public void setSystem(boolean b){
		isSystem = b;
	}
	
	public boolean isArray(){
		return false;
	}
	
	boolean isAtom(){
		return false;
	}
	
	public boolean isConstant(){
		return false;
	}

	public boolean isVariable(){
		return false;
	}
	
	public boolean isSimpleVariable(){
		return false;
	}
	
	// blank as variable in sparql query
	public boolean isBlankNode(){
		return false;
	}
	
	public boolean isTerm(){
		return false;
	}
	
	public boolean isTerm(String oper){
		return false;
	}
	
	public boolean isFunction(){
		return false;
	}
	
	public boolean isFunction(String str){
		return false;
	}
        
        public boolean isRecExist(){
            return false;
        }
		
	Bind validate(Bind env){
		return env;
	}
	
	public boolean validate(ASTQuery ast) {
		return true;
	}

	
	public String getLang(){
		return null;
	}
	
	public String getDatatype(){
		return null;
	}
	
	public String getSrcDatatype() {
		return null;
	}
	
	
	public boolean isOptionVar(List<String> stdVar){
		Variable var = getOptionVar(stdVar);
		return var != null;
	}
	
	public Variable getOptionVar(List<String> stdVar){
		return null;
	}
	
	public boolean isAnd(){
		return false;
	}
	
	public boolean isSeq(){
		return false;
	}
	
	public boolean isAlt(){
		return false;
	}
	
	public boolean isOr(){
		return false;
	}
	
	public boolean isPara(){
		return false;
	}
	
	public boolean isNot(){
		return false;
	}
	
	public void setWeight(String w){
	}
	
	public int getWeight(){
		return -1;
	}
	
	public boolean isInverse(){
		return isInverse;
	}
	
	public void setInverse(boolean b){
		isInverse = b;
	}
	
	public boolean isReverse(){
		return isReverse;
	}
	
	public void setReverse(boolean b){
		isReverse = b;
	}
	
	public Expression translate(){
		return this;
	}
	
	public boolean isNotOrReverse(){
		return false;
	}
	
	void setMin(int n){
		min = n;
	}
	
	public int getMin(){
		return min;
	}
	
	void setMax(int n){
		max = n;
	}
	
	public int getMax(){
		return max;
	}
	
	// include isPlus()
	public boolean isCounter(){
		return (min!=-1 || max != -1);
	}
	
	boolean isOrVarEqCst(Variable var){
		return false;
	}
	
	void getCst(Vector<Constant> vec){}
	
	public boolean isStar(){
		return false;
	}
	
	public boolean isOpt(){
		return false;
	}
	
	public boolean isTest(){
		return false;
	}
	
	public boolean isCheck(){
		return false;
	}
	
	public boolean isFinal(){
		return false;
	}
	
	public Expression reverse(){
		return this;
	}
	
	public Expression transform(){
		return transform(false);
	}
	
	public Expression transform(boolean isReverse){
		return this;
	}
	
	public int regLength(){
		return 0;
	}
	
	public int length(){
		return 0;
	}
	
	public boolean isPlus(){
		return false;
	}
	
	public boolean  isType (ASTQuery ast, int type){
		return false;
	}
	
	public boolean  isType (ASTQuery ast, Variable var, int type){
		return false;
	}
	
	public boolean isVisited(){
		return false;
	}
	
	public void setVisited(boolean b){
	}
	
	public boolean isPath(){
		return false;
	}
	
	public boolean isBound(){
		return false;
	}
	
	public Variable getVariable(){
		return null;
	}
	
	public Constant getConstant(){
		return null;
	}
	
	public Atom getAtom(){
		return null;
	}
	
	// get:gui::?name
	public Variable getIntVariable() {
        return null;
    }
	
	
	public String toRegex() {
		String str = toString();
		if (isReverse()){
			str = Term.SREV + str;
		}
		return str;
	}
	
	/**
	 * Translate some terms like :
	 * different(?x ?y ?z) -> (?x != ?y && ?y != ?z && ?x != ?z)
	 */
	public Expression process(){
		return  this;
	}
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 */
	public Expression process(ASTQuery ast){
		return  this;
	}
	
	public Expression rewrite(){
		return this;
	}
	
	
	public boolean isQName() {
		return isQName;
	}
	
	public void setQName(boolean isQName) {
		this.isQName = isQName;
	}
	
	public boolean isEget() {
		return isEget;
	}
	
	public void setEget(boolean isEget) {
		this.isEget = isEget;
	}
	
	/*************************************************************
	 * 
	 * KGRAM Filter & Exp
	 * 
	 */
	
	public Filter getFilter(){
		return this;
	}

	
	public Expr getExp() {
		
		return this;
	}

	
	public List<String> getVariables() {
		
		List<String> list = new ArrayList<String>();
		getVariables(list);
		return list;
	}
	
	public void getVariables(List<String> list) {
	}

	
	public int arity() {
		
		return 0;
	}

	
	public String getLabel() {
		
		if (longName!=null) return longName;
		return name;
	}

	
	public Object getValue() {
		return null;
	}

	
	public boolean isAggregate() {
		
		return false;
	}
	
	public boolean isExist(){
		if (oper() == ExprType.EXIST){
			return true;
		}
		else {
			for (Expr ee : getExpList()){
				if (ee.isExist()){
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean isRecAggregate() {
		
		return false;
	}
	
	public boolean isFunctional() {
		
		return false;
	}

	
	public int oper() {
		
		return -1;
	}
        
        public void setOper(int n){
            
        }

	
	public int type() {
		return type;
	}
	
	public int retype() {
		return retype;
	}
	
	void setretype(int n){
		retype = n;
	}
	
	public int getretype() {
		if (isConstant())return Regex.LABEL;
		if (isNot())	 return Regex.NOT;
		if (isSeq())	 return Regex.SEQ;
		if (isPara())	 return Regex.PARA;
		if (isAlt())	 return Regex.ALT;
		if (isPlus())	 return Regex.PLUS;
		if (isCounter()) return Regex.COUNT;
		if (isStar()) 	 return Regex.STAR;
		if (isOpt()) 	 return Regex.OPTION;
		if (isReverse()) return Regex.REVERSE;
		if (isTest())	 return Regex.TEST;
		if (isCheck())	 return Regex.CHECK;

		return Regex.UNDEF;
	}

	
	public List<Expr> getExpList() {
		return empty;
	}
	
	public Expr getExp(int i){
		return null;
	}
        
        public void setExp(int i, Expr e){
            
        }
        
        public void addExp(int i, Expr e){
            
        }
	
	public int getIndex() {
		return -1;
	}

	
	public void setIndex(int index) {		
	}

	
	public void setArg(Expr exp) {
		
	}
	
	public Expression getArg(){
		return null;
	}
	
	public Object getPattern(){
		return null;
	}

	public void setDistinct(boolean b) {
	}
	
	public boolean isDistinct() {
		return false;
	}
	
	public void setShort(boolean b) {
	}
	
	public boolean isShort() {
		return false;
	}
	
	public String getModality() {
		
		return null;
	}
        
        
        @Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
        
        public Expression copy(Variable o, Variable n){
            return this;
        }
        
	
}