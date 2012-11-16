/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.strategies.SourceSelectorWS;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizer;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizerFactory;
import fr.inria.edelweiss.kgram.api.core.*;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 * Implementation of the remote producer, acting as web service client for a
 * KGRAM endpoint (kgserver web service).
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteProducerWSImpl implements Producer {

    private static Logger logger = Logger.getLogger(RemoteProducerWSImpl.class);
    private RemoteProducer rp;
    private HashMap<String, Boolean> cacheIndex = new HashMap<String, Boolean>();

    public RemoteProducerWSImpl(URL url) {
        rp = RemoteProducerServiceClient.getPort(url);
    }

    @Override
    public void init(int nbNodes, int nbEdges) {
    }

    @Override
    /**
     * propagate to the server ? nothing
     */
    public void setMode(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms an EDGE request into a simple SPARQL query pushed to the
     * remote producer. Results are returned through standard web services
     * protocol.
     *
     * @param gNode graph variable if it exists, null otherwise
     * @param from "from named <g>" list
     * @param qEdge edge searched for
     * @param env query execution context (current variable values, etc.)
     * @return an iterator over graph entities
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {

        // si gNode != null et from non vide, alors "from named"
        // si gNode == null et from non vide alors "from"

        ArrayList<Entity> results = new ArrayList<Entity>();
        Iterator it = null;

//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createSimpleOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFilterOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createBindingOptimizer();
        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();
        String query = qo.getSparqlQuery(qEdge, env);
        Graph g = Graph.create();

        InputStream is = null;
        try {
//            StopWatch sw = new StopWatch();
//            sw.start();

            if (SourceSelectorWS.ask(qEdge, this, env)) {
//            if (true) {
//                logger.info("sending query \n" + query + "\n" + "to " + rp.getEndpoint());

//  Version no-streaming                
                String sparqlRes = rp.getEdges(query);
                if (sparqlRes != null) {
                    Load l = Load.create(g);
                    is = new ByteArrayInputStream(sparqlRes.getBytes());
                    l.load(is);
//                    logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
                }
//                logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
//                logger.info("Received results  from " + rp.getEndpoint());
//                System.out.println("Received results  from " + rp.getEndpoint());
//                sw.reset();
//                sw.start();                

                // Version streaming 
//                Map<String, Object> reqCtxt = ((BindingProvider) rp).getRequestContext();
//                reqCtxt.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//                reqCtxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//                StreamingDataHandler streamingDh = (StreamingDataHandler) rp.getEdges(query);
//                if (streamingDh != null) {
//                    is = streamingDh.readOnce();
//                    Load l = Load.create(g);
////                    is = new ByteArrayInputStream(sparqlRes.getBytes());
//                    l.load(is);
////                    logger.info("Results (cardinality " + g.size() + ")");
////                    logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
//                }
            } else {
//                logger.info("negative ASK (" + qEdge + ") -> pruning data source " + rp.getEndpoint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return g.getEdges();
    }

    /**
     * UNUSED with kgram (pas de manipulation des sommets)
     *
     * @param gNode
     * @param from
     * @param qNode
     * @param env
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * a priori inutile de le propager cote serveur car il sera declenche par le
     * moteur local (rien en pratique).
     *
     * @param qEdge
     * @param index
     */
    @Override
    public void initPath(Edge qEdge,
            int index) {
    }

    /**
     *
     * @param gNode
     * @param from
     * @param env
     * @param exp inutile dans ce cas la
     * @param index sert au sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp,
            int index) {

        String sparqlPrefixes = "";

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }
        ArrayList<String> filters = new ArrayList<String>();

        String sparql = sparqlPrefixes;
        sparql += "construct  { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " } \n where { \n";
        sparql += "\t " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + " .\n ";

        sparql += "}";

        Graph g = Graph.create();
        logger.info("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());

        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
            logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is);
                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return g.getAllNodes();
    }

    /**
     * Specific to SPARQL 1.1 property path expressions
     *
     * @param gNode
     * @param from
     * @param qEdge
     * @param env ne pas prendre en compte l'env dans le calcul des chemins
     * @param exp
     * @param src l'ensemble des
     * @param start noeud courant
     * @param index sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start, int index) {

        //prefix handling
        String sparqlPrefixes = "";
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        //Specific to path processing
        Node subject = null;
        Node object = null;
        if (start == null) {
            subject = qEdge.getNode(0);
            object = qEdge.getNode(1);
        } else if (index == 0) {
            //normal order
            subject = start;
            object = qEdge.getNode(1);
        } else {
            //reverse order
            subject = qEdge.getNode(1);
            object = start;
        }

        // Query rewriting
        String transformedExp = exp.toRegex();
        String sparql = sparqlPrefixes;
        if (exp.isReverse()) {
            sparql += "construct  { " + object + " <" + exp.getName() + "> " + subject + " } \n where { \n";
            sparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
        } else if (exp.isNot()) {
            boolean valid = checkNeg(exp);
            if (!valid) {
                logger.warn("Invalid negation in path expression " + exp.toString());
                return new ArrayList<Entity>();
            } else {
                List<String> negProps = getFlatRegEx(exp);
                sparql += "construct  { " + subject + " ?_p " + object + " } \n where { \n";
                sparql += "\t " + subject + " ?_p " + object + " .\n ";
                sparql += "\tFILTER ( \n";
                for (String negP : negProps) {
                    sparql += "\t\t (?_p != " + negP + " ) && \n ";
                }
                sparql = sparql.substring(0, sparql.lastIndexOf("&&"));
                sparql += " )";

            }
        } else {
            sparql += "construct  { " + subject + " <" + exp.getName() + "> " + object + " } \n where { \n";
            sparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
        }
        sparql += "\n}";

        // Remote query processing
        Graph g = Graph.create();
//        logger.info("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
//            logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is);
//                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return g.getEdges();
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Node getNode(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public boolean isBindable(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public List<Node> toNodeList(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Mappings map(List<Node> qNodes, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms a regular expression eventually containing nested expressions
     * into a flat list of expressions.
     *
     * @param exp a regular expression eventually containing nested expressions.
     * @return a flat list of initially nested expressions.
     */
    private List<String> getFlatRegEx(Regex exp) {
        ArrayList<String> res = new ArrayList<String>();
        if (exp.getArity() == -1) {
            res.add(exp.getName());
        } else if (exp.getArity() == 0) {
            res.add(exp.getName());
        } else {
            for (int i = 0; i < exp.getArity(); i++) {
                res.addAll(getFlatRegEx(exp.getArg(i)));
            }
        }
        return res;
    }

    /**
     * Checks that a negation only covers | operators.
     *
     * @param exp a regular exression.
     * @return true if the negation expresison is valid.
     */
    private boolean checkNeg(Regex exp) {
        boolean res = true;
        if (exp.getArity() == -1) {
            return true;
        } else if (exp.getArity() == 0) {
            return true;
        } else {
            if (!(exp.getName().equals("!") || exp.getName().equals("|"))) {
                return false;
            }
            for (int i = 0; i < exp.getArity(); i++) {
                res = res && checkNeg(exp.getArg(i));
            }
        }
        return res;
    }

    public synchronized HashMap<String, Boolean> getCacheIndex() {
        return cacheIndex;
    }

    public RemoteProducer getRp() {
        return rp;
    }
}