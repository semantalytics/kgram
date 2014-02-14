package junit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.xml.sax.SAXException;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.util.MappingComparator;

public class W3C {
	
	final static String local = "/home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/sparql11-test-suite/";
	static final String www = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/";
	static final String data = www;
	
	@Test
	public void test(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		DatatypeMap.setSPARQLCompliant(true);
		exec.setSPARQLCompliant(true);
		exec.getEvaluator().setMode(Evaluator.SPARQL_MODE);

		Load load = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		String q = ql.read(data + "functions/concat02.rq");
		System.out.println(q);
		SPARQLResult res = SPARQLResult.create(Graph.create());
		Mappings m = null;
		try {
			m = res.parse(data + "functions/concat02.srx");
			System.out.println("W3C Result: ");
			System.out.println(m);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			load.loadWE(data + "functions/data2.ttl");
		} catch (LoadException e) {
			e.printStackTrace();
		}
		
		String init = "insert data {" +
				"<http://test.fr/André> rdfs:comment 'Jérôme & \" test    '" +
				"}";
		
		g.init();
		
		
		String qq = "select * where {" +
				"?x ?p ?y }";
		
		try {
			
			System.out.println(g.display());
			exec.query(init);

			Mappings map = exec.query(q);
			
		
			System.out.println("KGRAM Result: ");

			System.out.println(map);
			
//			ResultFormat f = ResultFormat.create(map);
//			System.out.println(f);
			
//			MappingComparator test = MappingComparator.create();
//			test.validate(map, m);
			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
		System.out.println("français");
		System.out.println(System.getProperty("file.encoding"));
		
	}

}
