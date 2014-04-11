package fr.inria.acacia.corese.triple.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.function.SQLFun;
import fr.inria.acacia.corese.cg.datatype.function.VariableResolver;
import fr.inria.acacia.corese.cg.datatype.function.XPathFun;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.edelweiss.kgram.api.core.ExpPattern;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;

public class Processor {
	private static Logger logger = Logger.getLogger(Processor.class);

	static final String functionPrefix = KeywordPP.CORESE_PREFIX;
        static final String KGRAM = ExpType.KGRAM;
        static final String STL   = ExpType.STL;
	public static final String BOUND = "bound";
	public static final String COUNT = "count";
	public static final String LIST  = "list";
	public static final String IN  	 = "in";

	private static final String MIN = "min";
	private static final String MAX = "max";
	private static final String SUM = "sum";
	private static final String AVG = "avg";
	private static final String ISURI = "isURI";
	private static final String ISIRI = "isIRI";
	private static final String ISBLANK = "isBlank";
	private static final String ISLITERAL = "isLiteral";
	private static final String ISNUMERIC = "isNumeric";
	private static final String LANG = "lang";
	private static final String REGEX = "regex";
	public static  final String MATCH = "match";
	private static final String LANGMATCH = "langMatches";
	private static final String STRDT = "strdt";
	private static final String STRLANG = "strlang";
	public static final String IF = "if";
	public static final String COALESCE = "coalesce";
	public static final String BNODE = "bnode";
	public static final String GROUPCONCAT = "group_concat";
	public static final String AGGAND = "agg_and";
	static final String SEPARATOR = "; separator=";
	private static final String SAMPLE = "sample";


	private static final String PLENGTH = "pathLength";
	private static final String KGPLENGTH = KGRAM + "pathLength";
	private static final String PWEIGHT = "pathWeight";
	private static final String KGPWEIGHT = KGRAM +"pathWeight";

	private static final String DATATYPE = "datatype";
	private static final String STR = "str";
	private static final String XSDSTRING = RDFS.XSD + "string";
	private static final String URI = "uri";
	private static final String IRI = "iri";

	private static final String SELF = "self";
	private static final String DEBUG = "trace";

	static final String EXTERN 	= "extern";
	public static final String XPATH 	= "xpath";
	static final String SQL 	= "sql";
	static final String KGXPATH = KGRAM + "xpath";
	static final String KGSQL 	= KGRAM + "sql";
	
	static final String WRITE 		= KGRAM + "write"; 
	static final String PPRINT 		= KGRAM + "pprint"; 
	static final String PPRINTWITH 		= KGRAM + "pprintWith"; 
	static final String PPRINTALL		= KGRAM + "pprintAll"; 
	static final String PPRINTALLWITH	= KGRAM + "pprintAllWith"; 
	static final String TEMPLATE		= KGRAM + "template"; 
	static final String TEMPLATEWITH	= KGRAM + "templateWith"; 
	static final String EVAL 		= KGRAM + "eval";
        static final String PROLOG 		= KGRAM + "prolog";
	static final String KGPPRINT		= ExpType.KPREF + ":" + "pprint"; 
        static final String STL_PPRINT		= NSManager.STL_PREF + ":" + "process"; 

	static final String STL_DEFAULT             = STL + "default"; 
	static final String STL_PROCESS             = STL + "process"; 
        static final String FOCUS_NODE              = STL + "getFocusNode";
        static final String APPLY_TEMPLATES         = STL + "apply-templates";
        static final String APPLY_TEMPLATES_WITH    = STL + "apply-templates-with";
        static final String APPLY_ALL_TEMPLATES     = STL + "apply-all-templates";
        static final String APPLY_ALL_TEMPLATES_WITH= STL + "apply-all-templates-with";
        static final String CALL_TEMPLATE           = STL + "call-template";
        static final String CALL_TEMPLATE_WITH      = STL + "call-template-with";
	static final String STL_TURTLE              = STL + "turtle"; 
	static final String STL_URI                 = STL + "uri"; 
	static final String STL_URILITERAL          = STL + "uriLiteral"; 
	static final String STL_XSDLITERAL          = STL + "xsdLiteral"; 
	static final String STL_PROLOG              = STL + "prolog"; 
	static final String STL_INDENT              = STL + "indent"; 
	static final String STL_LEVEL               = STL + "level"; 
	static final String STL_DEFINE              = STL + "define"; 

