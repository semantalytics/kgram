package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import static fr.inria.edelweiss.kgtool.print.RDFFormat.getAST;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pretty printing for JSON-LD format
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb 2014
 */
public class JSONLDFormat {

    //open close style (OC_)
    public static final int OC_BRACE = 10;//@key:{..},
    public static final int OC_NOCOMMA = 11;//@key:{..}
    public static final int OC_SBRACKET = 20;//@key:[..],
    public static final int OC_NOKEY = 30;//{..},
    public static final int OC_NONE = 40;//..
    public static final int OC_ONELINE = 41;//{..}

    //JSON-LD keywords (KW_)
    public static final String KW_CONTEXT = "\"@context\"";
    public static final String KW_ID = "\"@id\"";
    public static final String KW_VALUE = "\"@value\"";
    public static final String KW_LANGUAGE = "\"@language\"";
    public static final String KW_TYPE = "\"@type\"";
    public static final String KW_CONTAINER = "\"@container\"";
    public static final String KW_LIST = "\"@list\"";
    public static final String KW_SET = "\"@set\"";
    public static final String KW_REVERSE = "\"@reverse\"";
    public static final String KW_INDEX = "\"@index\"";
    public static final String KW_BASE = "\"@base\"";
    public static final String KW_VOCAB = "\"@vocab\"";
    public static final String KW_GRAPH = "\"@graph\"";

    //Seprators (SP_)
    public static final String SP_COLON = ": ";
    public static final String SP_COMMA = ",";
    public static final String BRACE_LEFT = "{";
    public static final String BRACE_RIGHT = "}";
    public static final String SBRACKET_LEFT = "[";
    public static final String SBRACKET_RIGHT = "]";
    public static final String SP_TAB = "\t";
    public static final String SP_NL = System.getProperty("line.separator");

    Graph graph;
    Mapper map;
    NSManager nsm;
    Query query;
    ASTQuery ast;

    JSONLDFormat(NSManager n) {
        nsm = n;
    }

    JSONLDFormat(Graph g, Query q) {
        this(((ASTQuery) q.getAST()).getNSM());
        if (g != null) {
            graph = g;
            graph.prepare();
        }
        ast = getAST(q);
        query = q;
    }

    JSONLDFormat(Graph g, NSManager n) {
        this(n);
        if (g != null) {
            graph = g;
            graph.prepare();
        }
    }

    public static JSONLDFormat create(Graph g, NSManager n) {
        return new JSONLDFormat(g, n);
    }

