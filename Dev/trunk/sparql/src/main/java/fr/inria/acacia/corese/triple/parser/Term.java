package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.edelweiss.kgram.api.core.ExpPattern;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Term extends Expression {
	static final String RE_CHECK = "check";
	static final String RE_PARA = "||";
	public static final String RE_ALT = "|";
	public static final String RE_SEQ = "/";
	static final String OCST = "@{";
	static final String CCST = "}";
	static final String SPACE = " ";

	static final String STNOT = Keyword.STNOT;
	static final String SENOT = Keyword.SENOT;
	
	public static final String SEINV = "i";
	public static final String SREV = Keyword.SBE;
	public static final String SNOT = Keyword.SENOT;
	public static final String SEAND = Keyword.SEAND;
	static final String SEDIV = Keyword.SDIV;
	public static final String SEOR  = Keyword.SEOR;
	public static final String SEQ   = Keyword.SEQ;
	public static final String SNEQ   = Keyword.SNEQ;
	public static final String STAR = "star";
	public static final String TEST = "test";

	static final String OPT = "opt";
	static final String PLUS = "plus";
	static final String XPATH = "xpath";
	static final String DIFFER  = "differ";
	static final String ISDIFFER  = "isDifferent";
	static final String SIM = "similarity";
	static final String SCORE = "score";
	static final String SBOUND = "bound";
	static final String EXIST = "exists";
	static final String LIST = "list";
	static final String SERVICE = "service";


	Processor proc;
	Exp exist;
	Constant cname;
	
	ArrayList<Expression> args=new ArrayList<Expression>();
	// additional system arg:
	Expr exp;
	boolean isFunction = false,
	isCount = false,
	isPlus = false;
	boolean isSystem = false;
	boolean isDistinct = false;
	boolean isShort = false;
	String  modality;
	
	public Term() {
	}
	
	public Term(String name){
		setName(name);
	}
	
	public Term(String name, Expression exp1){
		setName(name);
		args.add(exp1);
	}
	
	public Term(String name, Expression exp1, Expression exp2){
		setName(name);
		args.add(exp1);
		args.add(exp2);
	}
	
	public static Term create(String name, Expression exp1, Expression exp2){
		return new Term(name, exp1, exp2);
	}
	
	public static Term create(String name, Expression exp1){
		return new Term(name, exp1);
	}
	
	public static Term create(String name){
		return new Term(name);
	}
	
	public static Term function(String name){
		Term fun = new Term(name); 
		fun.isFunction = true;
		return fun;
	}
	
	public static Term list(){
		return Term.function(LIST);
	}
	
	public static Term function(String name, Expression exp){
		Term t = function(name);
		t.args.add(exp);
		return t;
	}
	
	public static Term function(String name, Expression exp1, Expression exp2){
		Term t = function(name, exp1);
		t.args.add(exp2);
		return t;
	}
	
	public static Term function(String name, Expression exp1, Expression exp2, Expression exp3){
		Term t = function(name, exp1, exp2);
		t.args.add(exp3);
		return t;
	}
	
	public String getLabel() {
		if (getLongName()!=null) return getLongName();
		return name;
	}
	
	public void setCName(Constant c){
		cname = c;
	}	
	
	public Constant getCName(){
		return cname;
	}

	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
	public boolean isDistinct(){
		return isDistinct;
	}
	
	public void setShort(boolean b){
		isShort = b;
	}
	
	public boolean isShort(){
		return isShort;
	}
	
	public void setModality(String s){
		modality = s;
	}
	
	public String getModality(){
		return modality;
	}

	public static Term negation(Expression exp){
		return new Term(SENOT, exp);
	}
	
	public boolean isTerm(){
		return true;
	}
	
	public void setName(String name){
		super.setName(name);
	}
	
	public String toRegex() {
		if (isCount()){
			String str = paren(getArg(0).toRegex()) + "{";
			if (getMin() == getMax()){
				str += getMin();
			}
			else {
				//if (getMin()!=0){
					str += getMin();
				//}
				str += ",";
				if (getMax()!= Integer.MAX_VALUE){
					str += getMax();
				}
			}
			str += "}";
			return str;
		}
		else if (isPlus()){
			return paren(getArg(0).toRegex()) + "+";
		}
		else if (isStar()){
			return paren(getArg(0).toRegex()) + "*";
		}
		else if (isNot()){
			return SNOT + paren(getArg(0).toRegex());
		}
		else if (isReverse()){
			return SREV + paren(getArg(0).toRegex());
		}
		else if (isOpt()){
			return paren(getArg(0).toRegex()) + "?";
		}
		else if (isSeq()){
			if (getArg(1).isTest()){
				return toTest();
			}
			else {
				return getArg(0).toRegex() + RE_SEQ + getArg(1).toRegex();
			}
		}
		else if (isAlt()){
			return "(" + getArg(0).toRegex() + RE_ALT + getArg(1).toRegex() +")";
		}
		else if (isPara()){
			return getArg(0).toRegex() + RE_PARA + getArg(1).toRegex();
		}
		return toString();
	}
	
	String toTest(){
		return getArg(0).toRegex() + OCST + KeywordPP.FILTER + SPACE + getArg(1).getExpr() + CCST;
	}

	String paren(String s){
		return "(" + s + ")";
	}
	
	public StringBuffer toString(StringBuffer sb) {

		if (getName() == null) {
			return sb;
		} 
		
		if (getName().equals(EXIST)){
			return getExist().toString(sb);
		}
		boolean isope = true;
		int n = args.size();

		if (isNegation(getName())){
			sb.append(KeywordPP.OPEN_PAREN + SENOT);
			n = 1;
		} 
		else if (isFunction()){
			if (! getName().equals(LIST)){
				if (getCName() != null){
					getCName().toString(sb);
				}
				else {
					sb.append(getName());
				}
			}
			isope = false;
		}

		sb.append(KeywordPP.OPEN_PAREN);
		
		if (isDistinct()){
			// count(distinct ?x)
			sb.append(KeywordPP.DISTINCT);
			sb.append(SPACE);
		}

		for (int i=0; i < n; i++){

			getArg(i).toString(sb);

			if (i < n - 1) {
				if (isope) {						
					sb.append(SPACE + getName() + SPACE);						
				}
				else {
					sb.append(KeywordPP.COMMA);
					sb.append(SPACE);
				}
			}
		}

		if (getModality() != null && getName().equalsIgnoreCase(Processor.GROUPCONCAT)){
			sb.append(Processor.SEPARATOR);
			Constant.toString(getModality(), sb);
		}
		else if (n == 0 && getName().equalsIgnoreCase(Processor.COUNT)) {
			// count(*)
			sb.append(KeywordPP.STAR);
		}
		
		sb.append(KeywordPP.CLOSE_PAREN);

		if (isNegation(getName())) {
			sb.append(KeywordPP.CLOSE_PAREN);
		}
		
		return sb;
	}


	
	
	static boolean isNegation(String name) {
		return (name.equals(STNOT) || name.equals(SENOT));
	}
	
	
	public Variable getVariable(){
		Variable var;
		for (int i = 0; i < args.size(); i++) {
			var = getArg(i).getVariable();
			if (var != null) return var;
		}
		return null;
	}
	
	
	Bind validate(Bind env){
		for (Expression exp : getArgs()){
			exp.validate(env);
		}
		return env;
	}
	
	public boolean validate(ASTQuery ast) {
		
		if (isExist()){
			return getExist().validate(ast);
		}
		
		boolean ok = true;
		
		for (Expression exp : getArgs()){
			boolean b = exp.validate(ast);
			ok = ok && b;
		}
		return ok;
	}
	
	public boolean isExist(){
		return getExist() != null;
	}
        
        public boolean isRecExist(){
		if (isExist()){
                    return true;
                }
                for (Expression exp : getArgs()){
                    if (exp.isRecExist()){
                        return true;
                    }
                }
                return false;
	}
	
	public boolean isSeq(){
		return getName().equals(RE_SEQ);
	}
	
	public boolean isAnd(){
		return getName().equals(SEAND);
	}
	
	public boolean isOr(){
		return getName().equals(SEOR);
	}
	
	public boolean isAlt(){
		return getName().equals(RE_ALT);
	}
	
	public boolean isPara(){
		return getName().equals(RE_PARA);
	}
	
	public boolean isNot(){
		return getName().equals(SENOT);
	}
	
	public boolean isPathExp(){
		return getretype() != UNDEF;
	}
	
	public boolean isInverse(){
		return getName().equals(SEINV) || super.isInverse() ;
	}
	
	public boolean isReverse(){
		return getName().equals(SREV) || super.isReverse();
	}
	
	public boolean isStar(){
		return isFunction(STAR);
	}
	
	public boolean isOpt(){
		return isFunction(OPT);
	}
	
	public boolean isTest(){
		return isFunction(TEST);
	}
	
	public boolean isCheck(){
		return isFunction(RE_CHECK);
	}
	
	// final state in regexp
	public boolean isFinal(){
		if (isStar() || isOpt()) return true;
		if (isAnd() || isAlt()){
			for (Expression exp : args){
				if (! exp.isFinal()) return false;
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Return a copy of  the reverse regex :
	 * p/q 
	 * ->
	 * q/p
	 * 
	 * use case: ?x exp <a>
	 * walk the exp from <a> to ?x 
	 * and set index = 1
	 * 
	 */
	public Expression reverse(){
		Term term = this;
		if (isSeq()){
			term = Term.create(RE_SEQ, getArg(1).reverse(), getArg(0).reverse());
		}
		else if (isAlt() || isFunction()){
			if (isAlt()){
				term = Term.create(RE_ALT);
			}
			else {
				term = Term.function(getName());
			}
			for (Expression arg : getArgs()){
				term.add(arg.reverse());
			}
			
			term.copy(this);
		}
		return term;
	}
	
	void copy(Term t){
		setMax(t.getMax());
		setMin(t.getMin());
		setPlus(t.isPlus());
		setExpr(t.getExpr());
		setShort(t.isShort());
		setDistinct(t.isDistinct());
	}
	
	
	
	/**
	 * ^(p/q) -> ^q/^p
	 * 
	 *  and translate()
	 *  
	 *  inside reverse, properties (and ! prop)  are setReverse(true)
	 */
	public Expression transform(boolean isReverse){
		Term term = this;
		Expression exp;
		boolean trace = !true;		
		
		if (isNotOrReverse()){
			exp = translate();
			exp = exp.transform(isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}

		if (isReverse()){
			// Constant redefine transform()
			exp = getArg(0).transform(! isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}
		else if (isReverse && isSeq() && ! getArg(1).isTest()){
			term = Term.create(getName(), getArg(1).transform(isReverse), 
					getArg(0).transform(isReverse));
		}
		else { 
			if (isFunction()){
				term = Term.function(getName());
			}
			else {
				term = Term.create(getName());
			}
			
			for (Expression arg : getArgs()){
				term.add(arg.transform(isReverse));
			}
			
			switch (getretype()){
			
				case NOT: 
					term.setReverse(isReverse); 
					break;
					
				case PARA: 
				case OPTION:
					// additional argument for checking
					Term t = Term.function(RE_CHECK, term);
					t.setretype(CHECK);
					term.add(t);
					break;
					
					
			}
			
			term.copy(this);
		}
		term.setretype(term.getretype());
		return term;
	}
	

	/**
	 * this term is one of:
	 * ! (^ p) -> ^ !(p)
	 * ! (p | ^q) -> (!p) | ^ (!q)
	 */
	public Expression translate(){
		Expression exp = getArg(0);
		
		if (exp.isReverse()){
			Expression e1 = Term.negation(exp.getArg(0));
			Expression e2 = Term.function(SREV, e1);
			return e2;
		}
		
		if (exp.isAlt()){
			Expression std = null, rev = null;
			for (int i=0; i<exp.getArity(); i++){
				Expression ee = exp.getArg(i);
				if (ee.isReverse()){
					rev = add(RE_ALT, rev, Term.negation(ee.getArg(0)));
				}
				else {
					std = add(RE_ALT, std, Term.negation(ee));
				}
			}
			Expression res = null;
			if (std != null){
				res = std;
			}
			if (rev != null){
				res = add(RE_ALT, res, Term.function(SREV, rev));
			}
			return res;
		}
		
		return this;
	}
	
	
	/**
	 * ! (p1 | ^p2)
	 */
	public boolean isNotOrReverse(){
		if (! isNot()) return false;
		Expression ee = getArg(0);
		if (ee.isReverse()) return true;
		if (ee.isAlt()){
			for (int i = 0; i<ee.getArity(); i++){
				if (ee.getArg(i).isReverse()){
					return true;
				}
			}
		}
		return false;
	}
	
	
	Expression add (String ope, Expression e1, Expression e2){
		if (e1 == null){
			return e2;
		}
		else {
			return Term.create(ope, e1, e2);
		}
	}
	
	/**
	 * Length of shortest path that matches the regexp
	 */
	public int regLength(){
		if (isStar())
			return 0; //getArg(0).length();
		else return length();
	}
	
	public int length(){
		if (isSeq()){
			return getArg(0).length() + getArg(1).length();
		}
		else if (isAlt()){
			return Math.min(getArg(0).length(), getArg(1).length());
		}
		else if (isNot()){
			return 1;
		}
		else return 0;
	}
	
	
	
	public boolean isPlus(){
		return isPlus;
	}
	
	void setPlus(boolean b){
		isPlus = b;
	}
	
	public boolean isCount(){
		return isCount;
	}
	
	void setCount(boolean b){
		isCount = b;
	}
	
	public boolean isTerm(String oper){
		return name.equals(oper);
	}
	
	public boolean isFunction(){
		return isFunction;
	}
	
	public boolean isFunction(String str){
		return isFunction &&  getName().equals(str);
	}
	
	public boolean isType(ASTQuery ast, int type) {
		return isType(ast, null, type);
	}
	
	/**
	 * 1. Is the exp of type aggregate or bound ?
	 * 2. When var!=null: if exp contains var return false (sem checking)
	 */
	public boolean isType(ASTQuery ast, Variable var, int type) {
		if (isFunction()) {
			if (isType(getName(), type))
				return true;
		}
		else if (isOr()){
			// if type is BOUND : return true
			if (isType(getName(), type))
				return true;
		}
		for (Expression arg : getArgs()) {
			if (var != null && arg == var && type == BOUND){
				// it is not bound() hence we return false
				return false;
			}
			if (arg.isType(ast, type)){
				return true;
			}
		}
		return false;
	}
	
	boolean isType(String name, int type){
		switch (type) {
		case Expression.ENDFILTER :
			return name.equalsIgnoreCase(SIM) || name.equalsIgnoreCase(SCORE);
		case Expression.POSFILTER :
			return isAggregate(name);
		case Expression.BOUND :
			// see compiler
			return name.equalsIgnoreCase(SBOUND) || name.equals(SEOR);
		}
		return false;
	}
	
	public  boolean isAggregate(String name){
		for (String n : Keyword.aggregate){
			if (n.equalsIgnoreCase(name)){ 
				return true;
			}
		}
		return false;
	}
	
	public boolean isRecAggregate(){
		if (isAggregate(name)){
			return true;
		}
		for (Expr exp : getExpList()){
			if (exp.isRecAggregate()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isAggregate(){
		return isAggregate(name);
	}
	
	public boolean isFunctional() {
		return isFunction() && 
		(name.equals(Processor.UNNEST) || 
		name.equals(Processor.SQL) || 
		name.equals(Processor.XPATH) ||
		name.equals(Processor.SPARQL) ||
		name.equals(Processor.EXTERN)) ;
	}
	
	public boolean isBound(){
		if (isFunction()) {
			return getName().equalsIgnoreCase(Processor.BOUND);   
		} 
		else for (int i = 0; i < getArity(); i++) {
			if (getArg(i).isBound())
				return true;
		}
		return false;
	}
	
	
	public Variable getOptionVar(Vector<String> stdVar) {
		for (int i = 0; i < getArity(); i++) {
			Variable var = getArg(i).getOptionVar(stdVar);
			if (var != null) return var;
		}
		return null;
	}
	
	
	public  int getArity(){
		return args.size();
	}
	
	public ArrayList<Expression> getArgs(){
		return args;
	}
	
	public void add(Expression exp) {
		args.add(exp);
	}
	
	public void setArg(int i, Expression exp){
		args.set(i, exp);
	}
	
	public Expression getArg(int n){
		if (n > args.size() - 1)
			return null;
		return args.get(n);
	}

	public String getOper(){
		return getName();
	}
	
	public void setOper(String str){
		setName(str);
	}
        
         public void setOper(int n){
            if (proc != null){
                proc.setOper(n);
            }
        }
	
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 * Exception: do not rewrite in case of aggregate:
	 * foo(?x) as ?y
	 * sum(?y) as ?z
	 */
	public Expression process(ASTQuery ast){
		if (isAggregate() || (ast.isKgram() && isFunctional())) return this;
		for (int i=0; i<args.size(); i++){
			Expression exp = args.get(i).process(ast);
			if (exp == null) return null;
			args.set(i, exp);
		}
		return this;
	}
	
	public Term differ(){
		if (args.size() >= 2){
			Term res =  diff(args, 0);
			return res;
		}
		else return this;
	}
	
	/**
	 * generate ?x != ?y ?x != ?z ?y != ?z 
	 * from (?x ?y ?z)
	 */
	public Term diff(ArrayList<Expression> vars, int start){
		Term res = null;
		for (int i=start; i<vars.size(); i++){
			for (int j=i+1; j<vars.size(); j++){
				Term tt = 	new Term(Keyword.SNEQ, getArg(i), getArg(j));
				if (res == null) res = tt;
				else res = new Term(Keyword.SEAND, res, tt);
			}
		}
		return res;
	}
	
	
	/**
	 * KGRAM
	 */
	
	// Filter
	public void getVariables(List<String> list) {
		for (Expression ee : getArgs()){
			ee.getVariables(list);
		}
		if (oper() == ExprType.EXIST){
			getPattern().getVariables(list);
		}
	}
	
	
	public Expr getExp(int i){
		return proc.getExp(i);
	}
        
        public void setExp(int i, Expr e){
             proc.setExp(i, e);
	}
        
        public void addExp(int i, Expr e){
             proc.addExp(i, e);
	}
	
	public Expr getArg(){
		return exp;
	}
	
	public void setArg(Expr e){
		exp = e;
	}
	
	public List<Expr> getExpList(){
		return proc.getExpList();
	}
	
	public ExpPattern getPattern(){
		if (proc == null) return null;
		return proc.getPattern();
	}
	
	public void setPattern(ExpPattern pat){
		proc.setPattern(pat);
	}
	
	void setExist(Exp exp){
		exist = exp;
	}
	
	public Exp getExist(){
		return exist;
	}
	
	// Exp
	
	public Expression compile(ASTQuery ast){
		if (proc != null) return this;
		
		for (Expression exp : getArgs()){
			exp.compile(ast);
		}
		
		proc = new Processor(this);
		proc.compile(ast);
		return this;
		
	}

	
	public void compile(){
		compile(null);
	}
	
	public int arity(){
		return proc.arity();
	}
	
	
	public int type(){
		return proc.type();
	}
	
	public int oper(){
		return proc.oper();
	}

	public Processor getProcessor() {
		// TODO Auto-generated method stub
		return proc;
	}
	              
        public Term copy(Variable o, Variable n) {
            Term f = null;
            if (isFunction()) {
                f = function(getName());
                f.setLongName(getLongName());
                f.setModality(getModality());
            } else {
                f = Term.create(getName());
            }
            for (Expression e : getArgs()) {
                Expression ee = e.copy(o, n);
                f.add(ee);
            }
            return f;
    }

}