	static final String QNAME 	= KGRAM + "qname"; 
	static final String TURTLE 	= KGRAM + "turtle"; 
	static final String LEVEL 	= KGRAM + "level"; 
	static final String INDENT 	= KGRAM + "indent"; 
	static final String PPURI 	= KGRAM + "uri"; 
	static final String URILITERAL 	= KGRAM + "uriLiteral"; 
	static final String VISITED     = KGRAM + "isVisited"; 
	static final String ISSKOLEM    = KGRAM + "isSkolem"; 
	static final String SKOLEM      = KGRAM + "skolem"; 

	static final String UNNEST = "unnest";
	static final String KGUNNEST = KGRAM +"unnest";
	static final String SYSTEM = "system";
	static final String GROUPBY = "groupBy";

	static final String SPARQL   = KGRAM + "sparql";
	static final String SIMILAR  = KGRAM + "similarity";
	static final String CSIMILAR = KGRAM + "cSimilarity";
	static final String PSIMILAR = KGRAM + "pSimilarity";
	static final String ANCESTOR = KGRAM + "ancestor";
	static final String DEPTH    = KGRAM + "depth";
	static final String GRAPH    = KGRAM + "graph";
	static final String NODE     = KGRAM + "node";
	static final String GET      = KGRAM + "getObject";
	static final String SET      = KGRAM + "setObject";
	static final String GETP     = KGRAM + "getProperty";
	static final String SETP     = KGRAM + "setProperty";
	static final String LOAD     = KGRAM + "load";
	static final String NUMBER   = KGRAM + "number";
	static final String DISPLAY  = KGRAM + "display";
	static final String EXTEQUAL = KGRAM + "equals";
	static final String EXTCONT  = KGRAM + "contains";
	static final String PROCESS  = KGRAM + "process";
	static final String ENV  	 = KGRAM + "env";
	public static final String PATHNODE = KGRAM + "pathNode";

	static final String EXIST 	= Term.EXIST;
	static final String STRLEN 	= "strlen";
	static final String SUBSTR 	= "substr";
	static final String UCASE 	= "ucase";
	static final String LCASE 	= "lcase";
	static final String ENDS 	= "strends";
	static final String STARTS 	= "strstarts";
	static final String CONTAINS = "contains";
	static final String ENCODE 	= "encode_for_uri";
	static final String CONCAT 	= "concat"; 
	static final String STRBEFORE 	= "strbefore"; 
	static final String STRAFTER 	= "strafter"; 
	static final String STRREPLACE 	= "replace"; 
	static final String UUID 		= "uuid"; 
	static final String STRUUID 	= "struuid"; 

	
	static final String RANDOM 	= "rand"; 
	static final String ABS 	= "abs"; 
	static final String CEILING = "ceil"; 
	static final String FLOOR 	= "floor"; 
	static final String ROUND 	= "round"; 

	static final String NOW 	= "now"; 
	static final String YEAR 	= "year"; 
	static final String MONTH 	= "month"; 
	static final String DAY 	= "day"; 
	static final String HOURS 	= "hours";
	static final String MINUTES = "minutes";
	static final String SECONDS = "seconds";
	static final String TIMEZONE = "timezone";
	static final String TZ 		= "tz";

	static final String MD5 	= "md5";
	static final String SHA1 	= "sha1";
	static final String SHA224 	= "sha224";
	static final String SHA256	= "sha256";
	static final String SHA384 	= "sha384";
	static final String SHA512 	= "sha512";
	
	

	
	Term term;
	List<Expr> lExp;
	int type, oper;
	Pattern pat;
	Matcher match;
	XPathFun xfun;
	SQLFun sql;
	VariableResolver resolver;
	Object processor;
	Method fun;
	ExpPattern pattern;
	boolean isCorrect = true;
	
