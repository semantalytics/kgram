package junit;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.XSLTQuery;

class TestXSLT {

	//static final String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";

        
	public static void main(String args[]){

		new TestXSLT().process();
	}


	void process(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		try {
			exec.query("insert data {<John> <name> 'John'}");
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xsl = data + "kgraph/copy.xsl";
		String xml = data + "kgraph/test.html";

		XSLTQuery xq = XSLTQuery.create(xsl, exec);
		System.out.println(xq.xslt(xml));
		
	}

	void process(String xml, String xslt){
		try{
			TransformerFactory tFactory = TransformerFactory.newInstance();

			Transformer transformer = 
				tFactory.newTransformer(new StreamSource(xslt));

			transformer.transform(new StreamSource(xml), 
					new StreamResult(System.out));

		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

  

}




