package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import java.util.Arrays;
import java.util.List;

/**
 * Plugin for filter evaluator Compute semantic similarity of classes and
 * solutions for KGRAPH
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {

    static Logger logger = Logger.getLogger(PluginImpl.class);
    static String DEF_PPRINTER = Transformer.PPRINTER;
    private static final String NL = System.getProperty("line.separator");
   
    String PPRINTER = DEF_PPRINTER;
    // for storing Node setProperty() (cf Nicolas Marie store propagation values in nodes)
    // idem for setObject()
    static Table table;
    MatcherImpl match;
    Loader ld;
    private Object dtnumber;

    PluginImpl(Matcher m) {
        if (table == null) {
            table = new Table();
        }
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
        dtnumber = DatatypeMap.newInstance(Processor.FUN_NUMBER);
    }

    public static PluginImpl create(Matcher m) {
        return new PluginImpl(m);
    }   

    public Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {         

            case GRAPH:
                return getGraph(p);

            case LEVEL:
                return getLevel(env, p);
                
            case STL_NL:
                return nl(null, env, p);

            case PROLOG:
                return prolog(null, env, p);
                
            case STL_PREFIX:
                return prefix(env, p);
                
            case STL_NUMBER:
                return getValue(1 + env.count());
                                     
            case FOCUS_NODE:
                return getFocusNode(null, env);

            case SIM:
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                // solution similarity
                return similarity(g, env);
        }

        return null;
    }

    public Object function(Expr exp, Environment env, Producer p, Object o) {
        IDatatype dt = datatype(o);

        switch (exp.oper()) {

            case KGRAM:
            case NODE:
            case LOAD:
            case DEPTH:
            case SKOLEM:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                
                switch (exp.oper()) {
                    case KGRAM:
                        return kgram(g, o);

                    case NODE:
                        return node(g, o);

                    case LOAD:
                        return load(g, o);

                    case DEPTH:
                        return depth(g, o);
                        
                     case SKOLEM:               
                        return g.skolem(dt);    
                }                
                
            case INDENT:
                return indent(dt, env, p);
              
            case STL_NL:
                return nl(dt, env, p);   
                
            case PROLOG:
                return prolog(dt, env, p);
                
            case STL_PROCESS:
                return process(exp, env, p, dt);
                
            case APPLY_TEMPLATES:
            case APPLY_ALL_TEMPLATES:
                return pprint(dt, null, null, null, exp, env, p);

            case CALL_TEMPLATE:
                return pprint(null, dt, exp, env, p);

            case APPLY_TEMPLATES_WITH:
                return pprint(dt, null, exp, env, p);

            case TURTLE:
                return turtle(dt, env, p);

            case PPURI:
            case URILITERAL:
            case XSDLITERAL:
                return uri(exp, dt, env, p);
                
            case STL_LOAD:
                load(dt, env, p);
                return EMPTY;
                
            case APPLY_TEMPLATES_ON:
                return applyOn(null, dt, env, p);
                
            case FOCUS_NODE:
                return getFocusNode(dt, env);    

            case VISITED:
                return visited(dt, env, p);



            case GET:
                return getObject(o);

            case SET:
                return setObject(o, null);

            case QNAME:
                return qname(o, env);

           

        }
        return null;
    }

    private IDatatype visited(IDatatype dt, Environment env, Producer p) {
        Transformer pp = getTransformer(env, p);
        boolean b = pp.isVisited(dt);
        return getValue(b);
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1,
                dt2 = (IDatatype) o2;
        switch (exp.oper()) {

            case GETP:
                return getProperty(dt1, dt2.intValue());

            case SETP:
                return setProperty(dt1, dt2.intValue(), null);

            case SET:
                return setObject(dt1, dt2);

               
            case SIM:              
            case PSIM:               
            case ANCESTOR:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                switch (exp.oper()) {
                    case SIM:
                        // class similarity
                        return similarity(g, dt1, dt2);

                    case PSIM:
                        // prop similarity
                        return pSimilarity(g, dt1, dt2);


                    case ANCESTOR:
                        // common ancestor
                        return ancestor(g, dt1, dt2);
                }

             case WRITE:                
                return write(dt1, dt2);   
                
            case APPLY_TEMPLATES:
            case APPLY_ALL_TEMPLATES:
                // dt1: focus
                // dt2: arg
                return pprint(dt1, dt2, null, null, exp, env, p);
                
                
            case APPLY_TEMPLATES_ON:
                // dt1: transformation 
                // dt2: graph 
                return applyOn(dt1, dt2, env, p);    

            case APPLY_TEMPLATES_WITH:
            case APPLY_ALL_TEMPLATES_WITH:
                // dt1: uri of pprinter
                // dt2: focus
                return pprint(dt2, null, dt1, null, exp, env, p);

            case CALL_TEMPLATE:
                // dt1: template name
                // dt2: focus
                return pprint(dt2, null, null, dt1, exp, env, p);

            case CALL_TEMPLATE_WITH:
                // dt1: uri pprinter
                // dt2: template name
                return pprint(dt1, dt2, exp, env, p);

        }

        return null;
    }

    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {

        IDatatype dt1 =  (IDatatype) args[0];
        IDatatype dt2 =  (IDatatype) args[1];
        IDatatype dt3 =  (IDatatype) args[2];

        switch (exp.oper()) {

            case SETP:
                return setProperty(dt1, dt2.intValue(), dt3);


            case CALL_TEMPLATE:
                // dt1: template name
                // dt2: focus
                // dt3: arg
                return pprint(getArgs(args, 1), dt2, dt3, null, dt1, exp, env, p);

            case CALL_TEMPLATE_WITH:
                // dt1: uri pprinter
                // dt2: template name
                // dt3: focus
                return pprint(getArgs(args, 2), dt3, null, dt1, dt2, exp, env, p);

            case APPLY_TEMPLATES_WITH:
            case APPLY_ALL_TEMPLATES_WITH:
                // dt1: uri pprinter
                // dt2: focus
                // dt3: arg
                return pprint(getArgs(args, 1), dt2, dt3, dt1, null, exp, env, p);
                
                
            case APPLY_TEMPLATES:
            case APPLY_ALL_TEMPLATES:
                // dt1: focus
                // dt2: arg
                return pprint(getArgs(args, 0), dt1, dt2, null, null, exp, env, p);    

        }

        return null;
    }
    
    
    Object[] getArgs(Object[] obj, int n){
        return Arrays.copyOfRange(obj, n, obj.length);
    }

    IDatatype similarity(Graph g, IDatatype dt1, IDatatype dt2) {

        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    IDatatype ancestor(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        Node n = distance.ancestor(n1, n2);
        return (IDatatype) n.getValue();
    }

    IDatatype pSimilarity(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setPropertyDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    /**
     * Similarity of a solution with Corese method Sum distance of approximate
     * types Divide by number of nodes and edge
     *
     * TODO: cache distance in Environment during query proc
     */
    public IDatatype similarity(Graph g, Environment env) {
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
        Distance distance = g.setClassDistance();

        // number of node + edge in the answer
        int count = 0;
        float dd = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null) {
                Entity edge = memory.getEdge(qEdge);

                if (edge != null) {
                    count += 1;

                    for (int i = 0; i < edge.nbNode(); i++) {
                        // count nodes only once
                        Node n = edge.getNode(i);
                        if (!visit.containsKey(n)) {
                            count += 1;
                            visit.put(n, true);
                        }
                    }

                    if ((g.isType(qEdge) || env.getQuery().isRelax(qEdge))
                            && qEdge.getNode(1).isConstant()) {

                        Node qtype = g.getNode(qEdge.getNode(1).getLabel());
                        Node ttype = g.getNode(edge.getNode(1).getLabel());

                        if (qtype == null) {
                            // query type is undefined in ontology
                            qtype = qEdge.getNode(1);
                        }
                        if (ttype == null) {
                            // target type is undefined in ontology
                            ttype = edge.getNode(1);
                        }

                        if (!subClassOf(g, ttype, qtype, env)) {
                            dd += distance.distance(ttype, qtype);
                        }
                    }
                }
            }
        }

        if (dd == 0) {
            return getValue(1);
        }

        double sim = distance.similarity(dd, count);

        return getValue(sim);

    }

    boolean subClassOf(Graph g, Node n1, Node n2, Environment env) {
        if (match != null) {
            return match.isSubClassOf(n1, n2, env);
        }
        return g.isSubClassOf(n1, n2);
    }

    
    private IDatatype write(IDatatype dtfile, IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        ql.write(dtfile.getLabel(), dt.getLabel());
        return dt;
    }
   
    private Object getFocusNode(IDatatype dt, Environment env) {
        String name = Transformer.IN;
        if (dt != null){
            name = dt.getLabel();
        }
        Node node = env.getNode(name);
        if (node == null){
            return null;
        }
        return node.getValue();   
    }

    
    class Table extends Hashtable<Integer, PTable> {
    }

    class PTable extends Hashtable<Object, Object> {
    }

    PTable getPTable(Integer n) {
        PTable t = table.get(n);
        if (t == null) {
            t = new PTable();
            table.put(n, t);
        }
        return t;
    }

    Object getObject(Object o) {
        return getProperty(o, Node.OBJECT);
    }

    IDatatype setObject(Object o, Object v) {
        setProperty(o, Node.OBJECT, v);
        return TRUE;
    }

    IDatatype setProperty(Object o, Integer n, Object v) {
        PTable t = getPTable(n);
        t.put(o, v);
        return TRUE;
    }

    Object getProperty(Object o, Integer n) {
        PTable t = getPTable(n);
        return t.get(o);
    }

    Node node(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        Node n = g.getNode(dt, false, false);
        return n;
    }

    IDatatype depth(Graph g, Object o) {
        Node n = node(g, o);
        if (n == null || g.getClassDistance() == null) {
            return null;
        }
        Integer d = g.getClassDistance().getDepth(n);
        if (d == null) {
            return null;
        }
        return getValue(d);
    }

    IDatatype load(Graph g, Object o) {
        loader(g);
        IDatatype dt = (IDatatype) o;
        try {
            ld.loadWE(dt.getLabel());
        } catch (LoadException e) {
            logger.error(e);
            return FALSE;
        }
        return TRUE;
    }

    void loader(Graph g) {
        if (ld == null) {
            ld = ManagerImpl.getLoader();
            ld.init(g);
        }
    }

    Mappings kgram(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        String query = dt.getLabel();
        QueryProcess exec = QueryProcess.create(g, true);
        try {
            Mappings map = exec.sparqlQuery(query);
            return map;
        } catch (EngineException e) {
            return new Mappings();
        }
    }

    IDatatype qname(Object o, Environment env) {
        IDatatype dt = (IDatatype) o;
        if (!dt.isURI()) {
            return dt;
        }
        Query q = env.getQuery();
        if (q == null) {
            return dt;
        }
        ASTQuery ast = (ASTQuery) q.getAST();
        NSManager nsm = ast.getNSM();
        String qname = nsm.toPrefix(dt.getLabel(), true);
        if (qname.equals(dt.getLabel())) {
            return dt;
        }
        return getValue(qname);
    }
    
    /**
     * Increment indentation level
     */
    IDatatype indent(IDatatype dt, Environment env, Producer prod) {
        Transformer t = getTransformer(env, prod);
        t.setLevel(t.getLevel() + dt.intValue());        
        return EMPTY;
    }

    /**
     * New Line with indentation given by t.getLevel()
     * Increment level if dt!=null
     */
    IDatatype nl(IDatatype dt, Environment env, Producer prod) {
        Transformer t = getTransformer(env, prod);
        if (dt != null){
            t.setLevel(t.getLevel() + dt.intValue());
        }
        return t.tabulate();
   }
    
    IDatatype prolog(IDatatype dt, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        String title = null;
        if (dt != null){
            title = dt.getLabel();
        }
        String pref = p.getNSM().toString(title);
        return getValue(pref);
    }
    
    Mappings prefix(Environment env, Producer prod){
         Transformer p = getTransformer(env, prod);                 
         return p.NSMtoMappings();
    }

    IDatatype pprint(IDatatype tbase, IDatatype temp, Expr exp, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod, getLabel(tbase), null);
        return p.process(getLabel(temp),
                exp.oper() == ExprType.APPLY_ALL_TEMPLATES
                || exp.oper() == ExprType.APPLY_ALL_TEMPLATES_WITH,
                exp.getModality());
    }

    /**
     * exp is the calling expression: kg:pprint kg:pprintAll kg:template focus
     * is the node to be printed tbase is the path of the template base to be
     * used, may be null temp is the name of a named template, may be null
     * modality: kg:pprintAll(?x ; separator = "\n")
     */
    
    
     IDatatype pprint(IDatatype focus, IDatatype arg, IDatatype tbase, IDatatype temp, Expr exp, Environment env, Producer prod) {
          return pprint(null, focus, arg, tbase, temp, exp, env, prod); 
     }
     
     IDatatype pprint(Object[] args, IDatatype focus, IDatatype arg, IDatatype tbase, IDatatype temp, Expr exp, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod, getLabel(tbase), focus);
        IDatatype dt = p.process(args, focus, arg,
                getLabel(temp),
                exp.oper() == ExprType.APPLY_ALL_TEMPLATES
                || exp.oper() == ExprType.APPLY_ALL_TEMPLATES_WITH,
                exp.getModality(), exp, env.getQuery());
        return dt;
    }
     
    /**
     * st:process(var) : default variable processing by SPARQL Template
     * Ask PPrinter what is default behavior
     * set st:process() to it's default behavior
     * the default behavior is st:apply-templates
     */
    public Object process(Expr exp, Environment env, Producer p, IDatatype dt) {
        Query q = env.getQuery();
        Transformer pp = getTransformer(env, p);
        // overload current st:process() oper code to default behaviour oper code
        // future executions of this st:process() will directly execute target default behavior
        Expr def = q.getProfile(Transformer.STL_PROCESS); //pp.getProcessExp();
        
        if (def == null){
            int oper = pp.getProcess();                     
            exp.setOper(oper);
            Object res = function(exp, env, p, dt);
            // if we want STL_PROCESS to get back to it's initial behavior:
            // unset the comment below
            // exp.setOper(ExprType.STL_PROCESS);
            return res;
        }
        else {     
            Expr ee = rewrite(exp, def, (ASTQuery)env.getQuery().getAST());          
            exp.setOper(SELF);
            exp.setExp(0, ee);
            return getEvaluator().eval(ee, env, p);
        } 
    }
    
    
    /**
     * proc: st:process(?y)
     * def:  st:process(?x) = st:apply-templates(?x)
     * copy def right exp and rename its variable (?x) as proc variable (?y)
     * PRAGMA: do no process exists {} in def
     */
    Expr rewrite(Expr proc, Expr def, ASTQuery ast){
        Term tproc = (Term) proc;
        Term tdef  = (Term) def;
        Variable v1 = tdef.getArg(0).getArg(0).getVariable(); // ?x
        Variable v2 = tproc.getArg(0).getVariable(); // ?y
        Expression tt = tdef.getArg(1).copy(v1, v2);
        tt.compile(ast);
        return tt;
    }

    
    /**
     * 
    
     */


    IDatatype turtle(IDatatype o, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        IDatatype dt = p.turtle(o);
        return dt;
    }
    
    IDatatype xsdLiteral(IDatatype o, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        IDatatype dt = p.xsdLiteral(o);
        return dt;
    }

    IDatatype uri(Expr exp, IDatatype dt, Environment env, Producer prod) {
        if (dt.isURI()) {
            return turtle(dt, env, prod);
        } else if (dt.isLiteral() && exp.oper() == ExprType.URILITERAL) {
            return turtle(dt, env, prod);
        } else if (dt.isLiteral() && exp.oper() == ExprType.XSDLITERAL) {
            return xsdLiteral(dt, env, prod);
        } else {
            return pprint(dt, null, null, null, exp, env, prod);
        }
    }
    
    private void load(IDatatype dt, Environment env, Producer p) {
        Transformer t = getTransformer(env, p);
        t.load(dt.getLabel());
    }

    IDatatype applyOn(IDatatype trans, IDatatype graph, Environment env, Producer p){
        Transformer t = getTransformer(env, p, getLabel(trans), null);
        return t.processOn(graph.getLabel());
    }

    IDatatype getLevel(Environment env, Producer prod) {
        return getValue(level(env, prod));
    }

    int level(Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        return p.level();
    }
  
    String getLabel(IDatatype dt) {
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return null;
    }

    String getPP(Producer prod, IDatatype dt) {
        Graph g = getGraph(prod);
        if (g == null) {
            return Transformer.TURTLE;
        }
        IDatatype type = g.getValue(RDF.TYPE, dt);
        if (type != null) {
            String p = Transformer.getPP(type.getLabel());
            if (p != null) {
                return p;
            }
        }
        return Transformer.TURTLE;
    }


    Transformer getTransformer(Environment env, Producer p) {
        return getTransformer(env, p, (String) null, null);
    }
    
    Transformer getTransformer(Environment env, Producer prod, String t, IDatatype dt) {
        Query q = env.getQuery();
        String p = null;

        if (t != null) {
            p = t;
        } else if (q.hasPragma(Pragma.TEMPLATE)) {
            p = (String) q.getPragma(Pragma.TEMPLATE);
        } 

        Object o = q.getPP(p);

        if (o != null) {
            return (Transformer) o;
        } else {
            Graph g = getGraph(prod);
            if (g == null) {
                g = Graph.create();
            }
            Transformer pp = Transformer.create(g, p);
            ASTQuery ast = (ASTQuery) q.getAST();
            pp.setNSM(ast.getNSM());
           // pp.setProfile(q.getProfile());
            q.setPPrinter(p, pp);
            return pp;
        }
    }

    public void setPPrinter(String str) {
        PPRINTER = str;
    }
    
    /**
     * create concat(str, st:number(), str)
     */
    public Expr createFunction(String name, List<Object> args, Environment env){
        Term t = Term.function(name);
        for (Object arg : args){
            if (arg instanceof IDatatype){
                // str: arg is a StringBuilder, keep it as is
                Constant cst = Constant.create("Future", null, null);
                cst.setDatatypeValue((IDatatype) arg);
                t.add(cst);
            }
            else {
                // st:number()
               t.add((Expression) arg);
            }
        }
        t.compile((ASTQuery)env.getQuery().getAST());
        return t;
    }
}