	private static final int IFLAG[] = {
		Pattern.DOTALL, Pattern.MULTILINE, Pattern.CASE_INSENSITIVE,
		Pattern.COMMENTS};
	static final String SFLAG[] = {"s", "m", "i", "x"};
	
	
	public static Hashtable<String, Integer> table;
	public static Hashtable<Integer, String> tname, toccur;

	Processor(){
        }
        
	Processor(Term t){
		term = t;
	}
        
        public static Processor create(){
            return new Processor();
        }
	
	
	// Exp
	
	public List<Expr> getExpList(){
		return lExp;
	}
	
	// filter(exist {PAT})
	public ExpPattern getPattern(){
		return pattern;
	}
	
	public void setPattern(ExpPattern pat){
		 pattern = pat;
	}

	
	Expr getExp(int i){
		return lExp.get(i);
	}
        
        void setExp(int i, Expr e){
            if (i < lExp.size()){
                lExp.set(i, e);
            }
            else if (i == lExp.size()){
                lExp.add(i, e);
            }
        }
        
        void addExp(int i, Expr e){
            lExp.add(i, e);
        }

	
	void setArguments(){
		if (lExp == null){
			lExp = new ArrayList<Expr>();
			for (Expr e : term.getArgs()){
				lExp.add(e);
			}
		}
	}
	
	public int arity(){
		return lExp.size();
	}
	
	
	public int type(){
		return type;
	}
        
        void setOper(int n){
            oper = n;
        }
	
	public void compile(ASTQuery ast){
		if (table == null){
			deftable();
		}
		if (term.isFunction()){
			type = ExprType.FUNCTION;
			oper = getOper();
			switch(oper){
				case ExprType.IN: 	compileInList(); break;
				case ExprType.HASH:     compileHash(); break;
				case ExprType.URI: 	compileURI(ast); break;
				case ExprType.CAST:     compileCast(); break;
				case ExprType.REGEX:    compileRegex(); break;
				case ExprType.STRREPLACE: compileReplace(); break;
				case ExprType.XPATH:    compileXPath(ast); break;
				case ExprType.SQL:      compileSQL(ast); break;
				case ExprType.EXTERNAL: compileExternal(ast); break;
			}
		}
		else if (term.isAnd()){
			type = ExprType.BOOLEAN;
			oper = ExprType.AND;
		}
		else if (term.isOr()){
			type = ExprType.BOOLEAN;
			oper = ExprType.OR;
		}
		else if (term.isNot()){
			type = ExprType.BOOLEAN;
			oper = ExprType.NOT;
		}
		else {
			type = ExprType.TERM;
			oper = getOper();
		}
		
		if (oper == ExprType.UNDEF){
			if (term.isPathExp()){
				// Property Path Exp
			}
			else {
				ast.addError("Undefined expression: ", term);
			}
		}
		setArguments();
		check(ast);
	}
	
