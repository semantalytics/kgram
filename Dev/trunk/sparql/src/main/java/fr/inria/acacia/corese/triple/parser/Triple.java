package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.config.PropertyGetter.PropertyCallback;

import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * Represents a triple (resource property value)
 * <br>
 * @author Olivier Savoie
 */

public class Triple extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Triple.class);
	
	final static String SDT = KeywordPP.SDT;
	final static String LANG = KeywordPP.LANG;
	final static String PREFIX = "PREFIX";
	final static String BASE = "BASE";
	static int ccid = 0;
	// nb different system variables in a query ...
	static final int MAX = 1000;
	ASTQuery ast;
	//String tproperty;
	Atom subject, object;
	// property variable
	Variable variable;
	// property qname (or uri if written as <>)
	Constant predicate;
	// graph ?src/uri
	Atom source;
	// draft for tuple
	List<Atom> larg;
	// tuple contain a filter
	Expression exp, 
		// path regex
		regex;
	String mode;
	Vector<String> score; // score ?s { pattern }
	int id=-1; // an unique id for triple (if needed to generate variable/option)
	int star=0; // for path of length n
	int line=-1; 
	String rvar, pre;          // variable on get:gui::?x pre=?x value=get:gui
	boolean isexp=false;     // is a filter
	boolean isoption=false, isFirst=false, isLast=false, fake=false;  // is option::
	int firstOptionID = -1, lastOptionID = -1; // index of first/last triple of an option pattern
	boolean isRec = false;   // graph rec ?s {}
	boolean isall=false;     // all::p{n}
	boolean istype=false;    // rdf:type or <= rdf:type
	boolean isdirect=false;  // direct::rdf:type
	boolean isone=false;     // one:: only one occurrence of ?x ?p ?y
	boolean isset=false;     // all path of length <= n : relation{n}
	boolean trace = false;
	
	public Triple() {
		setID();
	}
	
	public Triple(int num) {
		id = num;
	}
	

	// subject pred::var object
	Triple(Atom sub, Constant pred, Variable var, Atom obj) {
		subject = sub;
		object  = obj;
		predicate = pred;
		variable = var;
		if (predicate.getName().equals(RDFS.rdftype) || predicate.getName().equals(RDFS.RDFTYPE)){
			istype = true;
		}
	}
	
	public static Triple create(Atom src, Atom sub, Constant pred, Variable var, Atom obj) {
		Triple t = new Triple(sub, pred, var, obj);
		t.source = src;
		t.setID();	
		return t;
	}
	
	public static Triple create(Atom src, Atom sub, Constant pred, Atom obj) {
		Triple t = new Triple(sub, pred, null, obj);
		t.source = src;
		t.setID();	
		return t;
	}
	
	public static Triple create(Atom sub, Constant pred, Variable var, Atom obj) {
		Triple t = new Triple(sub, pred, var, obj);
		t.setID();	
		return t;
	}
	
	public static Triple create(String subject, String property, String value){
	return create(null, subject, property, value);
	}
	
	public static Triple create(String source, 
			String subject, String property, String value){
		Constant src = null;
		if (source != null) src = new Constant(source);
		Triple t = Triple.create(src, new Constant(subject),
				new Constant(property), null, new Constant(value));
		return t;
	}
	
	// for triples
	public static Triple create(Expression subject, Atom predicate, Expression object) {
		if (! subject.isAtom() && ! object.isAtom()) return null;
		Variable var = null;
		Constant pred = null;
		if (predicate.isConstant()){
			pred = (Constant) predicate;
			var = pred.getIntVariable();
		}
		else {
			pred =  Constant.createResource(getRootPropertyQN());
			var = (Variable) predicate;
		}
		Triple t = new Triple((Atom)subject, pred, var, (Atom)object);
		t.setID();	
		t.setTriple(t.subject, predicate, t.object);
		return t;
	}
	
	
	// for filters
	public static Triple create(Expression exp) {
		Triple t = new Triple(); 
		t.exp = exp;
		t.isexp = true;
		t.setID();
		return t;
	}
	
	public static Triple createNS(Constant type, Constant prefix, Constant uri){
		return create(type, prefix, uri);
	}
	
	private void setTriple(Atom exp1, Atom atom, Atom exp2) {
		setOne(atom.isIsone());
	    setAll(atom.isIsall());
	    setDirect(atom.isIsdirect());
	    setIsset(atom.isIsset());
	    setPath(atom.getStar());
	    if (exp1.getIntVariable() != null){
	    	setRVar(exp1.getIntVariable().getName());
	    }
	    if (exp2.getIntVariable() != null){
	    	setVVar(exp2.getIntVariable().getName());
	    }
	}
	
	public Triple getTriple(){
		return this;
	}

	
	public void setID() {
		id = nextID();
	}
	
	public boolean isTriple(){
		return true;
	}
	
	synchronized static int nextID(){
		// nb max de variables systemes differentes ds une requete
		if (ccid == MAX){
			ccid = 0;
		}
		return ccid++;
	}
	
	public void setID(int num) {
		id = num;
	}
	
	public void setLine(int n){
		line = n;
	}
	
	public int getLine(){
		return line;
	}
	
	/************************************************************************
	 * 2. Semantic phase
	 * expand prefix with namespace uri
	 * expand get:gui
	 * expand path : p[2] p{2}
	 * return uri triple
	 */

	
	public void setAST(ASTQuery a){
		ast = a;
	}
	
	public ASTQuery getAST(){
		if (ast == null) ast = defaultAST();
		return ast;
	}
	
	ASTQuery defaultAST(){
		ASTQuery ast = ASTQuery.create();
		ast.setKgram(true);
		ast.setBody(new And());
		return ast;
	}
	

	
	

	/**
	 * Translate this exp triple as a Term
	 * @return
	 */
	Expression toTerm() {
		return exp;
	}
	
	boolean isString(String str) {
		if ((str.startsWith("\"") && str.endsWith("\""))
				|| (str.startsWith("'") && str.endsWith("'")))
			return true;
		else
			return false;
	}
	
	
	/***************************************************************************
	 *
	 * 2. Semantics
	 * expand prefix to uri namespace
	 * expand path and generate OR
	 */
