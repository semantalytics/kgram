package fr.inria.edelweiss.kgraph.query;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.Transformer;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
//import java.net.URL;


/**
 * Evaluator of SPARQL query by KGRAM
 * Implement KGRAM  as a lightweight version with KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryProcess extends QuerySolver {
	
	//sort query edges taking cardinality into account
	static boolean isSort = false;

	Construct constructor;
	Loader load;
	ReentrantReadWriteLock lock;
	// Producer may perform match locally
	boolean isMatch = false;
		
	public QueryProcess (){
	}
	
	
	protected QueryProcess (Producer p, Evaluator e, Matcher m){
		super(p, e, m);
		init();
	}
	
	void init(){
		if (isSort && producer instanceof ProducerImpl){
			ProducerImpl pp = (ProducerImpl) producer;
			set(SorterImpl.create(pp.getGraph()));
		}
		
		Graph g = getGraph();
		if (g != null){
			lock = g.getLock();
		}
		else {
			// TODO: the lock should be unique to all calls
			// hence it should be provided by Producer
			lock = new ReentrantReadWriteLock();
		}
	}


	public static QueryProcess create(Graph g){
		return create(g, false);
	}
	
	/**
	 * isMatch = true: 
	 * Each Producer perform local Matcher.match() on its own graph for subsumption
	 * Hence each graph can have its own ontology 
	 * isMatch = false: (default)
	 * Global producer perform Matcher.match()
	 */
	public static QueryProcess create(Graph g, boolean isMatch){
		ProducerImpl p =  ProducerImpl.create(g);
		p.setMatch(isMatch);
		QueryProcess exec = QueryProcess.create(p);
		exec.setMatch(isMatch);
		return exec;
	}
	
	public static QueryProcess create(Graph g, Graph g2){
		QueryProcess qp = QueryProcess.create(g);
		qp.add(g2);
		return qp;
	}
	
	public static void setSort(boolean b){
		isSort = b;
	}

	public void setLoader(Loader ld){
		load = ld;
	}
	
	public Loader getLoader(){
		return load;
	}
	
	void setMatch(boolean b){
		isMatch = b;
	}
        
	public Producer add(Graph g){
		ProducerImpl p = ProducerImpl.create(g);
		Matcher match  =  MatcherImpl.create(g);
		p.set(match);
		if (isMatch){
			p.setMatch(true);
		}
		add(p);
		return p;
	}
	
	public static QueryProcess create(ProducerImpl prod){
		Matcher match =  MatcherImpl.create(prod.getGraph());
		prod.set(match);
		if (prod.isMatch()){
			// there is local match in Producer
			// create global match with Relax mode 
			match =  MatcherImpl.create(prod.getGraph());
			match.setMode(Matcher.RELAX);
		}
		QueryProcess exec = QueryProcess.create(prod,  match);
		return exec;
	}
	
	public static QueryProcess create(Producer prod, Matcher match){
		Interpreter eval  = createInterpreter(prod, match);
		QueryProcess exec = new QueryProcess(prod, eval, match);
 		return exec;
	}
	
	public static QueryProcess create(Producer prod, Evaluator ev, Matcher match){
		QueryProcess exec = new QueryProcess(prod, ev, match);
		return exec;
	}
	
	public static Interpreter createInterpreter(Producer p, Matcher m){
		Interpreter eval  = interpreter(p);
		Graph g = sGetGraph(p);
		if (g != null){
			eval.getProxy().setPlugin(PluginImpl.create(g, m));
		}
		return eval;
	}
	
	
	/****************************************************************
	 * 
	 * API for query
	 * 
	 ****************************************************************/
	
	public Mappings update(String squery) throws EngineException{
		return query(squery, null, null, null);
	}
	
	public Mappings query(String squery) throws EngineException{
		return query(squery, null, null, null);
	}
	
	public Mappings query(String squery, Mapping map, List<String> from, List<String> named) throws EngineException{
		Query q = compile(squery, from, named);
		return query(q, map, from, named);
	}	
	
	public Mappings query(Query q) {
		return qquery(q, null);
	}

	public Mappings qquery(Query q, Mapping map) {
		try {
			return query(q, map, null, null);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Mappings.create(q);
	}
	
	
	
	/**
	 * KGRAM + full SPARQL compliance :
	 * - type of arguments of functions (e.g. sparql regex require string)
	 * - variable in select with group by
	 * - specify the dataset
	 */
	public Mappings sparql(String squery, List<String> from, List<String> named) throws EngineException{
		return sparqlQueryUpdate(squery, from, named, STD_ENTAILMENT);
	}
	
	public Mappings sparql(String squery, List<String> from, List<String> named, int entail) throws EngineException{
		return sparqlQueryUpdate(squery, from, named, entail);
	}
	
	
	public Mappings query(ASTQuery ast){
		if (ast.isUpdate()){
			return update(ast);
		}
		return synQuery(ast);
	}
	
	/**
	 * equivalent of std query(ast) but for update
	 */
	public Mappings update(ASTQuery ast){
		Transformer transformer =  transformer();
		Query query = transformer.transform(ast);
		return query(query);
	}
	
	
	/******************************************
	 * 
	 * Secure Query OR Update
	 * 
	 ******************************************/
	
	public Mappings sparqlQuery(String squery) throws EngineException{
		Query q = compile(squery, null, null);
		if (q.isUpdate()){
			throw new EngineException("Unauthorized Update in SPARQL Query:\n" + squery);
		}
		return query(q, null, null, null);
	}
	
	public Mappings sparqlUpdate(String squery) throws EngineException{
		Query q = compile(squery, null, null);
		if (! q.isUpdate()){
			throw new EngineException("Unauthorized Query in SPARQL Update:\n" + squery);
		}
		return query(q, null, null, null);
	}

	public Mappings sparqlQueryUpdate(String squery) throws EngineException{
		return query(squery);
	}

	
	/****************************************************************************
	 * 
	 * 
	 ****************************************************************************/
	
	Mappings query(Query q, Mapping map, List<String> from, List<String> named) throws EngineException{
		
		pragma(q);

		if (q.isUpdate()){
			log(Log.UPDATE, q);
			return synUpdate(q, from, named);
		}
		else {
			Mappings lMap =  synQuery(q, map);

			if (q.isConstruct()){
				// construct where
				construct(lMap);
			}
			log(Log.QUERY, q, lMap);
			return lMap;
		}
	}
	
	Mappings synQuery(Query query, Mapping m) {
		try {
			readLock();
			return query(query, m);
		}
		finally {
			readUnlock();
		}
	}
	
	void log(int type, Query q){
		Graph g = getGraph();
		if (g != null){
			g.log(type, q);
		}
	}
	
	void log(int type, Query q, Mappings m){
		Graph g = getGraph();
		if (g != null){
			g.log(type, q, m);
		}
	}
	
	
	
	Mappings synUpdate(Query query,  List<String> from, List<String> named) throws EngineException{
		try {
			writeLock();
			return update(query, from, named);
		}
		finally {
			writeUnlock();
		}
	}
	
	
	Mappings update(Query query,  List<String> from, List<String> named) throws EngineException{
		complete(from);
		ManagerImpl man = ManagerImpl.create(this, from, named);
		UpdateProcess up = UpdateProcess.create(man);
		up.setDebug(isDebug());
		Mappings lMap = up.update(query);
		lMap.setGraph(getGraph());
		return lMap;
	}
	
	
	void complete(List<String> from){
		if (from != null){
			// add the default graphs where insert or entailment may have been done previously
			for (String src : Entailment.GRAPHS){
				if (! from.contains(src)){
					from.add(src);
				}
			}		
		}
	}
	
	

	
	Mappings synQuery(ASTQuery ast){
		try {
			readLock();
			return super.query(ast);
		}
		finally {
			readUnlock();
		}
	}
	

	
	/**
	 * Called by Manager (delete/insert operations)
	 */
	Mappings update(ASTQuery ast, List<String> from, List<String> named) {
		Mappings lMap = super.query(ast, from, named);
		Query q = lMap.getQuery();
		
		// PRAGMA: update can be both delete & insert
		if (q.isDelete()){
			delete(lMap, from, named);
		}
		if (q.isConstruct()){ 
			// insert
			construct(lMap);
		}
		
		return lMap;
	}
	

		
		
	Mappings sparqlQueryUpdate(String squery, List<String> from, List<String> named, int entail) throws EngineException{
		getEvaluator().setMode(Evaluator.SPARQL_MODE);

		if (entail != STD_ENTAILMENT){
			if (from == null){
				from = new ArrayList<String>();
			}
			complete(from);
		}
		
		if (from != null && named == null){
			named = new ArrayList<String>();
			named.add("");
		}
		else if (from == null && named != null){
			from = new ArrayList<String>();
			from.add("");
		}
		Mappings map =  query(squery, null, from, named);
		if (! map.getQuery().isCorrect()){
			map.clear();
		}
		return map;
	}
	
	public Graph getGraph(Mappings map){
		return (Graph) map.getGraph();
	}
	
	public Graph getGraph(){
		return sGetGraph(getProducer());
	}
				
	static Graph sGetGraph(Producer p){
		Graph g = getGraph(p);
		if (g != null){
			return g;
		}
		else if (p instanceof MetaProducer){
			return getGraph(((MetaProducer)p).getProducer());
		}
		return null;	
	}
	
	static Graph getGraph(Producer p){
		if (p instanceof ProducerImpl){
			return ((ProducerImpl) p).getGraph();
		}
		return null;
	}
	
	
	/**
	 * construct {} where {} 			

	 */
	
	void construct(Mappings lMap){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph gg;
		if (getAST(query).isAdd()){
			Graph g = getGraph();
			gg = cons.insert(lMap, g);
		}
		else {
			gg = cons.construct(lMap);
		}
		lMap.setGraph(gg);
	}
	
	
	void delete(Mappings lMap, List<String> from, List<String> named){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph g = getGraph();
		Graph gg = cons.delete(lMap, g, from, named);
		lMap.setGraph(gg);
	}
	
	
	
	
	
	void pragma(Query query){
		ASTQuery ast = (ASTQuery) query.getAST();
		if (ast!=null && ast.getPragma() != null){
			 PragmaImpl.create(this, query).parse();
		}
	}
	
	
	
	/*************************************************/
	
	private Lock getReadLock(){
		return lock.readLock(); 
	}
	
	private Lock getWriteLock(){
		return lock.writeLock(); 
	}
	
	private void readLock(){
		getReadLock().lock();
	}

	private void readUnlock(){
		getReadLock().unlock();
	}

	private void writeLock(){
		getWriteLock().lock();
	}

	private void writeUnlock(){
		getWriteLock().unlock();
	}


	
}