	// TODO: error message
	void check(ASTQuery ast){
		if (term.isAggregate()){
			if (oper() == ExprType.GROUPCONCAT){

			}
			else if (term.getArity() > 1){
				ast.setCorrect(false);
				//ast.addError("Arity error: ", term);
			}
		}
	}
	
	
	void deftable(){
		table = new Hashtable<String, Integer>();
		tname = new Hashtable<Integer, String>();
		toccur = new Hashtable<Integer, String>();

		defoper("<", 	ExprType.LT);
		defoper("<=", 	ExprType.LE);
		defoper("=", 	ExprType.EQ);
		defoper("!=", 	ExprType.NEQ);
		defoper(">", 	ExprType.GT);
		defoper(">=", 	ExprType.GE);
		defoper("~", 	ExprType.CONT);
		defoper(KGRAM+"tilda", 	ExprType.CONT);
		defoper("^", 	ExprType.START);
		defoper(IN, 	ExprType.IN);
		defoper("+", 	ExprType.PLUS);
		defoper("-", 	ExprType.MINUS);
		defoper("*", 	ExprType.MULT);
		defoper("/", 	ExprType.DIV);
		
		defoper("<:", 	ExprType.TLT);
		defoper("<=:", 	ExprType.TLE);
		defoper("=:", 	ExprType.TEQ);
		defoper("!=:", 	ExprType.TNEQ);
		defoper(">:", 	ExprType.TGT);
		defoper(">=:", 	ExprType.TGE);
		
		defoper(BOUND, ExprType.BOUND);
		defoper(COUNT, 	ExprType.COUNT);
		defoper(MIN, 	ExprType.MIN);
		defoper(MAX, 	ExprType.MAX);
		defoper(SUM, 	ExprType.SUM);
		defoper(AVG, 	ExprType.AVG);
		defoper(ISURI, 	ExprType.ISURI);
		defoper(ISIRI, 	ExprType.ISURI);
		defoper(ISBLANK, ExprType.ISBLANK);
		defoper(ISLITERAL, ExprType.ISLITERAL);
		defoper(ISNUMERIC, ExprType.ISNUMERIC);
		defoper(LANG, 	ExprType.LANG);
		defoper(LANGMATCH, ExprType.LANGMATCH);
		
		defoper(STRDT, 		ExprType.STRDT);
		defoper(STRLANG, 	ExprType.STRLANG);
		defoper(BNODE, 		ExprType.BNODE);
		defoper(PATHNODE, 	ExprType.PATHNODE);
		defoper(COALESCE, 	ExprType.COALESCE);
		defoper(IF, 		ExprType.IF);
		defoper(GROUPCONCAT,    ExprType.GROUPCONCAT);
		defoper(AGGAND,         ExprType.AGGAND);
		defoper(SAMPLE, 	ExprType.SAMPLE);
		defoper(LIST, 		ExprType.LIST);
		defoper(ISSKOLEM,       ExprType.ISSKOLEM);
		defoper(SKOLEM,         ExprType.SKOLEM);

		
		defoper(REGEX, 		ExprType.REGEX);
		defoper(DATATYPE, 	ExprType.DATATYPE);
		defoper(STR, 		ExprType.STR);
		defoper(XSDSTRING, 	ExprType.XSDSTRING);
		defoper(URI, 		ExprType.URI);
		defoper(IRI, 		ExprType.URI);
		defoper(SELF, 		ExprType.SELF);
		defoper(DEBUG, 		ExprType.DEBUG);

		defoper(MATCH, 	ExprType.SKIP);
		defoper(PLENGTH, ExprType.LENGTH);
		defoper(KGPLENGTH, ExprType.LENGTH);
		defoper(KGPWEIGHT, ExprType.PWEIGHT);

		defoper(XPATH, 	ExprType.XPATH);
		defoper(KGXPATH, 	ExprType.XPATH);
		defoper(SQL, 	ExprType.SQL);
		defoper(KGSQL, 	ExprType.SQL);
		defoper(SPARQL, ExprType.KGRAM);
		defoper(EXTERN, ExprType.EXTERN);
		defoper(UNNEST, ExprType.UNNEST);
		defoper(KGUNNEST, ExprType.UNNEST);
		defoper(EXIST,  ExprType.EXIST);
		defoper(SYSTEM, ExprType.SYSTEM);
		defoper(GROUPBY, ExprType.GROUPBY);
		
		defoper(WRITE, 	ExprType.WRITE);
		defoper(QNAME, 	ExprType.QNAME);
                
		defoper(PPRINT, 	ExprType.APPLY_TEMPLATES);
		defoper(EVAL, 		ExprType.APPLY_TEMPLATES);
		defoper(PPRINTWITH, 	ExprType.APPLY_TEMPLATES_WITH);
		defoper(PPRINTALL, 	ExprType.APPLY_ALL_TEMPLATES);
		defoper(PPRINTALLWITH, 	ExprType.APPLY_ALL_TEMPLATES_WITH);
		defoper(TEMPLATE, 	ExprType.CALL_TEMPLATE);
		defoper(TEMPLATEWITH, 	ExprType.CALL_TEMPLATE_WITH);
		defoper(TURTLE,         ExprType.TURTLE);
                
                defoper(FOCUS_NODE,             ExprType.FOCUS_NODE);
                defoper(APPLY_TEMPLATES, 	ExprType.APPLY_TEMPLATES);
		defoper(APPLY_TEMPLATES_WITH, 	ExprType.APPLY_TEMPLATES_WITH);
		defoper(APPLY_ALL_TEMPLATES, 	ExprType.APPLY_ALL_TEMPLATES);
		defoper(APPLY_ALL_TEMPLATES_WITH,ExprType.APPLY_ALL_TEMPLATES_WITH);
		defoper(CALL_TEMPLATE,          ExprType.CALL_TEMPLATE);
		defoper(CALL_TEMPLATE_WITH, 	ExprType.CALL_TEMPLATE_WITH);
                defoper(STL_PROCESS,            ExprType.STL_PROCESS);
		defoper(STL_TURTLE,             ExprType.TURTLE);
                defoper(STL_URI,                ExprType.PPURI);
                defoper(STL_PROLOG,             ExprType.PROLOG);
		defoper(STL_INDENT,             ExprType.INDENT);
		defoper(STL_LEVEL,              ExprType.LEVEL);
		defoper(STL_URILITERAL, 	ExprType.URILITERAL);
		defoper(STL_XSDLITERAL,         ExprType.XSDLITERAL);

		defoper(LEVEL, 	ExprType.LEVEL);
		defoper(INDENT, ExprType.INDENT);
		defoper(PPURI, 	ExprType.PPURI);
		defoper(URILITERAL, 	ExprType.URILITERAL);
		defoper(STL_XSDLITERAL, 	ExprType.XSDLITERAL);
		defoper(VISITED, ExprType.VISITED);
		defoper(PROLOG,  ExprType.PROLOG);
		defoper(STL_DEFINE,  ExprType.STL_DEFINE);
                defoper(STL_DEFAULT, ExprType.STL_DEFAULT);

		defoper(SIMILAR, ExprType.SIM);
		defoper(CSIMILAR, ExprType.SIM);
		defoper(PSIMILAR, ExprType.PSIM);
		defoper(ANCESTOR, ExprType.ANCESTOR);
		defoper(DEPTH,   ExprType.DEPTH);
		defoper(GRAPH,   ExprType.GRAPH);
		defoper(NODE,    ExprType.NODE);
		defoper(GET,     ExprType.GET);
		defoper(SET,     ExprType.SET);
		defoper(GETP,    ExprType.GETP);
		defoper(SETP,    ExprType.SETP);
		
		defoper(LOAD,    ExprType.LOAD);
		defoper(NUMBER,  ExprType.NUMBER);
		defoper(DISPLAY, ExprType.DISPLAY);
		defoper(EXTEQUAL,ExprType.EXTEQUAL);
		defoper(EXTCONT, ExprType.EXTCONT);
		defoper(PROCESS, ExprType.PROCESS);
		defoper(ENV, 	 ExprType.ENV);

		defoper(STRLEN, ExprType.STRLEN);
		defoper(SUBSTR, ExprType.SUBSTR);
		defoper(UCASE, 	ExprType.UCASE);
		defoper(LCASE, 	ExprType.LCASE);
		defoper(ENDS, 	ExprType.ENDS);
		defoper(STARTS, ExprType.STARTS);
		defoper(CONTAINS, ExprType.CONTAINS);
		defoper("strcontains", ExprType.CONTAINS);
		defoper(ENCODE, ExprType.ENCODE);
		defoper(CONCAT, ExprType.CONCAT);
		defoper(STRBEFORE, ExprType.STRBEFORE);
		defoper(STRAFTER, ExprType.STRAFTER);
		defoper(STRREPLACE, ExprType.STRREPLACE);
		defoper(UUID, ExprType.FUUID);
		defoper(STRUUID, ExprType.STRUUID);

		
		defoper(RANDOM, ExprType.RANDOM);
		defoper(ABS, 	ExprType.ABS);
		defoper(FLOOR, 	ExprType.FLOOR);
		defoper(ROUND, 	ExprType.ROUND);
		defoper(CEILING, ExprType.CEILING);

		defoper(NOW, 	ExprType.NOW);
		defoper(YEAR, 	ExprType.YEAR);
		defoper(MONTH, 	ExprType.MONTH);
		defoper(DAY, 	ExprType.DAY);
		defoper(HOURS, 	ExprType.HOURS);
		defoper(MINUTES, ExprType.MINUTES);
		defoper(SECONDS, ExprType.SECONDS);
		defoper(TIMEZONE, ExprType.TIMEZONE);
		defoper(TZ, 	ExprType.TZ);


		defoper(MD5, 	ExprType.HASH);
		defoper(SHA1, 	ExprType.HASH);
		defoper(SHA224, ExprType.HASH);
		defoper(SHA256, ExprType.HASH);
		defoper(SHA384, ExprType.HASH);
		defoper(SHA512, ExprType.HASH);

	}
	
