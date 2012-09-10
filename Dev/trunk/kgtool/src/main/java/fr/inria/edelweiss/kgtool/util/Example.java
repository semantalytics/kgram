package fr.inria.edelweiss.kgtool.util;

import java.io.IOException;

import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;

/**
 * Hello World KGRAM Example
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 * 
 */
public class Example {
	
	static final String RULE = "ftp://ftp-sop.inria.fr/wimmics/soft/rule/rdfs.rul";
	
	public static void main(String[] args){
		new Example().process();
	}
	
	
	void process(){
		// create a graph with RDFS entailment
		// without entailment is:
		// Graph g = Graph.create();
		Graph g = Graph.create(true);
		
		// create a loader
		Load ld = Load.create(g);
		
		ld.load(RDF.RDFS);
		ld.load(RDF.RDF);

		// create a query solver
		QueryProcess exec = QueryProcess.create(g);
		
		// define a SPARQL query
		String q1 = "select * where {?x a rdf:Property}";
						
		try {
			// Execute a query
			Mappings map = exec.query(q1);
			System.out.println(map);
			
			// Exploiting query result
			for (Mapping m : map){
				System.out.println("?x = " + m.getNode("?x"));
			}
			
			// create an XML (select) or RDF (construct) result format 
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			
			// load a construct-where rule base
			ld.load(RULE);
			// get the rule engine
			RuleEngine re = ld.getRuleEngine();
			// perform entailment with inference rules
			re.process();
			
			// get rule entailment from graph kg:rule
			String q2 = "select * from kg:rule where {?x ?p ?y}";
			map = exec.query(q2);
			System.out.println(map);
			
			// get RDFS entailment from graph kg:entailment
			String q3 = "select * from kg:entailment where {?x ?p ?y}";
			map = exec.query(q3);
			System.out.println(map);
			
			// SPARQL update
			String update = 
					"delete {?x a rdfs:Resource}" +
					"insert {?x a owl:Thing}" +
					"where  {?x a rdfs:Resource}";
			
			exec.query(update);
			
			// Graph to Turtle
			TripleFormat tf = TripleFormat.create(g);
			System.out.println(tf);
			
			// Graph to Turtle with named graphs
			tf = TripleFormat.create(g, true);
			System.out.println(tf);
			
			try {
				// save graph into a file
				tf.write("/tmp/tmp.ttl");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// using pragma
			String q4 = 
					"select * where {?x rdfs:subClassOf* rdfs:Resource}" +
					"pragma {kg:path kg:expand 5}";
			
			map = exec.query(q4);
			System.out.println(map);

			
		} catch (EngineException e) {
			e.printStackTrace();
		}

	}

}
