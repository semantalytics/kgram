package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class DBPediaPersonsStandaloneTest {

    String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //            + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //                + "     OPTIONAL {?x foaf:mbox ?m}"
            + " FILTER ((?name ~ 'Bobby A') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";

    public DBPediaPersonsStandaloneTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    @Ignore
    public void remoteDBPediaQuery() throws EngineException, MalformedURLException, IOException {

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep1.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep2.rdf");

        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/338K/2-stores/persondata.1.rdf");
        File rep2 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/338K/2-stores/persondata.2.rdf");
        engine.load(rep1.getAbsolutePath());
        engine.load(rep2.getAbsolutePath());

        QueryExecDQP exec = QueryExecDQP.create(engine);

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlQuery);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        GraphEngine gEng = (GraphEngine) engine;
        System.out.println("Graph size " + gEng.getGraph().size());
        System.out.println("Results size " + res.size());
        String[] variables = res.getVariables();

        for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
            IResult r = en.nextElement();
            HashMap<String, String> result = new HashMap<String, String>();
            for (String var : variables) {
                if (r.isBound(var)) {
                    IResultValue[] values = r.getResultValues(var);
                    for (int j = 0; j < values.length; j++) {
                        System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                    }
                } else {
                    //System.out.println(var + " = Not bound");
                }
            }
        }
        System.out.println(sw.getTime() + " ms");
    }
}