	void defoper(String key, int value){
		table.put(key.toLowerCase(), value);
		tname.put(value, key);
	}
	       
	
	int getOper(){
                return getOper(term.getLabel());
        }
                
        public int getOper(String name){
		Integer n = table.get(name.toLowerCase());
		if (n == null){
			if (name.startsWith(RDFS.XSDPrefix) || name.startsWith(RDFS.XSD) || 
				name.startsWith(RDFS.RDFPrefix) || name.startsWith(RDFS.RDF)){
				n = ExprType.CAST;
			}
			else if (name.startsWith(KeywordPP.CORESE_PREFIX)){
				n = ExprType.EXTERNAL;
			}
			else {
				n = ExprType.UNDEF;
			}
		}
		// draft: record occurrences during test case
		//toccur.put(n, name);
		return n;
	}
        	
	public static void finish(){
		for (Integer n : table.values()){
			if (! toccur.containsKey(n)){
				System.out.println("Missing test: " + tname.get(n));
			}
		}
	}
	
	
	/**
	 * xsd:integer(?x)
	 * ->
	 * cast(?x, xsd:integer, CoreseInteger)
	 */
	void compileCast(){
		// name = xsd:integer | ... | str
		String name = term.getName();
		Constant dt = Constant.createResource(name);
		dt.getDatatypeValue();
		// type = CoreseInteger
		Constant type = Constant.create(Constant.getJavaType(name), RDFS.xsdstring);
		type.getDatatypeValue();
		lExp = new ArrayList<Expr>();
		lExp.add(term.getArg(0));
		lExp.add(dt);
		lExp.add(type);
	}
	
