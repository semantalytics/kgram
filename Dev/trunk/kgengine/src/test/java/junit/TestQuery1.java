package junit;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Join;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.StatListener;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.QueryGraph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgtool.load.BuildImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import fr.inria.edelweiss.kgraph.rdf.*;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;


import static org.junit.Assert.assertEquals;

/**
 * 
 * 
 */
public class TestQuery1 {
	
	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	static String test = "/home/corby/workspace/coreseV2/text/";
	static String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
	static String text = "/home/corby/workspace/kgengine/src/test/resources/text/";

	static Graph graph;
	
	@BeforeClass
	static public void init(){
		QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

		graph = Graph.create(true);
		graph.setOptimize(true);
		
		Load ld = Load.create(graph);
		init(graph, ld);
	}
	
	static  void init(Graph g, Load ld){
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");
	}
	
	
	Graph getGraph(){
		return graph;
	}
	
	
	
	Graph graph(){
		Graph graph = Graph.create(true);
		graph.setOptimize(true);
		
		Load ld = Load.create(graph);		
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");
		return graph;
	}
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void test1(){
		String query = "select check where {" +
				"?x rdf:type c:Person ;" +
				"c:FirstName 'John' ;" +
				"c:name ?n" +
				"}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", true, true);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}

	@Test
	public void test2(){
		String query = "select more * (kg:similarity() as ?sim) where {" +
				"?x rdf:type c:Engineer " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type c:WebPage" +
				"}" +
				"order by desc(?sim)";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");
			double sim = dt.getDoubleValue();
			
			assertEquals("Result", sim, .84, 1e-2);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	@Test
	public void test2b(){
		String query = "select more * (kg:similarity() as ?sim) where {" +
				"?x rdf:type ?c1 filter(kg:similarity(?c1, c:Engineer) > .5) " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type ?c2 filter(kg:similarity(?c2, c:WebPage) > .4)" +
				"}" +
				"order by desc(?sim)";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			
			assertEquals("Result", 9, map.size());
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	@Test
	public void test2c(){
		String query = "select  (kg:similarity(c:Person, c:Document) as ?sim) where {}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");
			double sim = dt.getDoubleValue();
			
			assertEquals("Result", sim, .16, 1e-2);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test3(){
		String query = "select  * (kg:similarity() as ?sim) where {" +
				"?x rdf:type c:Engineer " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type c:WebPage" +
				"}" +
				"order by desc(?sim)" +
				"pragma {kg:match kg:mode 'general'}";
		
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?x");
			
			assertEquals("Result", dt.getLabel(),  
					"http://www-sop.inria.fr/acacia/personnel/Fabien.Gandon/");
			
			assertEquals("Result", 39, map.size());
	
	
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test4(){
		Load ld = Load.create(Graph.create());
		try {
			ld.loadWE("gogo.rdf");
			assertEquals("Result", false, true);
		} catch (LoadException e) {
			System.out.println(e);
			assertEquals("Result", e, e);
		}
		try {
			ld.loadWE(data + "comma/fail.rdf");
			assertEquals("Result", false, true);
		} catch (LoadException e) {
			System.out.println(e);
			assertEquals("Result", e, e);
		}
	}
	
	
	
	
	@Test
	public void test5(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 1, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test6(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String drop = "drop graph kg:entailment";
		
		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			exec.query(drop);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 0, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test7(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String drop = "drop graph kg:entailment";
		String create = "create graph kg:entailment";

		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			exec.query(drop);
			exec.query(create);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 1, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test8(){

		String query = "select  *  where {" +
				"?x c:hasCreated ?doc" +
				"} " +
				"group by any " +
				"order by desc(count(?doc))" +
				"pragma {kg:kgram kg:list true}" ;
				
		try {
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			assertEquals("Result", 3, map.size()); 	
			Mapping m = map.get(0);
			assertEquals("Result", 2, m.getMappings().size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test9(){
		
		Graph g1 = Graph.create(true);
		Graph g2 = Graph.create(true);				

		String query = "select  *  where {" +
				"?x rdf:type ?t; c:name ?n" +
				"} " ;
				
		try {
			QueryProcess e1 = QueryProcess.create(g1);
			QueryProcess e2 = QueryProcess.create(g2);
			QueryProcess exec = QueryProcess.create(g1);
			exec.add(g2);

			e1.query("insert data {<John> rdf:type c:Person}");
			e2.query("insert data {<John> c:name 'John'}");
			
			Mappings map = exec.query(query);
			assertEquals("Result", 1, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test10(){
				
		String query = "select  *  where {" +
				"select (unnest(kg:sparql('select * where {?x rdf:type c:Person; c:hasCreated ?doc}')) as (?x, ?doc)) where {}" +
				"} " ;
				
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			
			Mappings map = exec.query(query);
			assertEquals("Result", 9, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	
	@Test
	public void test11(){
				
		String query = 
		"select * (count(?doc) as ?c)" +
		"(kg:setObject(?x, ?c) as ?t)" +
		"where {" +
		"?x c:hasCreated ?doc" +
		"" +
		"}" +
		"group by ?x" ;
		
		String query2 = 
			"select distinct ?x" +
			"(kg:getObject(?x) as ?v)" +
			"where {" +
			"?x c:hasCreated ?doc filter(kg:getObject(?x) > 0)" +
			"}" +
			"order by desc(kg:getObject(?x))";
			
		
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			
			exec.query(query);
			Mappings map = exec.query(query2);

			assertEquals("Result", 3, map.size()); 	
			
			IDatatype dt = getValue(map, "?v");
			
			assertEquals("Result", 2, dt.getIntegerValue()); 		

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test111(){
				
		String query = 
		"select * (count(?doc) as ?c)" +
		"(kg:setProperty(?x, 0, ?c) as ?t)" +
		"where {" +
		"?x c:hasCreated ?doc" +
		"" +
		"}" +
		"group by ?x" ;
		
		String query2 = 
			"select distinct ?x" +
			"(kg:getProperty(?x, 0) as ?v)" +
			"where {" +
			"?x c:hasCreated ?doc filter(kg:getProperty(?x, 0) > 0)" +
			"}" +
			"order by desc(kg:getProperty(?x, 0))";
			
		
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			
			exec.query(query);
			Mappings map = exec.query(query2);

			assertEquals("Result", 3, map.size()); 	
			
			IDatatype dt = getValue(map, "?v");
			
			assertEquals("Result", 2, dt.intValue()); 		

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test12(){
				
		String query = "select debug *  where {" +
				"?x rdf:type ?class; c:hasCreated ?doc}"  ;
				
		try {
		
			QueryProcess.setSort(true);			
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			QueryProcess.setSort(false);
			
			assertEquals("Result", 22, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test13(){
				
		String query = "select  *  where {" +
				"?x rdf:type ?class; c:hasCreated ?doc}"  ;
				
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			StatListener el = StatListener.create();
			exec.addEventListener(el);
			Mappings map = exec.query(query);
			//System.out.println(el);
			assertEquals("Result", 22, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	
	@Test
	public void test14(){
				
		String query = "select  *  where {" +
				"?x rdf:type c:Person; c:hasCreated ?doc " +
				"?doc rdf:type/rdfs:subClassOf* c:Document " +
				"c:Document rdfs:label ?l ;" +
				"rdfs:comment ?c" +
				"}"  ;
				
		try {
			
			Graph g = Graph.create(true);
			Load ld = Load.create(g);
			//ld.setBuild(new MyBuild(g));
						
			init(g, ld);
			
			QueryProcess exec = QueryProcess.create(g);
			Mappings map = exec.query(query);
			assertEquals("Result", 68, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
		
		
		
		
	}
	
	
	
	@Test
	public void test15(){
				
		String query = "select  (max(kg:depth(?x)) as ?max)  where {" +
				"?x rdfs:subClassOf ?sup" +
				"}"  ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 13, dt.intValue()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test16(){
				
		String query = "select  * (kg:number() as ?num)  where {" +
				"?x c:hasCreated ?doc " +
				"}"  ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			Mapping m = map.get(map.size()-1);
			IDatatype dt = datatype(m.getNode("?num"));
			System.out.println(map);
			assertEquals("Result", map.size(), dt.getIntegerValue()+1); 	
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	
	
	@Test
	public void test17(){
		
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		ld.load(data + "comma/comma.rdfs");
		
		QueryProcess exec = QueryProcess.create(g);
		String query = "select (kg:similarity(c:Person, c:Document) as ?sim) {}";
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");

			assertEquals("Result", true, dt.getDoubleValue()<0.5); 	

			String update = "insert data {c:Human rdfs:subClassOf c:Person}";
			exec.query(update);
			
	//		assertEquals("Result", null, g.getClassDistance()); 	

			map = exec.query(query);
			IDatatype sim = getValue(map, "?sim");
			
			assertEquals("Result", dt, sim); 	

			
		} catch (EngineException e) {
			assertEquals("Result", true, e); 	
		}
		
	}
	
	
	@Test
	public void test18(){
		String query = "select * where {" +
				"c:Person rdfs:subClassOf+ :: $path ?c " +
				"graph $path {?a ?p ?b}" +
				"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 28, map.size()); 	
		} catch (EngineException e) {
			assertEquals("Result", 28, e); 	
		}

	}
	
	@Test
	public void test19(){
		String query = "select * " +
				"(pathLength($path) as ?l) (count(?a) as ?c) where {" +
				"?x c:isMemberOf+ :: $path ?org " +
				"graph $path {?a ?p ?b}" +
				"}" +
				"group by $path";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 99, map.size()); 
			
			for (Mapping mm : map){
				IDatatype ldt = getValue(mm, "?l");
				IDatatype lc  = getValue(mm, "?c");

				assertEquals("Result", ldt, lc); 
			}
			
		} catch (EngineException e) {
			assertEquals("Result", 99, e); 	
		}

	}
	
	
	
	@Test
	public void test20(){
		String query = 
			"prefix ext: <function://junit.TestQuery> " +
			"select (ext:fun(?fn, ?ln) as ?res) where {" +
			"?x c:FirstName ?fn ; c:FamilyName ?ln" +
			"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 23, map.size()); 
			
			for (Mapping mm : map){
				IDatatype dt1 = getValue(mm, "?fn");
				IDatatype dt2  = getValue(mm, "?ln");
				IDatatype dt3  = getValue(mm, "?res");

				assertEquals("Result", dt3.getLabel(), concat(dt1, dt2)); 
			}
			
		} catch (EngineException e) {
			assertEquals("Result", 23, e); 	
		}

	}
	
	
	
	@Test
	public void test21(){
		String query = 
			
			"select  * where {" +
			"?x c:FirstName 'Olivier' " +
			"filter(kg:contains('é', 'e')) " +
			"filter(kg:contains('e', 'é')) " +
			"filter(kg:equals('e', 'é')) " +
			"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 2, map.size()); 
			
		} catch (EngineException e) {
			assertEquals("Result", 2, e); 	
		}

	}
	
	
	
	
	@Test
	public void test22(){
				
		String query = 
			"select  " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {?a ?p ?b}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 61, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test23(){
				
		String query = 
			"select  " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {{c:Toto ?p ?b} union {c:Engineer ?p ?b}}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 17, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	@Test
	public void test24(){
				
		String query = 
			"select debug " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {?a ?p ?b filter(?a = c:Engineer)}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 17, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	@Test
	public void test25(){
				
		String query = 
			"select  " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {optional{c:Engineer ?p ?b} filter(! bound(?b))}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 0, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test26(){
				
		String query = 
			"select  " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {optional{c:Toto ?p ?b} filter(! bound(?b))}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 17, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test27(){
				
		String query = 
			"select  " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {{c:Engineer ?p ?b} minus {?a ?p c:Engineer}}" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 17, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test28(){
				
		String query = 
			"select debug " +
			"where {" +
			"c:Engineer rdfs:subClassOf+ :: $path ?y " +
			"graph $path {c:Engineer ?p ?b} " +
			"?x rdf:type c:Engineer " +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 119, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	@Test
	public void test29(){
				
		String query = 
			"select debug " +
			"where {" +
			"?class rdfs:subClassOf+ :: $path c:Person " +
			"graph $path {?a ?p c:Person} " +
			"?x rdf:type/rdfs:subClassOf+ :: $path2 ?class " +
			"graph $path2 {?x rdf:type ?c } " +
			"?x c:FirstName ?n " +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 43, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	
	@Test
	public void test30(){
				
		String query = 
			"select  " +
			"(pathLength($path) as ?l) " +
			"(max(?l, groupBy(?x, ?y)) as ?m) " +
			"(max(?m) as ?max) " +
			"where {" +
			"?x rdfs:subClassOf+ :: $path ?y" +			
			"}" ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 13, dt.getIntegerValue()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	
	@Test
	public void test31(){
		String query = "select (count(?l) as ?c1) " +
				"(count(distinct ?l) as ?c2) " +
				"(count(distinct self(?l)) as ?c3) " +
				"where {" +
				"?x rdfs:label ?l" +
				"}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt1 = getValue(map, "?c1");
			IDatatype dt2 = getValue(map, "?c2");
			IDatatype dt3 = getValue(map, "?c3");

			assertEquals("Result", 1406, dt1.getIntegerValue());
			assertEquals("Result", 1367, dt2.getIntegerValue());
			assertEquals("Result", 1367, dt3.getIntegerValue());

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test32(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John>  <value> 1, 2 ." +
				"<Jack>  <value> 3, 4 ." +
				"<James> <value> 1, 2 ." +
				"}";
		
		String query = "select  (group_concat(distinct ?y, ?z) as ?str) where {" +
				"?x <value> ?y, ?z. filter(?y < ?z)" +
				"}";
		
		// TODO: bug with distinct function 
		String query2 = "select  (group_concat(distinct self(?y), self(?z)) as ?str) where {" +
		"?x <value> ?y, ?z. filter(?y < ?z)" +
		"}";
		
		try {
			exec.query(update);
			Mappings map = exec.query(query);

			IDatatype dt1 = getValue(map, "?str");
			System.out.println(dt1);
			
			Mappings map2 = exec.query(query2);
			IDatatype dt2 = getValue(map2, "?str");
			System.out.println(dt2);
			
			assertEquals("Result", true, true);

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	
	}
	
	

	@Test
	public void test33(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John> foaf:knows <Jack> " +
				"<Jack> foaf:knows <Jim> " +
				"}" ;
		
		String query = "select * where {" +
				"?x foaf:knows+ :: $path <Jim> " +
				"graph $path { ?a foaf:knows ?b }" +
				"}";
		
		try {
			exec.query(update);
			
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 3, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}		
	}
	
	
	@Test
	public void test34(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John> foaf:knows <Jack> " +
				"<Jack> foaf:knows <Jim> " +
				"}" ;
		
		String query = "select * where {" +
				"?x ^ (foaf:knows+) :: $path <John> " +
				"graph $path { ?a foaf:knows ?b }" +
				"}";
		
		try {
			exec.query(update);
			
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 3, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
	}
	
	
	@Test
	public void test35(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John> foaf:knows <Jack> " +
				"<Jack> foaf:knows <Jim> " +
				"}" ;
		
		String query = "select * where {" +
				"?x  (^foaf:knows)+ :: $path <John> " +
				"graph $path { ?a foaf:knows ?b }" +
				"}";
		
		try {
			exec.query(update);
			
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 3, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
	}
	
	
	
	
	
	@Test
	public void test36(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John> foaf:knows (<a> <b> <c>) " +
				"}" ;
		
		String query = "select * where {" +
				"graph ?g {optional{?x rdf:rest*/rdf:first ?y} " +
				"filter(!bound(?y))  " +
				"}" +
				"}";
		
		try {
			exec.query(update);
			
			Mappings map = exec.query(query);
			//System.out.println(map);
			assertEquals("Result", 0, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
	}
	
	
	
	@Test
	public void test37(){
		Graph g = Graph.create(true);
		QueryProcess exec  = QueryProcess.create(g);
		
		String init = "insert data {<John> <name> 'John'}";
		try {
			exec.query(init);
			
			g.init();
			
//			RDFFormat f = RDFFormat.create(g);
//			System.out.println(f);
												
			assertEquals("Result", 2, g.size());
			
			String query = "select * where {?p rdf:type rdf:Property}";
			
			Mappings res = exec.query(query);
//			System.out.println("** Res: " );
//			System.out.println(res);
			assertEquals("Result", 1, res.size());
			
			
			String update = "delete {?x ?p ?y} where {?x ?p ?y}";
			exec.query(update);
						
			
			String qq = "select * where {?x ?p ?y}";
			res = exec.query(qq);
			assertEquals("Result", 0, res.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	
	@Test
	public void test38(){
		Graph graph = Graph.create();
		QueryProcess exec = QueryProcess.create(graph);
		
		String init = "insert data {" +
				"<John>  <age> 20 " +
				"<John>  <age> 10 " +
				"<James> <age> 30 " +
				"}";
		
		String query = 
				"select distinct (sum(?age) as ?s) where {" +
				"?x <age> ?age" +
				"}" +
				"group by ?x";
		
		
		try {
			exec.query(init);
			Mappings res = exec.query(query);
			assertEquals("Result", 1, res.size());
			assertEquals("Result", 30, getValue(res, "?s").getIntegerValue());


		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	@Test
	public void test39(){
		Graph graph = Graph.create();
		QueryProcess exec = QueryProcess.create(graph);
		
		String init = 
				"insert data {" +
				"<a> foaf:knows <b> " +
				"<b> foaf:knows <a> " +
				"<b> foaf:knows <c> " +
				"<a> foaf:knows <c> " +
				"}";
		
		String query = 
				"select * where {" +
				"<a> foaf:knows+ ?t " +
				"}";
		
		String query2 = 
			"select * where {" +
			"<a> foaf:knows{1,10} ?t " +
			"}" +
			"pragma {kg:path kg:loop false}";
		
		
		try {
			exec.query(init);
			Mappings res = exec.query(query);
			assertEquals("Result", 2, res.size());
			
			exec.setPathLoop(false);
			res = exec.query(query);
			assertEquals("Result", 2, res.size());
			
			

		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	@Test
	public void test40(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix ex: <http://test/> " +
						"prefix foaf: <http://foaf/> " +
						"insert data {" +
						"foaf:knows rdfs:domain foaf:Person " +
						"foaf:knows rdfs:range foaf:Person " +						
						"ex:a foaf:knows ex:b " +
						"ex:b rdfs:seeAlso ex:c " +
						"ex:c foaf:knows ex:d " +
						"ex:d rdfs:seeAlso ex:e " +					
						"ex:a ex:rel ex:b " +
						"ex:b ex:rel ex:c " +
						"ex:c ex:rel ex:d " +					
						"ex:c ex:rel ex:e " +																		
					"}";
				
			String	 query = 
					"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"select * where {" +
					"?x (foaf:knows/rdfs:seeAlso @[a foaf:Person] || ex:rel+)+ ?y" +
					"}";
							 												
				try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 1, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}
	
	
	@Test
	public void test41(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"insert data {" +
					"ex:a foaf:knows ex:b " +
					"ex:b foaf:knows ex:c " +								

					"ex:b rdfs:seeAlso ex:a " +
					"ex:c rdfs:seeAlso ex:b " +
				"}";
			
			 String query = 
				"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"select * where {" +
				"ex:a (  foaf:knows+ || (^rdfs:seeAlso) +) ?y" +

				"}";
							 												
				try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}
	
	
	@Test
	public void test42(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"insert data {" +
					"ex:a foaf:knows ex:b " +
					"ex:b foaf:knows ex:c " +
				
					"ex:b rdfs:seeAlso ex:a " +
					"ex:c rdfs:seeAlso ex:b " +
				"}";
			
			 String query = 
				"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"select * where {" +
				"ex:a (  foaf:knows || ^rdfs:seeAlso )+ ?y" +

				"}";
							 												
				try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}
	
	
	@Test
	public void test43(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"insert data {" +
					"ex:a foaf:knows ex:b " +
					"ex:b foaf:knows ex:e " +
					"ex:e foaf:knows ex:c " +

					"ex:b rdfs:seeAlso ex:a " +
					"ex:c rdfs:seeAlso ex:b " +
				"}";
			
			 String query = 
				"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"select * where {" +
				"ex:a ( foaf:knows+ || (^rdfs:seeAlso)+ ) ?y" +

				"}";
							 												
				try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}
	
	
	@Test
	public void test44(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"insert data {" +
					"foaf:knows rdfs:domain foaf:Person " +
					"foaf:knows rdfs:range  foaf:Person " +
					
					"ex:a foaf:knows ex:b " +
					"ex:b foaf:knows ex:c " +

					"ex:a rdfs:seeAlso ex:b " +
					"ex:b rdfs:seeAlso ex:c " +
				"}";
			
			 String query = 
				"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"select * (kg:pathWeight($path) as ?w) where {" +
				"ex:a ( foaf:knows@2 | rdfs:seeAlso@1 )@[a rdfs:Resource] +  :: $path ?x " +
				"filter(kg:pathWeight($path) = 3)" +

				"}";
							 												
				try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 1, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}
	
	@Test
	public void test45(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix c: <http://test/> " +
					"insert data {" +
					"tuple(c:name <John>  'John' 1)" +
					"tuple(c:name <Jim>   'Jim' 1)" +
					"tuple(c:name <James> 'James')" +
					"}" ;
			 
			 String query = 
				"prefix c: <http://test/> " +
				 "select * where {" +
					"graph ?g { tuple(c:name ?x ?n 1) }" +
				 "}";

			 try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 
	}
	
	
	@Test
	public void test451(){			
			
			Graph graph = Graph.create(true);			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix c: <http://test/> " +
					"insert data {" +
					"tuple(c:name <John>  'John' 1)" +
					"tuple(c:name <John>  'John' 2)" +
					"}" ;
			 
			 String query = 
				"prefix c: <http://test/> " +
				 "select * where {" +
					"graph ?g { tuple(c:name ?x ?n ) }" +
				 "}";

			 try {
					exec.query(init);
					Mappings map = exec.query(query);
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 
	}
	
	
	
	@Test
	public void test46(){			
			
			Graph graph = Graph.create(true);	
			Load load  = Load.create(graph);
			load.load(root + "test/test1.ttl");
			load.load(root + "test/test1.rul");

			RuleEngine  re = load.getRuleEngine();
			QueryProcess exec = QueryProcess.create(graph);
			
			 String query = 
					"prefix c: <http://www.inria.fr/test/> " +
					 "select * where {" +
						"graph ?g { tuple(c:name ?x ?n 1) }" +
					 "}";
			 
			 String query2 = 
					"prefix c: <http://www.inria.fr/test/> " +
					 "select * where {" +
						"graph ?g { " +
						"tuple(c:name ?x ?n 1) " +
						"tuple(c:name ?y ?m 2) " +
						"}" +
					 "}";
			 
			 String query3 = 
					"prefix c: <http://www.inria.fr/test/> " +
					 "select * where {" +
						"graph ?g { " +
						"tuple(c:name ?x 'JohnJohn' 1) " +
						"tuple(c:name ?x  'John' ?v)" +
						"}" +
					 "}";
			 
			 
			 String query4 = 
					"prefix c: <http://www.inria.fr/test/> " +
					 "construct {" +
						"tuple(c:name ?x 'JohnJohn' ?y) " +
					 "} where {" +
						"graph ?g { " +
						"tuple(c:name ?x 'JohnJohn' ?y) " +
						"tuple(c:name ?x  'John' ?v)" +
						"}" +
					 "}";
			 
			 
			 try {
					Mappings map = exec.query(query);
					assertEquals("Result", 1, map.size());
					
					map = exec.query(query2);
					assertEquals("Result", 1, map.size());
					
					re.process();
					
					map = exec.query(query3);
					assertEquals("Result", 1, map.size());
					
					map = exec.query(query4);
					TripleFormat tf = TripleFormat.create(exec.getGraph(map));
					System.out.println(tf);
					

				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	@Test
	public void test47(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix i: <http://www.inria.fr/test/> " +
			"" +
			"insert data {" +
			"<doc> i:contain " +
				"'<doc>" +
				"<person><name>John</name><lname>K</lname></person>" +
				"<person><name>James</name><lname>C</lname></person>" +
				"</doc>'^^rdf:XMLLiteral   " +
			"}";
		
		String query = 		"" +
		"prefix i: <http://www.inria.fr/test/> " +
				"construct {" +
				"[i:name ?name]" +
				"} where {" +
				"select (concat(?n, '.', ?l) as ?name) where {" +
					"?x i:contain ?xml " +
					"{select  (xpath(?xml, '/doc/person') as ?p) where {}}" +
					"{select  (xpath(?p, 'name/text()')  as ?n)  where {}}" +
					"{select  (xpath(?p, 'lname/text()') as ?l)  where {}}" +
				"}}";
		
		
		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			System.out.println(map);			
			assertEquals("Result", 2, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	@Test
	public void test48(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = "insert data {" +
		"<John> foaf:knows <Jack> " +
		"<Jack> foaf:knows <Jim> " +
		"<John> foaf:knows <Jim> " +
		
		"<A> rdfs:seeAlso <B> " +
		"<John> rdfs:seeAlso <Jack> " +
		"}";

		String query = "select * where {" +
		"?x distinct(foaf:knows|rdfs:seeAlso)+ ?y" +
		"}";

		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			assertEquals("Result", 4, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	@Test
	public void test49(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix i: <http://www.inria.fr/test/> " +
			"" +
			"insert data {" +
			"<doc> i:contain " +
				"'<doc>" +
				"<phrase><subject>Cat</subject><verb>on</verb><object>mat</object></phrase>" +
				"<phrase><subject>Cat</subject><verb>eat</verb><object>mouse</object></phrase>" +
				"</doc>'^^rdf:XMLLiteral   " +
			"}";
		
		String query = 		"" +
		"base      <http://www.example.org/schema/>" +
		"prefix s: <http://www.example.org/schema/>" +
		"prefix i: <http://www.inria.fr/test/> " +
				"construct {?su ?pr ?o} " +
				"where {" +
				"select debug * where {" +
					//"?x i:contain ?xml " +
					"{select  (xpath(?xml, '/doc/phrase')   as ?st)  where {}}" +
					"{select  (xpath(?st, 'subject/text()')  as ?s)  where {}}" +
					"{select  (xpath(?st, 'verb/text()')     as ?p)  where {}}" +
					"{select  (xpath(?st, 'object/text()')   as ?o)  where {}}" +
					"{select  (uri(?s) as ?su) (uri(?p) as ?pr)      where {}}" +
				"}}" +
				"values ?xml {<file://" +
				text + "phrase.xml>" +
				"}";
		
		
		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			System.out.println(map);
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			assertEquals("Result", 2, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals("Result", 2, null);
		}
	
	}
	
	
	@Test
	public void test50(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"" +
			"insert data {" +
			":A0 :P :A1, :A2 . " +
			":A1 :P :A0, :A2 . " +
			":A2 :P :A0, :A1"+
			"}";
		
		String query = 		
			"prefix : <http://example.org/>"+
			"select * where { :A0 ((:P)*)* ?X }" ;
		
		
		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			System.out.println(map);
			assertEquals("Result", 3, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	

	@Test
	public void test51(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"" +
			"insert data {" +
			":A0 :P :A1, :A2 . " +
			":A1 :P :A0, :A2 . " +
			":A2 :P :A0, :A1"+
			"}";
		
		String query = 		
			"prefix : <http://example.org/>"+
			"select * where { ?X ((:P)*)* :A1 }" ;
		
		
		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			assertEquals("Result", 3, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	@Test
	public void test52(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"" +
			"insert data {" +
			":a :p :b, :c ." +
			":b :q :d " +
			":c :q :d " +
			":d :p :e " +
			":e :q :f " +
			""+
			"} ";
		
		String query = 		
			"prefix : <http://example.org/>"+
			"select * where { :a (:p/:q)+ ?y }" ;
		
		
		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			assertEquals("Result", 2, map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@Test
	public void test53(){
		String query = "select  * (kg:similarity() as ?sim) where {" +
				"?x rdf:type c:Engineer " +
				"}" +
				"order by desc(?sim)" +
				"pragma {kg:match kg:mode 'strict'}";
		
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			
			assertEquals("Result", 7, map.size());
	
	
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test54(){
		String query = "select * where {" +
				"graph ?g {?s <name> ?o} " +
				"?s <age> ?a" +
				"}";
		Graph graph = Graph.create();
		
		Node g = graph.addGraph("test");
		Node s = graph.addResource("URIJohn");
		Node p = graph.addProperty("age");
		Node o = graph.addLiteral(24);
		Node p2 = graph.addProperty("name");
		Node o2 = graph.addLiteral("John");
		
		Edge e = graph.addEdge(g, s, p, o);
		graph.addEdge(s, p2, o2);

		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			
			assertEquals("Result", 1, map.size());
	
	
		} catch (EngineException ee) {
			assertEquals("Result", true, ee);
		}
	}	
	
	
	
	/**
	 * Two graphs with partial ontology each
	 * Each graph answer with its viewpoint on the ontology
	 */
	@Test
	public void test55(){
		
		
		String o1 = "prefix foaf: <http://foaf.org/>" +
		"insert data {" +
		"foaf:Human rdfs:subClassOf foaf:Person " +
		"}";

		String o2 = "prefix foaf: <http://foaf.org/>" +
		"insert data {" +
		"foaf:Man rdfs:subClassOf foaf:Person " +
		"}";
		
		String init1 = "prefix foaf: <http://foaf.org/>" +
		"insert data {" +
		"<John> a foaf:Human"+
		"}";
		
		String init2 = "prefix foaf: <http://foaf.org/>" +
		"insert data {" +
		"<Jack> a foaf:Man"+
		"}";
		
		
		
		
		String query = "prefix foaf: <http://foaf.org/>" +
				"select * where {" +
				"?x a foaf:Person" +
				"}";

		Graph o  = Graph.create(true);
		Graph g1 = Graph.create(true);
		Graph g2 = Graph.create(true);

		QueryProcess exec1 = QueryProcess.create(g1);
		QueryProcess exec2 = QueryProcess.create(g2);
		
		QueryProcess exec  = QueryProcess.create(g1, true);
		exec.add(g2);
		

		try {
			exec1.query(o1);
			exec1.query(init1);
			
			exec2.query(o2);
			exec2.query(init2);
			
//			exec.query(o1);
//			exec.query(o2);

			
			Mappings map = exec.query(query);
			assertEquals("Result", 2, map.size());
			System.out.println(map);
			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void test56(){
		Graph graph = Graph.create();
		QueryProcess exec = QueryProcess.create(graph);
		exec.setOptimize(true);
		
		String init = "insert data {" +
				"graph <g1> {<John> foaf:knows <Jim> }" +
				"graph <g2> {<Jim> foaf:knows <Jack>}" +
				"}";
		
		String query = "select  * where {" +
				"?x foaf:knows+ ?y " +
				"filter(?y = <Jack> || <John> = ?x)" +
				"}" ;	
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			assertEquals("Result", 4, map.size());

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	@Test
	public void test57(){
		Graph graph = Graph.create();
		QueryProcess.definePrefix("e", "htp://example.org/");
		QueryProcess exec = QueryProcess.create(graph);
		
		RuleEngine re = RuleEngine.create(graph);
		
		String rule = 
			"construct {[a e:Parent; e:term(?x ?y)]}" +
			"where     {[a e:Father; e:term(?x ?y)]}";
		
		String rule2 = 
			"construct {[a e:Father;   e:term(?x ?y)]}" +
			"where     {[a e:Parent;   e:term(?x ?y)]}";
		
		
		String rule3 = 
			"construct {[a e:Parent]}" +
			"where     {[a e:Father]}";
		
		String rule4 = 
			"construct {[a e:Father]}" +
			"where     {[a e:Parent]}";
		
		
		try {
			re.defRule(rule);
			re.defRule(rule2);
		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String init = "insert data {" +
				"[ a e:Father ; e:term(<John> <Jack>) ]" +
				"}";
		
		String query = "select  * where {" +
				//"?x foaf:knows ?z " +
				"[a e:Parent; e:term(?x ?y)]" +
				"}" ;	
		
		try {
			exec.query(init);
			re.setDebug(true);
			re.process();
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 1, map.size());

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void test58(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = "insert data {" +
		"<John> foaf:knows <Jack> " +
		
		"<John> foaf:knows <Jim> " +
		"<Jim> foaf:knows <Jack> " +
		
		
		"}";

		String query = "select * (pathLength($path) as ?l) where {" +
		"?x short(foaf:knows|rdfs:seeAlso)+ :: $path ?y" +
		"}";

		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			assertEquals("Result", 3, map.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	@Test
	public void test59(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = "insert data {" +
		"<John> foaf:knows <Jack> " +
		
		"<John> foaf:name 'Jack' ;" +
		"foaf:age 12 ;" +
		"foaf:date '2012-04-01'^^xsd:date ;" +
		"foaf:knows [] " +
		"}";

		String query = "select * where {?x ?p ?y}";

		try {
			Mappings map =exec.query(init);
			map =exec.query(query);
			XMLFormat f = XMLFormat.create(map);
						
			XMLResult xml = XMLResult.create(exec.getProducer());
			Mappings m = xml.parseString(f.toString());
			System.out.println(m);
			
			assertEquals("Result", 5, map.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	
	@Test
	public void test60(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"select debug * where {" +
				"service <http://fr.dbpedia.org/sparql> {" +
					"?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr " +
				"}" +
			"}";
		
		
		try {
			Mappings map = exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 3, map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	@Test
	public void test601(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"construct {?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr}" +
			" where {" +
				"service <http://fr.dbpedia.org/sparql> {" +
					"?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr " +
				"}" +
			"}";
		
		
		try {
			Mappings map = exec.query(query);
			//ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 3, map.size());
			Graph gg = (Graph) map.getGraph();
			assertEquals("Result", 3, gg.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	@Test
	public void test61(){			
		DatatypeMap.setLiteralAsString(false);
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>" + 
			"select  * where {" +
				"service <http://fr.dbpedia.org/sparql> {"+
				"<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y .}" +
			"}" +
			"pragma {kg:path kg:expand 20}";
		
		
		try {
			Mappings map = exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 15, map.size());
			
			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	

	@Test
	public void test62(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"insert data {" +
				"<John> foaf:age 12 " +
				"<James> foaf:age 20" +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"select * where {" +
				"?x foaf:age ?age" +
			"}";		
		
		try {
			Mappings map = exec.query(init);
			map = exec.query(query);
			assertEquals("Result", 2, map.size());

			exec.filter(map, "?age > 15");
			assertEquals("Result", 1, map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void test63(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"insert data {" +
				"<A> foaf:date '2012-05-10'^^xsd:dateTime " +
				"<B> foaf:date '2012-05-11'^^xsd:dateTime " +
				"<C> foaf:date '2012-05-10T10:20:30'^^xsd:dateTime " +
				"<D> foaf:date '2012-05-10T10:30:30.50'^^xsd:dateTime " +
				"<E> foaf:date '2012-05-10T10:30:30'^^xsd:dateTime " +
				"}";
		
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"select * where {" +
				"?x foaf:date ?date" +
			"}" +
			"order by desc(?date)";		
		
		try {
			Mappings map = exec.query(init);
			map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 5, map.size());
			
			IDatatype dt0 = (IDatatype) map.get(0).getNode("?x").getValue();
			IDatatype dt1 = (IDatatype) map.get(1).getNode("?x").getValue();
			IDatatype dt2 = (IDatatype) map.get(2).getNode("?x").getValue();
			IDatatype dt3 = (IDatatype) map.get(3).getNode("?x").getValue();
			IDatatype dt4 = (IDatatype) map.get(4).getNode("?x").getValue();
			
			assertEquals("Result", "B", dt0.getLabel());
			assertEquals("Result", "D", dt1.getLabel());
			assertEquals("Result", "E", dt2.getLabel());
			assertEquals("Result", "C", dt3.getLabel());
			assertEquals("Result", "A", dt4.getLabel());

			// B D E C A


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	@Test
	public void test64(){
		
		QueryProcess exec = QueryProcess.create(graph);
		
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select (kg:ancestor(c:Event, c:Document) as ?a) where {" +
				""+
				"}";
		
		try {
			Mappings map = exec.query(query);
			
			Node aa = graph.getResource("http://www.inria.fr/acacia/comma#Something");
			Node rr = map.getNode("?a");

			assertEquals("Result", aa.getLabel(), rr.getLabel());

			
			Node n1 = graph.getResource("http://www.inria.fr/acacia/comma#Person");
			Node n2 = graph.getResource("http://www.inria.fr/acacia/comma#Event");
			
			System.out.println("ANC: " + n1);
			System.out.println("ANC: " + n2);
			graph.setClassDistance();
			Node vv = graph.getClassDistance().ancestor(n1, n2);
			assertEquals("Result", aa.getLabel(), vv.getLabel());

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	@Test
	public void test65(){
		Graph g = Graph.create();
		
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(root + "test/iso.ttl");
			ld.loadWE(root + "test/iso.rdf");
			
			ld.loadWE(root + "test/utf.ttl");
			ld.loadWE(root + "test/utf.rdf");
		} catch (LoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String query = "select * where {" +
				"?x ?p ?y . ?z ?q ?y filter(?x != ?z)" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 4, map.size());
			
		} catch (EngineException e) {
			e.printStackTrace();
		}

	
	}
	
	
	@Test
	public void testRelax(){
		Graph g = graph();
					
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"insert data {" +
				"<John> foaf:type c:Researcher " +
				"<John> foaf:knows <Jack> " +
				"<Jack> foaf:type c:Engineer " +
				
				"<John> foaf:knows <Jim> " +
				"<Jim> foaf:type c:Fireman " +
				
				"<e> foaf:type c:Event " +
				"}" ;
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select   more * (kg:similarity() as ?s) where {" +
				"?x foaf:type c:Engineer " +
				"?x foaf:knows ?y " +
				"?y foaf:type c:Engineer" +
			"}" +
			"order by desc(?s) " +
			"pragma {kg:kgram kg:relax (foaf:type)}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 2, map.size());
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	

	
	
	
	@Test
	public void testJoin2(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		String init = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data   {" +
			"graph <g1> {" +
		"<John> foaf:name 'John' " +
		"<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>" +
		"<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>" +
		"}" +

		"graph <g1> {" +		
		"<Jim> foaf:knows <James>" +
		"<Jim> foaf:name 'Jim' " +
		"}" +
		"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select debug * where {" +
			
				"service <http://fr.dbpedia.org/sparql> {" +
					"select * where {" +
					"?x rdfs:label ?n " +
					"filter(regex(?n, '^August'))" +
					"} limit 20" +
				"}" +
				
				"?x foaf:knows ?y " +

				"service <http://fr.dbpedia.org/sparql> {" +
					"select * where {" +
					"?x rdfs:label ?n " +
					"}" +
				"}" +				
			"}" +
			"";
		
		QueryProcess exec = QueryProcess.create(g);
		exec.setSlice(30);
		exec.setDebug(true);

		try {
			exec.query(init);
		
			Mappings map = exec.query(query);
			System.out.println(map);
			
			assertEquals("Result", 2, map.size());
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
	
	}
	
	
	
	/**
	 * Create a Query graph from an RDF Graph
	 * Execute the query
	 * Use case: find similar Graphs (cf Corentin)
	 */
	@Test
	public void testQueryGraph(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"" +
			"insert data {" +
			":a :p :b, :c ." +
			":b :q :d " +
			":c :q :d " +
			":d :p :e " +
			":e :q :f " +
			""+
			"} ";
		
		String cons = 
				"prefix : <http://example.org/> "+
				"" +
				"construct {?x :p []}" +
				"where {?x :p ?y}" ;					
		
		String init2 = 
				"prefix : <http://example.org/> "+
				"" +
				"insert data {" +
				":a :p [] ." +
				"}";
		
		
		try {
			// create a graph
			exec.query(init);
			
			// create a copy where triple objects (values) are Blank Nodes (aka Variables)
			// consider the copy as a Query Graph and execute it
			Mappings map = exec.queryGraph(cons);
										
			assertEquals("Results", 4, map.size());
			
			Graph g2 = Graph.create();	
			QueryProcess exec2 = QueryProcess.create(g2);		
			exec2.query(init2);

			QueryGraph qg = QueryGraph.create(g2);
			QGVisitor vis = new QGVisitor();
			//qg.setVisitor(vis);
			qg.setConstruct(true);
			map = exec.query(qg);									
			
			Graph res = exec.getGraph(map);
			assertEquals("Results", 2, res.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public IDatatype fun(Object o1, Object o2){
		IDatatype dt1 = datatype(o1);
		IDatatype dt2 = datatype(o2);
		String str = concat(dt1, dt2);
		return DatatypeMap.createLiteral(str); 
	}
	
	String concat(IDatatype dt1, IDatatype dt2){
		return dt1.getLabel() + "." + dt2.getLabel();
	}
	
	
	IDatatype getValue(Mapping map, String name){
		return datatype(map.getValue(name));
	}
	
	
	IDatatype getValue(Mappings map, String name){
		Object value = map.getValue(name);
		if (value == null) return null;
		return datatype(value);
	}
	
	IDatatype datatype(Object n){
		return (IDatatype) n;
	}
	
	IDatatype datatype(Node n){
		return (IDatatype) n.getValue();
	}


}
