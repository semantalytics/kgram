package fr.inria.edelweiss.kgdqp.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.CompileService;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.*;
import java.util.logging.Level;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

//import fr.inria.wimmics.sparql.soap.client.SparqlResult;
//import fr.inria.wimmics.sparql.soap.client.SparqlSoapClient;
/**
 * Implements service expression There may be local QueryProcess for some URI
 * (use case: W3C test case) Send query to sparql endpoint using HTTP POST query
 * There may be a default QueryProcess
 *
 * TODO: check use same ProducerImpl to generate Nodes ?
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderWSImpl implements Provider {

    private static Logger logger = Logger.getLogger(ProviderWSImpl.class);
    HashMap<String, QueryProcess> table;
    QueryProcess defaut;
    CompileService compiler;

    ProviderWSImpl() {
        table = new HashMap<String, QueryProcess>();
        compiler = new CompileService();
    }

    public static ProviderWSImpl create() {
        return new ProviderWSImpl();
    }

    /**
     * Define a QueryProcess for this URI
     */
    public void add(String uri, Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        table.put(uri, exec);
    }

    /**
     * Define a default QueryProcess
     */
    public void add(Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        defaut = exec;
    }

    /**
     * If there is a QueryProcess for this URI, use it Otherwise send query to
     * spaql endpoint If endpoint fails, use default QueryProcess if it exists
     */
    @Override
    public Mappings service(Node serv, Exp exp, Mappings lmap, Environment env) {
        Query q = exp.getQuery();

        QueryProcess exec = table.get(serv.getLabel());

        if (exec == null) {

            Mappings map = send(serv, q, lmap, env);
            if (map != null) {
                return map;
            }

            if (defaut == null) {
                map = Mappings.create(q);
                if (q.isSilent()) {
                    map.add(Mapping.create());
                }
                return map;
            } else {
                exec = defaut;
            }
        }

        ASTQuery ast = exec.getAST(q);
        Mappings map = exec.query(ast);

        return map;
    }

    /**
     * Send query to sparql endpoint using a POST HTTP query
     */
    Mappings send(Node serv, Query q, Mappings lmap, Environment env){
        try {
            Query g = q.getOuterQuery();
            compile(serv, q, lmap, env);	

            ASTQuery ag = (ASTQuery) g.getAST();
            ASTQuery ast = (ASTQuery) q.getAST();

            ast.setDebug(g.isDebug());
            ast.setPrefixExp(ag.getPrefixExp());

            String query = ast.toString();

            if (g.isDebug()) {
                logger.info("** Provider: \n" + query);
            }

            // HTTP endpoint implem : 
            //StringBuffer sb = doPost(serv.getLabel(), query);
            // Web service implem : 
            RemoteProducer rp = RemoteProducerServiceClient.getPort(serv.getLabel());
            String sparqlRes = rp.query(query);
            if (sparqlRes == null) {
                return null;
            } else {
                Mappings maps = parseXML(new StringBuffer(sparqlRes));
                return maps;
            }
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Search select variable of query that is bound in env Generate binding for
     * such variable Set bindings in ASTQuery
     */
//    void bindings(Query q, Environment env) {
//        ASTQuery ast = (ASTQuery) q.getAST();
//        ast.clearBindings();
//        ArrayList<Variable> lvar = new ArrayList<Variable>();
//        ArrayList<Constant> lval = new ArrayList<Constant>();
//
//        for (Node qv : q.getSelect()) {
//            String var = qv.getLabel();
//            Node val = env.getNode(var);
//
//            if (val != null) {
//                lvar.add(Variable.create(var));
//                IDatatype dt = (IDatatype) val.getValue();
//                Constant cst = Constant.create(dt);
//                lval.add(cst);
//            }
//        }
//
//        if (lvar.size() > 0) {
//            ast.setVariableBindings(lvar);
//            ast.setValueBindings(lval);
//        }
//    }

    /**
     * ********************************************************************
     *
     * SPARQL Protocol client
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    Mappings parseXML(StringBuffer sb) throws ParserConfigurationException, SAXException, IOException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = XMLResult.create(p);
        Mappings map = r.parseString(sb.toString());
        return map;
    }

    public StringBuffer doPost(String server, String query) throws IOException {
        URLConnection cc = post(server, query);
        return getBuffer(cc.getInputStream());
    }

    URLConnection post(String server, String query) throws IOException {
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

        URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml, text/xml");
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();

        return urlConn;

    }

    StringBuffer getBuffer(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();

        String str = null;
        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }

        return sb;
    }
//	public String callSoapEndPoint() {
//		SparqlSoapClient client = new SparqlSoapClient();
//		SparqlResult result = client.sparqlQuery("http://dbpedia.inria.fr/sparql", "select ?x ?r ?y where { ?x ?r ?y} limit 100");
//		String stringResult = result.toString();
//		return stringResult;
//	}
//
//	public static void main(String[] args) {
//		ProviderImpl impl = new ProviderImpl();
//		System.out.println(impl.callSoapEndPoint());
//	}

    @Override
    public Mappings service(Node serv, Exp exp, Environment env) {
        return service(serv, exp, null, env);
    }

    @Override
    public void set(String uri, double version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void compile(Node serv, Query q, Mappings lmap, Environment env) {
        // share prefix
        compiler.prepare(q);
        // bindings
        //TODO modif alban nullpointer exception
        if (lmap == null) {
            lmap = Mappings.create(q);
        }
        //end modif alban
        compiler.compile(serv, q, lmap, env, 0, lmap.size());
    }
}