	/**
	 * sha256(?x) ->
	 * hash("SHA-256", ?x)
	 */
	void compileHash(){
		String name = term.getName();
		if (name.startsWith("sha") || name.startsWith("SHA")){
			name = "SHA-" + name.substring(3);
		}
		term.setModality(name);
	}
	
	void compileURI(ASTQuery ast){
		String base = ast.getNSM().getBase();
		if (base!=null && base!=""){
			term.setModality(ast.getNSM().getBase());
		}
	}
	
	void compileInList(){
		// ?x in (a b)
		
	}
	
	
	public int oper(){
		return oper;
	}
	
	/**
	 * term = regex(?x,  ".*toto",  ["i"])
	 * match.reset(string);
	 * boolean res = match.matches();
	 */
	void compileRegex(){
		if (term.getArg(1).isConstant()){
			compilePattern(term.getArg(1).getName());
		}

	}
	
	
	void compilePattern(String patdtvalue){
		String sflag = null;
		if (term.getArity()==3){
			sflag = term.getArg(2).getName();
		}

		int flag = 0;
		if (sflag!=null){ // flag argument "smix"
			for (int i = 0; i < IFLAG.length; i++){
				if (sflag.indexOf(SFLAG[i]) != -1){ //is flag[i] present
					flag =  flag | IFLAG[i]; // add the corresponding int flag
				}
			}
		}

		if (!patdtvalue.startsWith("^") && !patdtvalue.startsWith(".*"))
			patdtvalue = ".*"+patdtvalue;
		if (!patdtvalue.endsWith("$") && !patdtvalue.endsWith(".*"))
			patdtvalue = patdtvalue+".*";

		if (flag == 0)
			pat = Pattern.compile(patdtvalue);
		else pat = Pattern.compile(patdtvalue, flag);
		
		match = pat.matcher("");
	}
	