//	Exp validate(Parser parser){
////		if (isall) {
////			parser.setAll(true);
////		}
//		if (predicate != null) istype = parser.isDefType(predicate);
//		Exp stmt=this;
//		//processIS();
//		if (isExp()){
//			//exp=defExpression(parser);
//		}
//		else {
//			stmt=defRelation(parser);
//		}
//		return stmt;
//	}
	
	
	
	/**
	 * 2. Semantics after validate
	 * Generate target expression or relation, expand path
	 * @param parser
	 * @return
	 */
//	Exp defExpression(Parser parser) {
//		return this;
//	}
	
	/**
	 * Return an  expression from this triple
	 * If property[2] generate (or (x p y) (x p z p y))
	 * isliteral = true if value between "" or has datatype or
	 * range is Literal
	 */
//	Exp defRelation(Parser parser){
//		boolean literalp= false;
//		// to accept 12 as a (datatype) literal value, need to check range
//		// i.e. 12 vs "12"
//		Exp exp;
//		if (isset){
//			if (id == -1 ) id=nextID();//parser.getTripleId();
//			exp= defSet(parser);
//		}
//		else if (star > 1){
//			exp = defPath(parser, literalp);
//		}
//		else {
//			exp=this;
//		}
//		return exp;
//	}
//	
//	Exp defPath(Parser parser, boolean literalp) {
//		if (id == -1)
//			id = nextID();//parser.getTripleId();
//		Exp filter = toVariable(); // replace cst by var
//		Exp exp = new Or(), tmp;
//		for (int i = 1; i <= star; i++) {
//			tmp = defPath(parser, i, literalp);
//			exp.add(new And(tmp));
//		}
//		if (filter != null) {
//			exp = new And(exp);
//			exp.add(filter);
//		}
//		return exp;
//	}
//	
//
//	
//	/**
//	 * From ?x p[k] ?y Generate  one path of length n   ?x p ?v1 ?v1 p ?y ...
//	 * @param n
//	 * @param literalp
//	 * @return
//	 */
//	Exp defPath(Parser parser, int n, boolean literalp){
//		And star=new And();
//		Triple triple;
//		String prev=subject.getName(), //tresource,
//		var=prev;// tresource
//		Variable vprev=new Variable(prev), vvar=new Variable(var);
//		for (int i=1; i<n; i++){
//			var=genVar(i);
//			vvar=new Variable(var);
//			triple = create(vprev, predicate, variable, vvar);
//			prev=var;
//			vprev=vvar;
//			triple.source=source;
//			triple.score=score;
//			triple.istype = istype;
//			star.add(triple);
//		}
//		triple = create(vvar, predicate, variable, object);
//		triple.source=source;
//		triple.score=score;
//		triple.istype = istype;
//		star.add(triple);
//		return star;
//	}
	
	/**
	 * ?x rdfs:label ?l@en
	 * ?x rdfs:label toto@en
	 *
	 */
