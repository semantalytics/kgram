package fr.inria.edelweiss.kgtool.load.jsonld;

import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgtool.load.AddTripleHelper;
import fr.inria.edelweiss.kgtool.load.ILoadSerialization;
import java.util.List;

/**
 * Implementation of interface from Jsonld-java (json-ld parser) for adding
 * triples to corese graph
 *
 * @author Fuqi Song, Wimmics inria i3s
 * @date 10 Feb. 2014 new
 */
public class CoreseJsonTripleCallback implements JSONLDTripleCallback {

    private AddTripleHelper helper;
    private Graph graph;
    private Node graphSource;
    private final static String JSONLD_DEFALUT_GRAPH = "@default";
    private final static String JSONLD_BNODE_PREFIX = ":_";

    public CoreseJsonTripleCallback(Graph graph, String source) {
        this.graph = graph;

        if (source == null) {
            graphSource = this.graph.addGraph(Entailment.DEFAULT);
        } else {
            graphSource = this.graph.addGraph(source);
        }

        helper = AddTripleHelper.create(this.graph);
    }

    @Override
    public Object call(RDFDataset dataset) {

        for (String graphName : dataset.graphNames()) {

            //add graphs
            if (JSONLD_DEFALUT_GRAPH.equals(graphName)) {
                graphSource = graph.addGraph(Entailment.DEFAULT);
            } else if (graphName.startsWith(JSONLD_BNODE_PREFIX)) {
                graphSource = graph.addBlank(helper.getID(graphName));
                graph.addGraphNode(graphSource);
            } else {
                graphSource = graph.addGraph(graphName);
            }

            //add all triples to this graph
            final List<RDFDataset.Quad> quads = dataset.getQuads(graphName);
            for (final RDFDataset.Quad quad : quads) {

                String subject = quad.getSubject().getValue();
                String predicate = quad.getPredicate().getValue();
                RDFDataset.Node objectNode = quad.getObject();
                String object = objectNode.getValue();
                String lang = objectNode.getLanguage();
                String type = objectNode.getDatatype();

                int tripleType;
                if (objectNode.isLiteral()) {
                    tripleType = ILoadSerialization.LITERAL;
                } else {
                    tripleType = ILoadSerialization.NON_LITERAL;
                }

                helper.addTriples(subject, predicate, object, lang, type, tripleType, graphSource);
            }
        }

        return graph;
    }

    /**
     * Set parameters for helper class
     *
     * @param renameBNode
     * @param limit
     */
    public void setHelper(boolean renameBNode, int limit) {
        helper.setRenameBlankNode(renameBNode);
        helper.setLimit(limit);
    }
}