	void compileReplace(){
		if (term.getArg(1).isConstant()){
			compileReplace(term.getArg(1).getName());
		}
	}
	
	// TODO: test if constant
	void compileReplace(String str){
		pat = Pattern.compile(str);
		match = pat.matcher("");
	}
	
	// replace('%abc@def#', '[^a-z0-9]', '-')
	public String replace(String str, String rep){
		match.reset(str);
		String res = match.replaceAll(rep);
		return res;
	}
	
	public boolean regex(String str){
		match.reset(str);
		return match.matches();
	}
	
	public boolean regex(String str, String exp){
		if (term.getArg(1).isVariable()){
			compilePattern(exp);
		}
		match.reset(str);
		return match.matches();
	}
	
	void compileSQL(ASTQuery ast){
		//sql = new SQLFun();
	}
	
	// @deprecated
	public ResultSet sql(IDatatype uri, IDatatype login, IDatatype passwd, IDatatype query){
		return null; //return sql.sql(uri, login, passwd, query);
	}
	
	public ResultSet sql(IDatatype uri, IDatatype driver, IDatatype login, IDatatype passwd, IDatatype query){
		return null; //return sql.sql(uri, driver, login, passwd, query);
	}
	
	/**
	 * xpath(?g, '/book/title')
	 */
	void compileXPath(ASTQuery ast){
		xfun = new XPathFun();
		if (ast == null) ast = ASTQuery.create();
		xfun.init(ast.getNSM(),  !true);
	}
	
	public XPathFun getXPathFun(){
		return xfun;
	}
	
	public IDatatype xpath(IDatatype doc, IDatatype exp){
		try {
		IDatatype dt = xfun.xpath(doc, exp);
		return dt;
		}
		catch (RuntimeException e){
			logger.error("XPath error: " + e.getMessage());
		}
		return null;
	}
	
	public VariableResolver getResolver(){
		return xfun.getResolver();
	}
	
	public void setResolver(VariableResolver res){
		xfun.set(res);
	}
	

	public void setProcessor(Object obj){
		processor = obj;
	}
	
	public void setMethod(Method m){
		fun = m;
	}
	
	public Method getMethod() {
		return fun;
	}
	
	
	/**
	 * Load external method definition for ext:fun
	 * prefix ext: <function://package.className>
	 * ext:fun() 
	 */
	void compileExternal(ASTQuery ast)  {
		String oper = term.getLabel();
		String p ;
		String path ;
		Class c = null;  
		isCorrect = false;
		try {
			if (! oper.startsWith(functionPrefix)) {
				String message = "Undefined function: "+oper;
				if (oper.contains("://")) 
					message += "\nThe prefix should start with \""+functionPrefix+"\"";
				logger.warn(message);
				return ;
			}
			int lio = oper.lastIndexOf(".");
			if (lio == -1){
				logger.error("Undefined function: "+oper);
				return;
			}
			p = oper.substring(0, lio);
			path = p.substring(functionPrefix.length(),p.length());
			oper = oper.substring(p.length() + 1, oper.length());
			
			ClassLoader cl = getClass().getClassLoader(); 
			c = cl.loadClass(path);
			
			Class<Object>[] aclasses = new Class[term.getArity()];
			for (int i = 0; i < aclasses.length; i++) {
				aclasses[i] = Object.class;
			}
			
			setProcessor(c.newInstance());  
			setMethod(c.getMethod(oper, aclasses));
			isCorrect = true;
		} 
		
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	/**
	 * Eval external method
	 */
	public Object eval(Object[] args){
		if (! isCorrect) return null;
		try {
			return fun.invoke(processor, args);
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