//	Exp defLang(){
//		Exp exp=new And();
//		exp.add(this);
//		
//		
//		return exp;
//	}
	
	/**
	 * ?x relation{n} ?y
	 * create all paths of length 1 to n
	 * return an OR of these paths
	 */
//	Exp defSet(Parser parser) {
//		Exp set = new Or();
//		Exp filter = toVariable();
//		set = set(parser, set, getStar());
//		if (filter != null) {
//			set = new And(set);
//			set.add(filter);
//		}
//		return set;
//	}
	
	/**
	 * Replace constant by variable :
	 */
//	Exp toVariable() {
//		Triple tfilter1 = null, tfilter2 = null;
//		if (subject.isConstant()) {
//			// generate a variable and a filter variable = constant
//			tfilter1 = toVariable(true);
//		}
//		if (object.isConstant()) {
//			tfilter2 = toVariable(false);
//		}
//		if (tfilter1 != null && tfilter2 != null){
//			And and = new And(tfilter1);
//			and.add(tfilter2);
//			return and;
//		}
//		else if (tfilter1 != null)
//			return tfilter1;
//		else return tfilter2;
//	}
	
	/**
	 *
	 * Replace constant arg by variable arg
	 *    */
//	Triple toVariable(boolean first) {
//		String name = ExpParser.SYSVAR + id + ((first)?"_1":"_2");
//		Variable var = new Variable(name);
//		Term filter = new Term("=", var, (first)?subject:object);
//		toVariable(var, first);
//		Triple tfilter =   Triple.create(filter);
//		//tfilter.setID(parser.getTripleId());
//		return tfilter;
//	}
	
//	public static Triple fake(){
//		int id = nextID();
//		Triple t =  Triple.create(
//				new Variable(ExpParser.SYSVAR + "fa"+id), 
//				Constant.createResource(RDFS.RootPropertyURI), 
//				new Variable(ExpParser.SYSVAR + "fb"+id));
//		t.setFake(true);
//		//t.setID(id);
//		return t;
//	}
	

//	void toVariable(Variable var, boolean first) {
//		if (first) {
//			subject = var;
//		}
//		else {
//			object = var;
//		}
//	}
	
	
	
	/**
	 * all paths from 1 to n
	 */
//	Exp set(Parser parser, Exp set, int n){
//		for (int i=1; i<=n; i++){
//			set=set(parser, set, new And(), 0, i);
//		}
//		return set;
//	}
	
	/**
	 * create all paths of same size n
	 * generate 2^n combinaison :
	 * ?x R ?v1 ?v1 R ?y
	 * ?x R ?v1 ?y R ?v1
	 * ?v1 R ?x ?v1 R ?y
	 * ?v1 R ?x ?y R ?v1
	 * set = set of paths already created
	 * cur = current path of length i {and a1 .. ai }
	 * add ai+1 and inv(ai+1) into current path
	 * complete recursively untill n
	 * return set
	 */
