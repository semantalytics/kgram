package kgraph;

import java.util.Date;





import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.CoreseBoolean;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.load.RuleLoad;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import fr.inria.edelweiss.kgtool.print.XSLTQuery;

public class TestBiblio {
	
	public static void main(String[] args) throws EngineException{
		new TestBiblio().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String file = "file://" + data + "test.xml";
		String path = "file://" + data;
		
		QuerySolver.definePrefix("geo", "http://rdf.insee.fr/geo/");
		

		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(true);
//		graph.set(Entailment.RDFSRANGE, true);
//		graph.set(Entailment.RDFSSUBCLASSOF, true);
		//graph.set(Entailment.RDFSSUBPROPERTYOF, !true);

		Load loader =  Load.create(graph);
		
		long t1 = new Date().getTime();
		loader.load(data + "bib/src/test.rdf");
		
		QueryLoad ql = QueryLoad.create();
		QueryProcess exec = QueryProcess.create(graph);

		String query;
//		query = ql.read(data + "bib/query/member.txt");
//		exec.query(query);
		 
		query = ql.read(data + "bib/query/tmp.txt");
		System.out.println(query);
		
//		query = "select * where {" +
//				"?p rdfs:subPropertyOf ?y" +
//				"}";

		Mappings map = exec.query(query);
		
		long t2 = new Date().getTime();
		System.out.println(map);

		System.out.println("** Time: " + (t2-t1)/1000.0);
		System.out.println("** Time: " + map.size());

	}

}
