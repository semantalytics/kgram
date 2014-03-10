package test.w3c.rdfa;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import test.w3c.model.IEvaluate;
import test.w3c.model.TestCase;
import test.w3c.model.TestHelper;
import test.w3c.model.TestCaseSet;
import static test.w3c.model.TestType.TEST_TYPE;

/**
 * Implementation of RDFa test case evaluation
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class RDFaTestEvaluate implements IEvaluate {

    private static String query
            = "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "prefix mf:     <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> .\n"
            + "prefix rdft:   <http://www.w3.org/ns/rdftest#> .\n"
            + "prefix test:   <http://www.w3.org/2006/03/test-description#> .\n"
            + "select * where\n"
            + "{\n"
            + "?test mf:name ?name;\n"
            + "rdf:type ?type;\n"
            + "rdfs:comment ?comment;\n"
            + "test:classification ?classif;\n"
            + "mf:result ?result.\n"
            + "?test mf:action ?action. ?action qt:query ?sparql; qt:data ?data."
            + "} \n"
            + "group by ?test order by ?test";

    public static RDFaTestEvaluate create() {
        return new RDFaTestEvaluate();
    }

    @Override
    public TestCaseSet generateTestCases(String manifest, String root) {
        TestCaseSet suite = TestHelper.generateTestSuite(root + manifest, query, TestHelper.CASE_RDFA, TEST_TYPE);

        return suite;
    }

    @Override
    public void run(TestCase tc) {

        RDFaTestCase ttc = (RDFaTestCase) tc;

        boolean result;

        result = validate(ttc.getData(), ttc.getSparql());
        ttc.setRealResult(result);
        ttc.setTested(true);
        ttc.setPassed(tc.getExpectedResult() == tc.getRealResult());

        Assert.assertTrue(ttc.isPassed(), ttc.getName());
    }

    private boolean validate(String data, String sparql) {
        //1. create graph and load data
        Graph graph = Graph.create();
        Load ld = Load.create(graph);
        ld.load(data);

        //2. read sparql statement
        String querys = QueryLoad.create().read(sparql);

        //3. query 
        QueryProcess exec = QueryProcess.create(graph);
        Mappings map;
        boolean realResult = false;
        try {
            map = exec.query(querys);
            if (map.size() != 0) {
                realResult = true;
            }
        } catch (EngineException ex) {
            Logger.getLogger(RDFaTestEvaluate.class.getName()).log(Level.SEVERE, null, ex);
        }

        return realResult;
    }
}
