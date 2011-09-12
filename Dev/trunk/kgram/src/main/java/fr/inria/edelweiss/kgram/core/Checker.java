package fr.inria.edelweiss.kgram.core;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 * Query Type Checker
 * 
 * Check occurrence of properties in Producer
 * Check if class/property are defined in Producer 
 * - p rdf:type rdf:Property
 * - c rdf:type rdfs:Class
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Checker {
	
	static Logger logger = Logger.getLogger(Checker.class);
	
	Eval eval;
	Producer producer;
	Matcher matcher;
	Query query;
	
	Checker(Eval e){
		eval = e;
		producer = e.getProducer();
		matcher  = e.getMatcher();
	}
	
	public static Checker create(Eval e){
		return new Checker(e);
	}
	
	void check(Query q){
		query = q;
		check(null, q.getBody(), eval.getMemory());
	}
	
	
	void check(Node gNode, Exp exp, Environment env){
		
		switch (exp.type()){
		
		case Exp.EDGE:
			edge(gNode, exp, env);
			break;
			
			
		case Exp.QUERY:
			check(gNode, exp.getQuery().getBody(), env);
			break;

		default:
			for (Exp ee : exp.getExpList()){
				check(gNode, ee, env);
			}
		}
			
			
	}
	
	/**
	 * Check occurrence of edge
	 * If edge has an associated query, check class/property definition in ontology
	 */
	void edge(Node gNode, Exp exp, Environment env){
		Edge edge = exp.getEdge();
		boolean exist = false, match = false, define = false;

		for (Entity ent : producer.getEdges(gNode, query.getFrom(gNode), edge, env)){

			if (ent != null){
				exist = true;
				if (matcher.match(edge, ent.getEdge(), env)){
					match = true;
					break;
				}
			}
		}
		
		Query q = query.get(edge);
		if (q != null){
			Eval ee = Eval.create(producer, eval.getEvaluator(), matcher);
			Mappings map = ee.query(q);
			define = map.size()>0;
			report(edge, exist, match, define);
		}
		else {
			report(edge, exist, match);
		}
	}

	
	void report(Edge edge, boolean exist, boolean match, boolean define){
		logger.info("Edge: " + edge + ": " + exist + " " + match + " " + define);
	}
	
	void report(Edge edge, boolean exist, boolean match){
		logger.info("Edge: " + edge + ": " + exist + " " + match);
	}
	
	void report(Edge edge, boolean exist){
		logger.info("Defined: " + edge + ": " + exist);
	}
	
	
	
	
	

}
