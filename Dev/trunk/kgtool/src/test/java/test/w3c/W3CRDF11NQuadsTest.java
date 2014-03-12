package test.w3c;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.w3c.model.IEvaluate;
import test.w3c.model.TestCase;
import test.w3c.model.TestHelper;
import test.w3c.model.TestCaseSet;
import static test.w3c.model.TestType.NQNegativeSyntax;
import static test.w3c.model.TestType.NQPositiveSyntax;
import test.w3c.turtle.TurtleTestEvaluate;

/**
 * W3C RDF1.1 N-Quads test (http://www.w3.org/2013/N-QuadsTests )
 *
 * @author Fuqi Song Wimmics inria i3s
 * @date Mar 11 2014
 */
public class W3CRDF11NQuadsTest {

    private static final String web = "http://www.w3.org/2013/N-QuadsTests/";
    private static final String manifest = "manifest.ttl";
    //static final String local = W3CRDF11NQuadsTest.class.getClassLoader().getResource("data").getPath() + "/w3c-n-quands/";
    private static final String head = "W3C RDF1.1 N-Quads test";
    private static TestCaseSet suite = null;
    private static final IEvaluate eval = TurtleTestEvaluate.create();

    @BeforeClass
    public void init() {
        suite = eval.generateTestCases(manifest, web);
        suite.setName(head);
        suite.setUri(web);
        suite.setManifest(manifest);
    }

    @DataProvider
    public static Object[][] data(Method mt) {
        String test = mt.getName();
        List list;
        if ("testNQPositiveSyntax".equals(test)) {
            list = suite.getTestCasesByType(NQPositiveSyntax);
        } else if ("testNQNegativeSyntax".equals(test)) {
            list = suite.getTestCasesByType(NQNegativeSyntax);
        } else {//all test cases
            list = suite.getTests();
        }
        return TestHelper.toObjectArray(list);
    }

    //@Test(dataProvider = "data")
    public void testAll(TestCase tc) {
        eval.run(tc);
    }

    //@Test(dataProvider = "data")
    public void testNQPositiveSyntax(TestCase tc) {
        eval.run(tc);
    }

    //@Test(dataProvider = "data")
    public void testNQNegativeSyntax(TestCase tc) {
        eval.run(tc);
    }

    @AfterClass
    public void report() {
        System.out.println(TestReport.statsString(suite, true, false, true, false));
        //TestReport.toHtml(suite, "/Users/fsong/Downloads/result-nq1103.html", head);
    }
}
