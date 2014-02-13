package test.kgraph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.inria.acacia.corese.cg.datatype.CoreseDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.NodeImpl;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

public class SDK {
	List<Node> lVar = new ArrayList<Node>();
	List<Node> lValue = new ArrayList<Node>();
	
	public static void main(String[] args){
		new SDK().process();
	}

	void process(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		Graph graph = Graph.create();
		Load ld = Load.create(graph);
		
		long t1 = new Date().getTime();

		ld.load(data + "kgraph/sdk.rdf");

		//System.out.println(graph.getInference().display());		
		long t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");

	
		QueryProcess exec = QueryProcess.create(graph);
		//exec.addEventListener(EvalListener.create());

		Edge edge = graph.getEdge(RDFS.RDF + "value", "http://www.inria.fr/acacia/sdk#query", 0);
		String str = edge.getNode(1).getLabel();
		
		
		String query = str;

		try {
			trace(graph);

			t1 = new Date().getTime();
			Mappings lMap = null;
			System.out.println("start");

			Mappings res = null;
			t1 = new Date().getTime();
			
			res = exec.query(query);

			t2 = new Date().getTime();
			//if (lMap.size()<=10) 
			System.out.println(res);
			System.out.println(res.size() + " " + (t2-t1) / 1000.0 + "s");

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	Mapping init(){
		lVar.clear(); 
		lValue.clear(); 

		add("?a1", 2);
		add("?b2", 5);
		add("?c3", 7);

		Mapping map = new Mapping(lVar, lValue);
		return map;
	}
	
	void add(String var, int val){
		lVar.add(var(var));
		lValue.add(value(val));
	}
	
	
	Node var(String var){
		return fr.inria.edelweiss.kgenv.parser.NodeImpl.createVariable(var);
	}
	
	Node value(int n){
//		try {
//			return new NodeImpl(CoreseDatatype.createLiteral(Integer.toString(n), RDFS.xsdinteger, null));
//		} catch (CoreseDatatypeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return null;
	}

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
