package fr.inria.edelweiss.kgenv.eval;


import fr.inria.acacia.corese.triple.parser.Dataset;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgenv.parser.Transformer;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgram.event.EventListener;
import fr.inria.edelweiss.kgram.event.EventManager;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;



/**
 * Evaluator of SPARQL query by KGRAM
 * Ready to use Package with KGRAM and SPARQL Parser & Transformer
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QuerySolver  {
	private static Logger logger = Logger.getLogger(QuerySolver.class);
	
	public static final int STD_ENTAILMENT  = 0;
	public static final int RDF_ENTAILMENT  = 1;
	public static final int RDFS_ENTAILMENT = 2;

	static String NAMESPACES;

	protected EventManager manager;
	protected ResultListener listener;
	protected Producer producer;
	protected Provider provider;
	protected Evaluator evaluator;
	protected Matcher matcher;
	protected Sorter sort;
	protected List<QueryVisitor> visit;

	
	boolean isListGroup = false,
	isListPath = false,
	isCountPath = false,
	isCheckLoop = false,
	isDebug = false,
	isOptimize = false,
	isSPARQLCompliant = false;
        // skolemize construct/describe result graphs (and only them)
        private boolean isSkolem = false;

	private boolean isDetail = false;
	
	boolean isSequence = false;
	
	int slice = 0;
	
	// set default base for SPARQL Query
	// it is overloaded if query has a base (cf prefix/base)
	// use case: expand from <data.ttl> in manifest.ttl
	String defaultBase;

	private BasicGraphPattern pragma;
	
	static int count = 0;
	
	static boolean test = true;
	
	public QuerySolver (){
	}
	
	protected QuerySolver (Producer p, Evaluator e, Matcher m){
		producer = p;
		evaluator = e;
		matcher = m;
		setPragma(BasicGraphPattern.create());
	}

	public static QuerySolver create(){
		return new QuerySolver();
	}
		

	public static QuerySolver create(Producer prod, Evaluator ev, Matcher match){
		QuerySolver exec = new QuerySolver(prod, ev, match);
		return exec;
	}
	
	public static QuerySolver create(Producer prod, Matcher match){
		Interpreter eval  = interpreter(prod);
		QuerySolver exec = new QuerySolver(prod, eval, match);
		return exec;
	}
	
	public static Interpreter interpreter(Producer p){
		Interpreter eval  = new Interpreter(new ProxyImpl());
		eval.setProducer(p);
		return eval;
	}
        
	public void add(Producer prod){
		MetaProducer meta;
		if (producer instanceof MetaProducer){
			meta = (MetaProducer) producer;
		}
		else {
			meta = MetaProducer.create();
                        meta.add(producer);
			producer = meta;
                        evaluator.setProducer(producer);
		}
		meta.add(prod);
	}
        
	public void set(Sorter s){
		sort = s;
	}
	
	public void setVisitor(QueryVisitor v){
		addVisitor(v);
	}
	
	
	public void addVisitor(QueryVisitor v){
		if (visit == null){
			visit = new ArrayList<QueryVisitor>();
		}
		visit.add(v);
	}
	
	public void set(Provider p){
		provider = p;
	}
	
	public Provider getProvider(){
		return provider;
	}
	
	public void setSPARQL1(boolean b){
	}
	
	public static void defaultSPARQL1(boolean b){
	}
	
	public void setDefaultBase(String str){
		defaultBase = str;
	}
	
	protected Transformer transformer(){
		Transformer transformer = Transformer.create();
		if (sort != null) {
			transformer.set(sort);
		}
		if (visit!=null){
			transformer.add(visit);
		}
		return transformer;
	}
	
	/**
	 * Does not perform construct {} if any
	 * it return the Mappings in this case
	 */
	public Mappings basicQuery(ASTQuery ast) {
		return basicQuery(ast, null);
	}
	public Mappings basicQuery(ASTQuery ast, Dataset ds) {
		if (ds!=null){
			ast.setDefaultDataset(ds);
		}
		Transformer transformer =  transformer();
		Query query = transformer.transform(ast);
		return query(query, null);
	}
	
	
	public Mappings query(String squery) throws EngineException{
		return query(squery, null, null);
	}
	
	public Mappings query(String squery, Mapping map) throws EngineException{
		return query(squery, map, null);
	}

//	public Mappings query(String squery, Mapping map, List<String> from, List<String> named) throws EngineException{
//		Dataset ds = Dataset.create(from, named);
//		return query(squery, map, ds);
//	}
	
	public Mappings query(String squery, Mapping map, Dataset ds) throws EngineException{
		Query query = compile(squery, ds);
		return query(query, map);
	}
	
	
	/**
	 * Core QueryExec processor
	 */
	public Mappings query(Query query, Mapping map){
		init(query);
		debug(query);
		
        if (producer instanceof MetaProducer){
			query.setDistribute(true);
			if (isSequence){
				return queries(query, map);
			}
		}

		Eval kgram = Eval.create(producer, evaluator, matcher);
		kgram.set(provider);

		events(kgram);
		
		pragma(kgram, query);
		
		Mappings lMap  = kgram.query(query, map);
		
		return lMap;
	}
	
	
	void init(Query q){
		q.setListGroup(isListGroup);
		q.setListPath(isListPath);
		q.setCountPath(isCountPath);
		q.setCheckLoop(isCheckLoop);
		q.setDetail(isDetail);
		q.setSlice(slice);
		if (isDebug) q.setDebug(isDebug);
		if (isOptimize) q.setOptimize(isOptimize);
	}
	

