package kgraph;

import java.util.Date;


import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.RuleLoad;

public class TestRule2 {

	public static void main(String[] args){
		new TestRule2().process();
	}

	void process(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String file = "file://" + data + "test.xml";

		String path = "file:///home/corby/workspace/coreseV2/src/test/resources/data";

		QuerySolver.defaultNamespaces(
				"data "  + path + "/comma/ " +
				"data2 " + path + "/comma/data2/  " +
				"data1 " + path + "/comma/data/  " +
		"c http://www.inria.fr/acacia/comma#");

		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(true);
		Graph onto = Graph.create(true);

		graph.set(RDFS.RANGE, true);
		graph.set(RDFS.SUBCLASSOF, !true);
		graph.set(RDFS.SUBPROPERTYOF, !true);
		Load loader =  Load.create(graph);
		Load ld =  Load.create(onto);

		long t1 = new Date().getTime();
		ld.load(data + "kgraph/rdf.rdf", RDF.RDF);
		ld.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
		//		loader.load(data + "meta.rdfs");
		ld.load(data + "comma/comma.rdfs");
		
		loader.load(data + "comma/testrdf.rdf");
		loader.load(data + "comma/model.rdf");
		loader.load(data + "comma/data");
		//loader.load(data + "comma/data2");

		//loader.load(data + "tmp2.rdf");


		//		for (int i=2; i<=10; i++){
		//			System.out.println("** Load: " + i);
		//			loader.load(data + "comma/comma" + i);
		//		}


		//System.out.println(graph.getIndex());

		//System.out.println(graph.getInference().display());		
		long t2 = new Date().getTime();
		System.out.println(graph);
		System.out.println((t2-t1) / 1000.0 + "s");

		
		QueryProcess qp = QueryProcess.create(onto);
		qp.add(graph);
		Graph ng = Graph.create();
		
		RuleEngine engine = RuleEngine.create(ng, qp);
		RuleLoad rl = RuleLoad.create(engine);
		rl.load(data + "kgraph/meta.rul");	
		//ld.load(data + "kgraph/meta2.rul");	
		//ld.load(data + "kgraph/kgraph.rul");	

		String query = 
			"select distinct ?g where {" +
			"graph ?g {?x rdf:type ?c}" +
			"}";
//6.681s
		// 43845
		// 43861
		t1 = new Date().getTime();
		engine.setDebug(!true);
		int count = engine.process();
		t2 = new Date().getTime();

		QueryProcess exec = QueryProcess.create(ng);
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(ng.getIndex());
			System.out.println(count);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		System.out.println((t2-t1) / 1000.0 + "s");
	}


	//	public IDatatype equalsIgnoreAccent(Object o1, Object o2){
	//		IDatatype dt1 = (IDatatype) o1;
	//		IDatatype dt2 = (IDatatype) o2;
	//		boolean b = StringHelper.equalsIgnoreAccent(dt1.getLabel(), dt2.getLabel());
	//		if (b) return CoreseBoolean.TRUE;
	//		return CoreseBoolean.FALSE;
	//	}


	void trace(Graph graph){
		System.out.println(graph);
		//		graph.init();
		//System.out.println(graph.getIndex());
		int n = 0;
		//		for (Entity ent : graph.getIndex().get(graph.getNode(RDF.RDFTYPE))){
		//			System.out.println(ent);
		//			if (n++>50) break;
		//		}
	}

}