//	Exp set(Parser parser, Exp set, Exp cur, int i, int n){
//		if (i==n){
//			set.add(cur);
//			return set;
//		}
//		Triple triple=copy(parser, i, n, false);
//		Exp tmp=cur.copy();
//		tmp.add(triple);
//		set=set(parser, set, tmp, i+1, n);
//		triple=copy(parser, i, n, true);
//		cur.add(triple);
//		set=set(parser, set, cur, i+1, n);
//		return set;
//	}
	

	
//	Triple copy(Parser parser, int i, int n, boolean swap){
//		String x=getVar(i, n);
//		String y=getVar(i+1, n);
//		Variable vx=new Variable(x),
//		vy=new Variable(y);
//		Triple triple;
//		if (swap){
//			triple = create(vy, predicate, variable, vx);
//	}
//		else {
//			triple = create(vx, predicate, variable, vy);
//		}
//		// when {1} do not complete both args :
//		if (i==0)   complete(triple, true, swap);
//		if (i + 1 == n)
//			complete(triple, false, swap);
//		triple.source=source;
//		triple.score=score;
//		triple.istype = istype;
//		return triple;
//	}
//	
	/**
	 * triple is a copy of this
	 * set count, join, distinct
	 * if res : copy res (else value)
	 * if swap : swap to value (else res)
	 */
