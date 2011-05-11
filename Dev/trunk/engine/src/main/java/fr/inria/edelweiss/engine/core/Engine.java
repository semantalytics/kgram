package fr.inria.edelweiss.engine.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IBRuleEngine;
import fr.inria.acacia.corese.api.IResults;
//import fr.inria.acacia.corese.event.Event;
//import fr.inria.acacia.corese.event.RuleEvent;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;

import fr.inria.edelweiss.engine.core.Backward;
import fr.inria.edelweiss.engine.core.Engine;
import fr.inria.edelweiss.engine.core.ResultManager;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.LBind;
import fr.inria.edelweiss.engine.model.api.RuleBase;
import fr.inria.edelweiss.engine.model.core.BindImpl;
import fr.inria.edelweiss.engine.model.core.QueryImpl;
import fr.inria.edelweiss.engine.tool.api.EventsTreatment;
import fr.inria.edelweiss.engine.tool.core.EventsTreatmentImpl;
import fr.inria.edelweiss.engine.tool.core.RulesTreatmentImpl;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
//import fr.inria.edelweiss.kgramenv.util.QueryExec;

public class Engine implements IBRuleEngine {
	private static Logger logger = Logger.getLogger(Engine.class);
	boolean hasEvent = false;
	/**
     * DATA
     */
	//the base of rules
    private  RuleBase ruleBase;
    
    //the engine server 
    private  IEngine server;
    
    private QuerySolver exec;
    
    private EventsTreatment proc;
    
    
    public static Engine create(){
    	Engine engine = new Engine();
    	return engine;
    }
    
    public static Engine create(IEngine e){
    	Engine engine = new Engine();
    	engine.setEngine(e);
    	return engine;
    }
    
    public static Engine create(QuerySolver exec){
    	Engine engine = new Engine();
    	engine.exec = exec;
    	return engine;
    }

    
    public void setEngine(IEngine server){
    	this.server = server;
    }
    
    
    /**
     * Load rule documents into the rule base
     *
     */
    public void load(String path){
    	List<String> files = new ArrayList<String>();
    	files.add(path);
		ruleBase = new RulesTreatmentImpl().createRules(ruleBase,  files);		
    }
    
    public void load(List<String> lpath){
		ruleBase = new RulesTreatmentImpl().createRules(ruleBase,  lpath);		
    }
    
    public IResults SPARQLProve(String squery){
    	return query(squery);
    }

    
    public ResultManager query(String squery){
      	ASTQuery ast = parse(squery);
    	if (ast == null) return null;
    	return query(ast);
    }
    
    public ResultManager query(ASTQuery ast){
   	    if (ruleBase == null){
    		logger.error("** Backward Engine: RuleBase is empty");
    		return null;
    	}
 
//   	    if (server!=null){
//   	    	hasEvent = server.getEventManager().handle(Event.RULE);
//   	    }
//    	if (hasEvent){
//    		RuleEvent re = RuleEvent.create(Event.RULE_START, ast);
//    		server.getEventManager().send(re);
//    	}
    	
    	//proc = new EventsTreatmentImplSave(server);
    	//if (exec == null) exec = QueryExec.create(server);
    	proc = new EventsTreatmentImpl(exec);

    	Backward backward = new Backward(ruleBase, proc);
    	
   	    if (server!=null){
   	    	//backward.setEventManager(server.getEventManager());
   	    }
   	    
    	QueryImpl query = new QueryImpl(ast);
    	query.setVariables(exec.compile(ast).getVariables());
    	
    	Bind bind = new BindImpl(); 
 
    	LBind lBind = backward.prove(query, bind); 
    	
//      	if (hasEvent){
//    		RuleEvent re = RuleEvent.create(Event.RULE_END, ast);
//    		server.getEventManager().send(re);
//    	}
      	
      	ResultManager res = ResultManager.create(lBind, query);
		
		return res;
    }
    
    
    ASTQuery parse(String queryString){
    	try {
    		// parse a query
    		//ASTQuery ast=server.parse(queryString);
    		ASTQuery ast = ASTQuery.create(queryString);
    		ast.setSPARQL1(true);
    		ParserSparql1.create(ast).parse();
    		// expansion of prefix
    		ast = ast.expand();
    		logger.debug("Prove: \n" + ast + "\n");
    		// create the query containing the body of the SPARQL query
   		return ast;

    	} catch (EngineException e) {
    		System.out.println("Parse error: "+queryString);
    		System.out.println(e);
    	}
    	return null;
    }
    

  
}
