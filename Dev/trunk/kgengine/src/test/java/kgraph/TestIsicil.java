package kgraph;

import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.*;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.XMLFormat;

public class TestIsicil {
	
	
	public static void main(String[] args) throws EngineException{
		new TestIsicil().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

		GraphEngine engine = GraphEngine.create();
		Graph graph = Graph.create();
		Load ld = Load.create(graph);
		
		QueryLoad ql = QueryLoad.create();
		String ud = ql.read(data + "isicil/update.rq");
		String qq = ql.read(data + "isicil/test.rq");
		
		ld.load(data + "isicil/semSNA.rdf");
		
		
		QueryProcess exec = QueryProcess.create(graph);
		
		String query = "";
		
		Mappings map = exec.query(qq);
		
		
		XMLFormat f = XMLFormat.create(map);
		System.out.println(f);

		
	}
	
	
}