    public static JSONLDFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            Query q = map.getQuery();
            NSManager nsm = ((ASTQuery) q.getAST()).getNSM();
            return create(g, nsm);
        }
        return create(Graph.create());
    }

    public static JSONLDFormat create(Graph g) {
        return new JSONLDFormat(g, NSManager.create());
    }

    @Override
    public String toString() {
        StringBuilder error = error();
        if (error.length() != 0) {
            return error.toString();
        } else {
            return getJsonLdObject().toString();
        }
    }

    /**
     * Get the top level object of JSON-LD
     * @return JSON object
     */
    public JSONLDObject getJsonLdObject() {
        //****1 check condition
        if (graph == null && map == null) return new JSONLDObject();

        JSONLDObject topLevel = new JSONLDObject(OC_NOKEY);
        
        JSONLDObject defaultGraph = null;
        List<JSONLDObject> otherGraphs = new ArrayList<JSONLDObject>();

        //****2. Add graphs
        if (size(graph.getGraphNodes()) <= 1) {// only one graph, put them all together 
            defaultGraph = graph(null);
        } else {//multiple graph, read each graph 
            for (Node gNode : graph.getGraphNodes()) {
                //2.1 default graph
                if (Entailment.DEFAULT.equals(gNode.getLabel())) {
                    defaultGraph = graph(gNode);
                } else {
                    //2.2.0 get the info of graph
                    JSONLDObject graphInfo = jsonldObject(gNode, gNode);
                    graphInfo.setModularType(OC_NONE);

                    JSONLDObject other = new JSONLDObject(OC_BRACE);
                    //2.2.1 add graph info
                    other.addObject(graphInfo);
                    //2.2.2 add graph
                    other.addObject(graph(gNode));

                    //2.2.3 add this graph to graph list
                    otherGraphs.add(other);
                }
            }
        }
        if (defaultGraph == null)  defaultGraph = new JSONLDObject(KW_GRAPH);

        //****3. add the other graphs to default graph
        defaultGraph.addObject(otherGraphs);
        defaultGraph.setModularType(OC_SBRACKET);

        //****3.1 Add context
        topLevel.addObject(context());

        //****4. add default(all) graph(s) to top level object
        topLevel.addObject(defaultGraph);
        return topLevel;
    }

    //Get the Json Object of "@Context"
    private JSONLDObject context() {
        JSONLDObject context = new JSONLDObject(KW_CONTEXT, OC_BRACE);

        Set<String> ns = nsm.getPrefixSet();

        //1. add base @base
        String base = nsm.getBase();
        if (base != null) {
            context.addObject(new JSONLDObject(KW_BASE, base));
        }

        //2. add prefixes 
        for (String p : ns) {
            context.addObject(new JSONLDObject(quote(p), quote(nsm.getNamespace(p))));
        }

        return context;
    }

    //Get the Json oject of "@Graph" according to graph node:gNode
    //if gNode == null, get all nodes 
    private JSONLDObject graph(Node gNode) {
        JSONLDObject jGraph = new JSONLDObject(KW_GRAPH, OC_SBRACKET);
        
        Iterable<Entity> allNode = gNode == null ? graph.getAllNodes(): graph.getNodes(gNode);

        //iterate each node and add to this graph
        for (Entity ent : allNode) {
            Node node = ent.getNode();
            JSONLDObject jo = jsonldObject(gNode, node);
            if (jo != null) {
                jGraph.addObject(jo);
            }
        }
        return jGraph;
    }

    //compose one object of jsonld from graph
    private JSONLDObject jsonldObject(Node gNode, Node node) {
        if (size(graph.getNodeEdges(gNode, node)) < 1) return null;
        
        JSONLDObject jo = new JSONLDObject(OC_BRACE);
        
        //1. add node id
        jo.addObject(subjectId(node));

        //2. add properties and objects
        jo.addObject(propertyAndObject(gNode, node));

        return jo;
    }

    //Get "@id" of the node
    private JSONLDObject subjectId(Node node) {
        JSONLDObject jo = new JSONLDObject(KW_ID);

        IDatatype dt = (IDatatype) node.getValue();
        String subject = dt.isBlank() ? dt.getLabel() : nsm.toPrefixURI(dt.getLabel());
        subject = filter(subject);
        
        //repalce rdf:type with @type
        if (RDF.TYPE.equals(dt.getLabel())) {
            subject = KW_TYPE;
        }

        jo.setObject(quote(subject));
        return jo;
    }
    
    //get the list of proerperties and objects according to given node subject id
    private List<JSONLDObject> propertyAndObject(Node gNode, Node node) {
        CopyOnWriteArrayList<JSONLDObject> list = new CopyOnWriteArrayList<JSONLDObject>();

        for (Entity ent : graph.getNodeEdges(gNode, node)) {
            if (ent == null) continue;

            Edge edge = ent.getEdge();

            //1. get property
            String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());
            //repalce @type
            boolean type = false;
            if (RDF.TYPE.equals(edge.getEdgeNode().getLabel())) {
                type = true;
                pred = KW_TYPE;
            } else {
                pred = quote(filter(pred));
            }

            Object obj=null;

            //2. get object
            IDatatype dt = (IDatatype) edge.getNode(1).getValue();
            if (dt.isLiteral()) {//2.1 literal
                IDatatype datatype = dt.getDatatype();
                IDatatype lang = dt.getDataLang();
                if (datatype != null || lang != null) {
                    obj = addLiteralInfo(dt);
                } else {
                    obj = dt.getLabel();
                }
            } else {
                String label = null;
                if (dt.isBlank()) {//2.2 blank node
                    label = dt.getLabel();
                } else if (dt.isURI()) {//2.3 uri
                    label = nsm.toPrefixURI(dt.getLabel());
                }
                label = quote(filter(label));
                
                //add key word @id to these nodes expect those nodes whose 
                //properties are @type
                if (!type) {
                    JSONLDObject temp = new JSONLDObject(OC_NOCOMMA);
                    temp.addObject(new JSONLDObject(KW_ID, label));
                    obj = temp;
                }else{
                   obj = label; 
                }
            }
            list.add(new JSONLDObject(pred, obj));
        }

        return merge(list);
    }

    private List merge(CopyOnWriteArrayList<JSONLDObject> list) {
        List newList = new ArrayList();
        for (JSONLDObject jo : list) {
            //more than one nodes share same predicate
            newList.add(filterObject(list, jo));
        }
        return newList;
    }

    private JSONLDObject filterObject(List<JSONLDObject> list, JSONLDObject obj) {
         
        List<JSONLDObject> selected = new ArrayList();
        for (JSONLDObject jo : list) {
            if (jo.getKey().equals(obj.getKey())) {
                selected.add(jo);
                list.remove(jo);
            }
        }
        
        JSONLDObject merge=null;
        if(selected.size()>1){
            merge = new JSONLDObject();
            merge.setKey(selected.get(0).getKey());
            merge.setModularType(OC_SBRACKET);
            for (JSONLDObject jo : selected) {
                merge.addObject(jo.getObject());
            }
        }else if(selected.size()==1){//only one object, nonthing changes
            merge = selected.get(0);
        }else{
            System.out.println("stop here");
        }
        return merge;
    }

    //Expand the informaion of literal:@value, @type or @value, @langauge
    private JSONLDObject addLiteralInfo(IDatatype literal) {

        JSONLDObject jo = new JSONLDObject(OC_NOCOMMA);

        IDatatype datatype = literal.getDatatype();
        IDatatype lang = literal.getDataLang();

        // 1 add @value
        jo.addObject(new JSONLDObject(KW_VALUE, quote(filter(literal.getLabel()))));

        boolean bType = datatype != null && !datatype.getLabel().isEmpty();
        boolean bLang = lang != null && !lang.getLabel().isEmpty();

        if (bLang) {//2 add @language tag if any
            String language = nsm.toPrefixURI(lang.getLabel());
            jo.addObject(new JSONLDObject(KW_LANGUAGE, quote(filter(language))));
        } else if (bType) {//3 add @type if any
            String type = nsm.toPrefixURI(datatype.getLabel());
            jo.addObject(new JSONLDObject(KW_TYPE, quote(filter(type))));
        }

        return jo;
    }

    //add qutation marks
    private String quote(String str) {
        return "\"" + str + "\"";
    }

    //return the size of an iterable object
    private int size(Iterable it) {
        int counter = 0;
        for (Object e : it) {
            counter++;
        }

        return counter;
    }

    //append a string with new line
    private void append(StringBuilder sb, Object add) {
        sb.append(add).append(SP_NL);
    }

    //filter string to remove < or >
    private String filter(String label) {
        if (label.contains("<") && label.contains(">")) {
            return label.replaceAll("<|>", "");
        }
        return label;
    }

    

    /**
     * write Json-ld output to file
     *
     * @param name file name
     * @throws IOException
     */
    public void write(String name) throws IOException {
        StringBuilder sb = this.getJsonLdObject().toStringBuilder();
        FileOutputStream fos = new FileOutputStream(name);
        for (int i = 0; i < sb.length(); i++) {
            fos.write(sb.charAt(i));
        }
        fos.close();
    }

    // error message
    private StringBuilder error() {
        StringBuilder error = new StringBuilder();
        boolean bAstError = ast != null && ast.getErrors() != null;
        boolean bQueryError = query != null && query.getErrors() != null;

        if (bAstError || bQueryError) {

            if (ast.getText() != null) {
                append(error, ast.getText());
            }
            append(error, "");

            if (bAstError) {
                for (String mes : ast.getErrors()) {
                    append(error, mes);
                }
            }
            if (bQueryError) {
                for (String mes : query.getErrors()) {
                    append(error, mes);
                }
            }
            append(error, "");
        }
        return error;
    }    
}