//	public Transformer getTransformer(){
//		return transformer;
//	}
	
	public Matcher getMatcher(){
		return matcher;
	}
	
	public Producer getProducer(){
		return producer;
	}
	
	public Evaluator getEvaluator(){
		return evaluator;
	}
	
	public ASTQuery getAST(Query q){
		return (ASTQuery) q.getAST();
	}
	
	public ASTQuery getAST(Mappings lm){
		Query q = lm.getQuery();
		return getAST(q);
	}
		
	public void addEventListener(EventListener el){
		if (manager == null) manager = new EventManager();
		manager.addEventListener(el);
	}
	
	public void addResultListener(ResultListener el){
		listener = el;
	}

	
	public Query compile(String squery) throws EngineException {
		return compile(squery, null);
	}
	
	// rule: construct where 
	public Query compileRule(String squery) throws EngineException {
		Transformer transformer =  transformer();			
		transformer.setNamespaces(NAMESPACES);
		transformer.setBase(defaultBase);
		transformer.setPragma(getPragma());
		Query query = transformer.transform(squery, true);
		return query;	
	}
	
	public Query compile(ASTQuery ast) {
		Transformer transformer =  transformer();			
		transformer.setSPARQLCompliant(isSPARQLCompliant);
		transformer.setNamespaces(NAMESPACES);
		transformer.setPragma(getPragma());
		Query query = transformer.transform(ast);
		return query;
	}
	
	public Query compile(String squery, Dataset ds) throws EngineException {
		Transformer transformer =  transformer();			
		transformer.setSPARQLCompliant(isSPARQLCompliant);
		transformer.setNamespaces(NAMESPACES);
		transformer.setBase(defaultBase);
		transformer.setPragma(getPragma());
		transformer.set(ds);

		Query query = transformer.transform(squery);
		return query;
	}
	
	
	public Mappings filter(Mappings map, String filter) throws EngineException{
		Query q = compileFilter(filter);
		Eval kgram = Eval.create(producer, evaluator, matcher);
		kgram.filter(map, q);
		return map;
	}
	
	Query compileFilter(String filter) throws EngineException {
		String str = "select * where {} having(" + filter + ")";
		Query q = compile(str);
		return q;
	}

		
	public Mappings query(Query query){
		return query(query, null);
	}
	
	

	
	/**
	 * Draft with several producers in sequence
	 * TODO:
	 * PB with aggregates, limit
	 */
	public Mappings queries(Query query, Mapping map){
		
		MetaProducer meta = (MetaProducer) producer;
		
		Mappings lMap = null;
		
		for (Producer p : meta){
			
			Eval kgram = Eval.create(p, evaluator, matcher);
			if (lMap != null) kgram.setMappings(lMap);

			events(kgram);

			pragma(kgram, query);

			lMap  = kgram.query(query, map);
		}
		
		return lMap;
	}
	

	void debug(Query query){
		if (query.isDebug()){
			logger.debug(query.getBody());
			logger.debug("limit " + query.getLimit());
			if (query.isFail()){
				logger.debug("Fail at compile time");
			}
		}
	}
	
	void events(Eval kgram){
		if (manager!=null){
			for (EventListener el : manager){
				kgram.addEventListener(el);
			}
		}
		kgram.addResultListener(listener);
	}
	
	void pragma(Eval kgram, Query query){
		ASTQuery ast = (ASTQuery) query.getAST();
		Pragma pg = new Pragma(kgram, query, ast);
		if (getPragma() != null) {
			pg.parse(getPragma());
		}
		if (ast!=null && ast.getPragma() != null){
			pg.parse();
		}
	}
	
	public void addPragma(String subject, String property, String value){
		Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
		getPragma().add(t);
	}
	
	public void addPragma(String subject, String property, int value){
		Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
		getPragma().add(t);
	}
	
	public void addPragma(String subject, String property, boolean value){
		Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
		getPragma().add(t);
	}
	
	public void addPragma(Atom subject, Atom property, Atom value){
		if (getPragma() == null) {
			setPragma(BasicGraphPattern.create());
		}
		getPragma().add(Triple.create(subject, property, value));
	}
	
	
	public static void defaultNamespaces (String ns){
		NAMESPACES = ns;
	}
	
	public static void definePrefix(String pref, String ns){
		if (NAMESPACES == null) NAMESPACES = "";
		NAMESPACES += pref + " " + ns + " ";
	}
	
	public void setListGroup(boolean b){
		isListGroup = b;
	}
	
	public void setListPath(boolean b){
		isListPath = b;
	}
	
	public void setCountPath(boolean b){
		isCountPath = b;
	}
	
	public void setPathLoop(boolean b){
		isCheckLoop = !b;
	}
	

	public void setSPARQLCompliant(boolean isSPARQLCompliant) {
		this.isSPARQLCompliant = isSPARQLCompliant;
	}


	boolean isSPARQLCompliant() {
		return isSPARQLCompliant;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	public boolean isDebug(){
		return isDebug;
	}
	
	public void setOptimize(boolean b){
		isOptimize = b;
	}
	
	public void setSlice(int n){
		slice = n;
	}

	public BasicGraphPattern getPragma() {
		return pragma;
	}

	public void setPragma(BasicGraphPattern pragma) {
		this.pragma = pragma;
	}

	public boolean isDetail() {
		return isDetail;
	}

	public void setDetail(boolean isDetail) {
		this.isDetail = isDetail;
	}

    /**
     * @return the isSkolem
     */
    public boolean isSkolem() {
        return isSkolem;
    }

    /**
     * @param isSkolem the isSkolem to set
     */
    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }
	
}
