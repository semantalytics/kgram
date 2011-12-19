package fr.inria.edelweiss.kgramenv.util;


import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.EventListener;
import fr.inria.edelweiss.kgraph.query.QueryProcess;



/**
 * Evaluator of SPARQL query by KGRAM
 * Implement KGRAM on top of Corese with Corese API, lightweight version:
 * IResults SPARQLQuery
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class QueryExec  {
	protected QueryProcess exec;
	protected boolean isListGroup = false, 
	isDebug = false;
	protected ArrayList<EventListener> list;
	

	public QueryExec (){
		list = new ArrayList<EventListener>();
	}

	public static QueryExec create(){
		return new QueryExec();
	}
	
	 /**
	  * Corese implementation
	  */
	public static QueryExec create(IEngine eng){
		GraphEngine engine = (GraphEngine) eng;
		QueryExec qe = new QueryExec();
		qe.add(engine);
		return qe;
	}
		
	/**
	 * Draft with several engine
	 * 
	 * TODO:
	 * add is done in first engine (see constructor in set() )
	 */
	
	public void add(IEngine eng){
		GraphEngine engine = (GraphEngine) eng;
		if (exec == null){
			exec = engine.createQueryProcess();
			//exec.setListGroup(isListGroup);
			exec.setDebug(isDebug);
			for (EventListener el : list){
				exec.addEventListener(el);
			}
		}
		else {
			exec.add(engine.getGraph());
		}
	}
	
	public void definePrefix(String p, String ns){
		QueryProcess.definePrefix(p, ns);
	}
	
	public void setListGroup(boolean b){
		isListGroup = b;
		if (exec!=null) exec.setListGroup(true);
	}
	
	public void setDebug(boolean b){
		isDebug = b;
		if (exec!=null) exec.setDebug(true);
	}
	
	/**
	 * User API query processor
	 */
	
	public IResults SPARQLQuery(String squery) throws EngineException {
		Mappings map =  exec.query(squery);
		QueryResults res = QueryResults.create(map);
		return res;	
		}
	
	public IResults query(String squery) throws EngineException {
		Mappings map =  exec.sparqlQuery(squery);
		QueryResults res = QueryResults.create(map);
		return res;	
		}
	
	public IResults update(String squery) throws EngineException {
		Mappings map =  exec.sparqlUpdate(squery);
		QueryResults res = QueryResults.create(map);
		return res;
	}
	
	public IResults SPARQLQuery(String squery, List<String> from, List<String> named) throws EngineException {
		Mappings map =  exec.query(squery, null, from, named);
		QueryResults res = QueryResults.create(map);
		return res;
	}
		
	public void addEventListener(EventListener el){
		list.add(el);
		if (exec!=null) exec.addEventListener(el);
	}
	
	public IResults SPARQLQuery(ASTQuery ast) throws EngineException {
		Mappings map =  exec.query(ast);
		QueryResults res = QueryResults.create(map);
		return res;	
	}

}