//	Triple complete(Triple triple, boolean res, boolean swap){
//		return triple;
//	}
//	
	
	/**
	 * Util functions
	 * @return
	 */
	
	
	public String getResource(){
		return subject.getName();
	}

	public String getValue(){
		return object.getName();
	}
	
	public String getSourceName(){
		if (source == null) return null;
		return source.getName();
	}
	
	public Atom getSourceExp(String name){
		if (name == null) return null;
		Atom at;
		if (Triple.isVariable(name)){ 
			Variable var = new Variable(name);
			if (Variable.isBlankVariable(name)){
				var.setBlankNode(true);
			}
			at = var;
		}
		else {
			at = Constant.createResource(name);
		}
		return at;
	}
	
	public Atom getSource(){
		return source;
	}
	
	public void setScore(Vector<String> names){
		score = names;
	}
	
	public Vector<String> getScore(){
		return score;
	}
	
	/**
	 * An outermost source does not overwrite local source
	 */
	public void setSource(String src) {
		if (source == null && ! isexp){
			setVSource(src);
		}
	}
	
	public void setVSource(String src) {
		source = getSourceExp(src);
	}
	
	
	public void setVSource(Atom at) {
		source = at;
	}
	
	
	public void setRec(boolean b){
		  isRec = b;
	  }
	
	public boolean isRec(){
		return isRec;
	}
	
	
	public Atom getExp(int i){
		switch (i){
		case 0: return getSubject();
		case 1: return getObject();
		case 2: return getVariable();
		case 3: return getSource();
		default: return null;
		}
	}
	
	public Atom getArg(int i){
		switch (i){
		case 2:  return getProperty();
		default: return getExp(i);
		}
	}
	

	
	public boolean similar(Triple t2){
		for (int i=0; i<getArity(); i++){
			if (getArg(i) == null || t2.getArg(i) == null)
				if (getArg(i) != t2.getArg(i)) 
					return false;
			if (! getArg(i).getName().equals(t2.getArg(i).getName()))
				return false;
		}
		return true;
	}
	
	public void setArg(int i, Atom exp){
		setExp(i, exp);
	}
	
	public void setExp(int i, Atom exp){
		switch (i){
		case 0: setSubject(exp);break;
		case 1: setObject(exp);break;
		case 2: 
			if (exp.isVariable()){
				setVariable((Variable)exp);
			}
			else {
				setProperty((Constant)exp);
			}
			break;
		case 3: source = (Atom) exp;break;
		}
	}
	
	public int getArity(){
		return 4;
	}
	
	public List<Atom> getArgs(){
		return larg;
	}
	
	public void setArgs(List<Atom> l){
		larg = l;
	}
	
	
	public void setExp(Expression e) {
		exp = e;
	}
	
	public void setSubject(Atom e1) {
		subject = e1;
	}
	
	public void setObject(Atom e2) {
		object = e2;
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public Expression getFilter() {
		return exp;
	}
	
	
	public Expression getRegex() {
		return regex;
	}
	
	public void setRegex(Expression exp) {
		regex = exp;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String m) {
		mode = m;
	}
	
	public boolean isExpression() {
		return isexp;
	}
	
	public String getDatatype() {
		return object.getDatatype();
	}
	
	public String getLang() {
		String l = object.getLang();
		if (l == null) return "";
		else return l;
	}
	
	public int getStar() {
		return star;
	}
	
	public void setStar(int s) {
		star = s;
	}
	
	boolean isNamespace() {
		return (subject.getName().equalsIgnoreCase(PREFIX) || 
				subject.getName().equals(BASE) );
	}
	
	/**
	 * Variable for ith node in the path
	 * 0 and n are the variables of the genuine query (?x and ?y)
	 * others are generated : ?v_i
	 */
	String getVar(int i, int n) {
		if (i == 0)
			return subject.getName() ;
		else if (i == n)
			return object.getName();
		else
			return genVar(i);
	}
	
	String genVar(int i) {
		return "?v" + id + "_" + i;
	}
	
	public static boolean isVariable(String str) {
		return (str.indexOf(KeywordPP.VAR1) == 0 || 
				str.indexOf(KeywordPP.VAR2) == 0 );
	}
	
	public static boolean isQName(String str) {
		return str.toLowerCase().matches("[a-z]*:[a-z_]*");
	}
	
	public static boolean isABaseWord(String str) {
		return (!isVariable(str) && !isQName(str) && 
				!(str.startsWith(KeywordPP.OPEN) && 
				  str.endsWith(KeywordPP.CLOSE)));
	}
	

	public boolean isType() {
		return istype;
	}
	
	public void setType(boolean b) {
		istype = b;
	}
	

	
	String getPre() {
		return pre;
	}
	
	
	/**
	 *
	 * @param str
	 * @return
	 */
	String clean(String str) {
		return str;
	}
	

	
	public StringBuffer ftoSparql(Expression exp, StringBuffer sb) {
		if (exp == null) return sb;
		boolean isAtom = (exp.isAtom());
		sb.append(KeywordPP.FILTER + KeywordPP.SPACE);
		if (isAtom) sb.append("(");
		exp.toString(sb);
		if (isAtom) sb.append(")");
		sb.append(KeywordPP.SPACE);
		return sb;
	}
	
	
	public StringBuffer toString(StringBuffer sb) {
		
		if (isExpression()) {
			return ftoSparql(getExp(), sb);
		}
				
		String SPACE = " ";
		
		if (source != null){
			sb.append(source.toString() + " ");
		}
		
		String r = subject.toString();
		String p = predicate.toString();
		String v = object.toString();
	
		if (isPath()){
			p = getRegex().toRegex();
			
			if (variable != null && ! variable.getName().startsWith(ASTQuery.SYSVAR)){
				p += " :: " + variable.getName();
			}
		}
		else if (variable != null) {
			if (isTopProperty()){
				p = variable.getName();
			}
			else {
				p += "::" + variable.getName();
			}
		}
				
		if (larg != null) {
			// tuple()
			sb.append(KeywordPP.TUPLE + KeywordPP.OPEN_PAREN + p + SPACE + r + SPACE + v + SPACE);
			for (Atom e : larg) {
				sb.append(e.toString() + SPACE);
			}
			sb.append(KeywordPP.CLOSE_PAREN + KeywordPP.DOT);
		}
		else {
			sb.append(r + SPACE + p + SPACE + v + KeywordPP.DOT);
		}
		
		return sb;
	}
	

	boolean isTopProperty(){
		return 
		predicate.getLongName().equals(getRootPropertyURI()) || 
		predicate.getName().equals(getRootPropertyQN());
	}
	
	public void setExp(boolean b) {
		isexp = b;
	}
	
	/**
	 * This triple will generate a relation in the graph
	 * not an exp, not an rdf:type with a constant value
	 */
	public boolean isRelation() {
		if (istype) {
			return (object.isVariable());
		} else
			return !isexp;
	}
	
	public boolean isExp() {
		return isexp;
	}
	
	public boolean isFilter(){
		return isexp;
	}
	
	/**
	 * Does this exp refer one option variable (a var that is only referenced
	 * in an option)
	 * stdVar is the list of non optional variables (std var)
	 */
	public boolean isOptionVar(Vector<String> stdVar) {
		if (exp == null)
			return false;
		return exp.isOptionVar(stdVar);
	}
	
	Bind validate(Bind global, int n){
		Bind env = new Bind();
		if (isExp()){
			return exp.validate(env);
		}
		for (int i=0; i<getArity(); i++){
			Atom arg = getExp(i);
			if (arg != null && arg.isVariable()){
				env.bind(arg.getName());
			}
		}
		return env;
	}
	
	/**
	 * Does triple bind this variable ?
	 */
	public boolean bind(Variable var){
		if (isExp()) return false;
		String name = var.getName();
		for (int i=0; i<getArity(); i++){
			Expression arg = getExp(i);
			if (arg != null && arg.isVariable() && arg.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isOption() {
		return isoption;
	}
	
	public void setLastOptionID(int num) {
		lastOptionID = num;
	}
	
	public int getLastOptionID() {
		return lastOptionID;
	}
	
	public void setFirstOptionID(int num) {
		firstOptionID = num;
	}
	
	public int getFirstOptionID() {
		return firstOptionID;
	}
	
	public int getID() {
		return id;
	}
	
	Triple lastTriple() {
		return this;
	}
	
	public void setTOption(boolean b) {
		isoption = b;
		isFirst = b;
		isLast = b;
		firstOptionID = id;
		lastOptionID = id;
	}
	
	public void setOption(boolean b) {
		isoption = b;
	}
	
	public void setFake(boolean b) {
		fake = b;
	}
	
	public boolean isFake() {
		return fake;
	}
	
	public void setFirst(boolean b) {
		isFirst = b;
	}
	
	public void setLast(boolean b) {
		isLast = b;
	}
	
	public boolean isFirst() {
		return isFirst;
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public void setVariable(String var) {
		variable = new Variable(var);
	}
	
	public void setVariable(Variable var) {
		variable = var;
	}
	
	public boolean isDirect() {
		return isdirect;
	}
	
	public boolean isOne() {
		return isone;
	}
	
	public boolean isPath(){
		return variable != null && variable.isPath();
	}
	
	public boolean isXPath(){
		if (regex == null) return false;
		return regex.getName().equals(Term.XPATH);
	}
	
	public Expression getXPath(){
		return Term.function(Term.XPATH, subject, regex.getArg(0));
	}
	
	
	void setOne(boolean b) {
		isone = b;
	}

	
	/**
	 * @return  a variable on which we attach the evaluable expression
	 */
	public String getExpVariable() {
		if (exp == null)
			return null;
		Variable var = exp.getVariable();
		if (var != null)
			return var.getName();
		else
			return null;
	}
	
	public String getVariableName() {
		if (variable == null) return null;
		return variable.getName();
	}
	
	public Atom getSubject(){
		return subject;
	}
	
	public Atom getPredicate(){
		if (variable != null)
			return variable;
		else return predicate;
	}
	
	public Variable getVariable(){
		return variable;
	}
	
	public Constant getProperty(){
		return predicate;
	}
	
	public void setProperty(Constant prop){
		predicate = prop;
	}
	
	public Atom getObject(){
		return object;
	}
	
	
	
	public void setAll(boolean b) {
		isall = b;
	}
	
	public void setDirect(boolean b) {
		isdirect = b;
	}
	
	public void setIsset(boolean b) {
		isset = b;
	}
	
	public void setPath(int i) {
		star = i;
	}
	
	public void setRVar(String s) {
		rvar = s;
	}
	
	public void setVVar(String s) {
		pre = s;
	}
	
	private static String getRootPropertyQN() {
		return ASTQuery.getRootPropertyQN();
	}
	
	private String getRootPropertyURI() {
		return ASTQuery.getRootPropertyURI();
	}
	
	
	public boolean validateData(){
		if (subject.isSimpleVariable() || object.isSimpleVariable() || variable!=null){
			return false;
		}
		return true;
	}
	
	public boolean validateDelete(){
		if (subject.isBlankNode()){
			return false;
		}
		if (object.isBlankNode()){
			return false;
		}
		return true;
	}
	
	
	public boolean contains(Atom at){
		return subject.equals(at) || object.equals(at) || predicate.equals(at) || variable.equals(at);
	}
	
}
