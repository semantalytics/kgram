/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class DqpRestTest {

    private String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + "     ?x rdf:type foaf:Person ."
            + "     FILTER (?name ~ 'Bobby')"
            //            + "} LIMIT 100";
            + "} ";
    private static Logger logger = Logger.getLogger(DqpRestTest.class);
    private static int port1 = 9081;
    private static int port2 = 9082;
    private static Process server1 = null;
    private static Server server2;

    public DqpRestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, IOException, InterruptedException {

        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        /////////////// First server in another JVM (through ProcessBuilder)
        ProcessBuilder pb = new ProcessBuilder("java", "-Xmx512m", "-cp",
//                "/Users/gaignard/devKgram/kgserver/target/kgserver-1.0.7-jar-with-dependencies.jar",
                "./target/kgserver-1.0.7-jar-with-dependencies.jar",
                "fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer", "-p", "9081", "&");
        
        pb.redirectErrorStream(true);
        server1 = pb.start();
        Thread.sleep(2000);

        /////////////// Secondserver in that JVM
        URI webappUri1 = EmbeddedJettyServer.extractResourceDir("webapp", true);
        server2 = new Server(port2);

        ServletHolder jerseyServletHolder_s1 = new ServletHolder(ServletContainer.class);
        jerseyServletHolder_s1.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder_s1.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
        Context servletCtx_s1 = new Context(server2, "/kgram", Context.SESSIONS);
        servletCtx_s1.addServlet(jerseyServletHolder_s1, "/*");
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port2 + "/kgram");
        logger.info("----------------------------------------------");

        ResourceHandler resource_handler_s1 = new ResourceHandler();
        resource_handler_s1.setWelcomeFiles(new String[]{"index.html"});
//        resource_handler_s1.setResourceBase("/Users/gaignard/Documents/Dev/svn-kgram/Dev/trunk/kgserver/src/main/resources/webapp");
        resource_handler_s1.setResourceBase(webappUri1.getRawPath());
        ContextHandler staticContextHandler_s1 = new ContextHandler();
        staticContextHandler_s1.setContextPath("/");
        staticContextHandler_s1.setHandler(resource_handler_s1);
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port2);
        logger.info("----------------------------------------------");

        HandlerList handlers_s1 = new HandlerList();
        handlers_s1.setHandlers(new Handler[]{staticContextHandler_s1, servletCtx_s1});
        server2.setHandler(handlers_s1);

        try {
            server2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ///// Data upload
        ClientConfig config = new DefaultClientConfig();
        Client client1 = Client.create(config);
        WebResource service1 = client1.resource(new URI("http://localhost:" + port1 + "/kgram"));
        System.out.println(service1.path("sparql").path("reset").post(String.class).toString());
        MultivaluedMap formData1 = new MultivaluedMapImpl();
        formData1.add("remote_path", "http://nyx.unice.fr/~gaignard/data/persondata.1.rdf");
        service1.path("sparql").path("load").post(formData1);

        Client client2 = Client.create(config);
        WebResource service2 = client2.resource(new URI("http://localhost:" + port2 + "/kgram"));
        System.out.println(service2.path("sparql").path("reset").post(String.class).toString());
        MultivaluedMap formData2 = new MultivaluedMapImpl();
        formData2.add("remote_path", "http://nyx.unice.fr/~gaignard/data/persondata.2.rdf");
        service2.path("sparql").path("load").post(formData2);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server1 != null) {
            server1.destroy();
        }
        if (server2 != null) {
            server2.stop();
            server2.destroy();
            server2 = null;
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void JavaApiTest() throws MalformedURLException, EngineException, URISyntaxException {

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://localhost:" + port1 + "/kgram/sparql"), WSImplem.REST);
        exec.addRemote(new URL("http://localhost:" + port2 + "/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlQuery);
        int dqpSize = map.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        System.out.println("");

        assertEquals(60, map.size());
    }
    
    @Test
    public void RestApiTest() throws MalformedURLException, EngineException, URISyntaxException, LoadException {

        ClientConfig config = new DefaultClientConfig();
        Client client1 = Client.create(config);
        WebResource serviceDQP = client1.resource(new URI("http://localhost:" + port2 + "/kgram/dqp"));

        MultivaluedMap formData1 = new MultivaluedMapImpl();
        formData1.add("endpointUrl", "http://localhost:" + port1 + "/kgram/sparql");
        MultivaluedMap formData2 = new MultivaluedMapImpl();
        formData2.add("endpointUrl", "http://localhost:" + port2 + "/kgram/sparql");
        serviceDQP.path("configureDatasources").post(formData1);
        serviceDQP.path("configureDatasources").post(formData2);

        String sparqlRes = serviceDQP.path("sparql").queryParam("query", sparqlQuery).accept("application/sparql-results+xml").get(String.class);
        int nbResults = StringUtils.countMatches(sparqlRes,"<result>");
        assertEquals(60, nbResults);
    }
}