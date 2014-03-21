package fr.inria.edelweiss.kgraph.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Forward Rule Engine Use construct {} where {} SPARQL Query as Rule
 *
 * TODO: This engine creates target blank nodes for rule blank nodes hence it
 * may loop:
 *
 * construct {?x ex:rel _:b2} where {?x ex:rel ?y}
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 */
public class RuleEngine implements Engine {

    private static final String UNKNOWN = "unknown";
    private static Logger logger = Logger.getLogger(RuleEngine.class);
    Graph graph;
    QueryProcess exec;
    List<Rule> rules;
    private PTable ptable;
    RTable rtable;
    STable stable;
    // check that kgram solutions contain a newly entailed edge
    ResultWatcher rw;
    // kgram ResultListener create edges instead of create Mappings
    // LIMITATION: do not use if rule creates Node because graph would be 
    // modified during query execution
    private boolean isConstructResult = false;
    // run rules for wich new edges were created at loop n-1
    // check that rule solutions contains one edge from loop n-1
    // LIMITATION: do not use if Corese RDFS entailment is set to true
    // because we test predicate equality (we do not check rdfs:subPropertyOf)
    private boolean isOptimize = false;
    
    boolean debug = false, 
            trace = false;
    // RETE like, is not efficient
    private boolean isOptimization = false;
    int loop = 0;
    private boolean isActivate = true;

    RuleEngine() {
        rules = new ArrayList<Rule>();
    }

    void set(Graph g) {
        graph = g;
    }

    public void set(QueryProcess p) {
        exec = p;
        p.setListPath(true);
    }

    public QueryProcess getQueryProcess() {
        return exec;
    }

    public void set(Sorter s) {
        if (exec != null) {
            exec.set(s);
        }
    }

    public void setOptimize(boolean b) {
        isOptimize = b;
    }
    
    public void setTrace(boolean b) {
        trace = b;
    }
    public static RuleEngine create(Graph g) {
        RuleEngine eng = new RuleEngine();
        eng.set(g);
        eng.set(QueryProcess.create(g));
        return eng;
    }

    public static RuleEngine create(QueryProcess q) {
        RuleEngine eng = new RuleEngine();
        eng.set(q);
        return eng;
    }

    public static RuleEngine create(Graph g, QueryProcess q) {
        RuleEngine eng = new RuleEngine();
        eng.set(g);
        eng.set(q);
        return eng;
    }

    public boolean process() {
        if (graph == null) {
            set(Graph.create());
        }
        //OC:
        //synEntail();
        int size = graph.size();
        entail();
        return graph.size() > size;
    }

    public int process(Graph g) {
        set(g);
        if (exec == null) {
            set(QueryProcess.create(g));
        }
        return entail();
    }

    public int process(Graph g, QueryProcess q) {
        set(g);
        set(q);
        return entail();
    }

    public int process(QueryProcess q) {
        if (graph == null) {
            set(Graph.create());
        }
        set(q);
        return entail();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public void clear() {
        rules.clear();
    }

    /**
     * Define a construct {} where {} rule
     */
    public Query defRule(String rule) throws EngineException {
        return defRule(UNKNOWN, rule);
    }

    public void defRule(Query rule) {
        rules.add(Rule.create(UNKNOWN, rule));
    }

    public void addRule(String rule) {
        try {
            defRule(rule);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public ResultWatcher getResultListener(){
        return rw;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Query defRule(String name, String rule) throws EngineException {
        Query qq = exec.compileRule(rule);

        if (!qq.isConstruct()) {
            // template
            qq.setRule(false);
            ASTQuery ast = (ASTQuery) qq.getAST();
            ast.setRule(false);
        }

        if (qq != null) { // && qq.isConstruct()) {
            rules.add(Rule.create(name, qq));
            return qq;
        }
        return null;
    }

    int synEntail() {
        try {
            graph.writeLock().lock();
            return entail();
        } finally {
            graph.writeLock().unlock();
        }
    }
    
    
    

    /**
     * Process rule base at saturation PRAGMA: not synchronized on write lock
     */
    public int entail() {

        if (isOptimize || isOptimization) {
            // consider only rules that match newly entailed edge predicates
            // consider solutions that contain at leat one newly entailed edge
            start();
            if (loop != 0){
                // start a new rule processing
                clean();
            }
        }
        if (isOptimization) {
            // apply pertinent rules on newly entailed edges using kgram edge binding
            // not so efficient
            start2();
        }
        
        int size = graph.size(),
                start = size;
        loop = 0;
        int skip = 0, nbrule = 0, tskip = 0, trun = 0, tnbres = 0;
        boolean go = true;

        // Entailment 
        graph.init();

        List<Entity> list = null, current;

        ITable t = null;
        stable = new STable();
        
        if (isOptimize){
            // kgram return solutions that contain newly entailed edge
            rw = new ResultWatcher(); 
            exec.addResultListener(rw);
        }
        
        while (go) {
            Date d1 = new Date();
            skip = 0;
            nbrule = 0;
            tnbres = 0;
            if (trace){
                System.out.println("Loop: " + loop);
           }

            // List of edges created by rules in this loop 
            current = list;
             list = new ArrayList<Entity>();            
         
            if (loop == 0 || ! isOptimization) {
                
                for (Rule rule : rules) {
                    
                    if (debug) {
                        rule.getQuery().setDebug(true);
                    }

                    int nbres = 0;

                    if (isOptimize) {
                        // start exec ResultWatcher, it checks that each solution 
                        // of rule contains at least one new edge from current
                        rw.start(rule);
                        t = record(rule);

                        if (loop == 0 || accept(rule, getRecord(rule), t)) {
                            nbres = process(rule, null, list);
                            tnbres += nbres;
                            if (debug) {
                                System.out.println("List: " + list.size());
                            }
//                            if (trace && nbres > 0){
//                                System.out.println("RE: " + nbres + " " + rule.getAST());
//                            }
                            nbrule++;
                        } else {
                            skip++;
                        }

                        // record cardinality (computed before rule execution)
                        setRecord(rule, t);
                        rw.finish(rule);
                    } else {
                        nbres = process(rule, null, list);
                       nbrule++;
                    }

                    if (trace) {
                        stable.record(rule, nbres);
                    }
                }                             
            }
            else {
                // isOptimization = true
                // sort newly entailed edges according to their predicate
                ETable etable = new ETable();
                for (Entity ent : current){
                    etable.add(ent);
                }
                
               // for each predicate, get rules that match predicate
               // run rule with kgram binding new edges of this predicate
               for (Node p : etable.keySet()){
                    for (Rule r : ptable.getRules(p.getLabel())){
                        int nbres = process(r, etable.get(p), list);                        
                    }
                }
            }
            
            if (trace){
                System.out.println("NBrule: " + nbrule);   
//                System.out.println("nbres: "  + tnbres);   
                System.out.println("Graph: "  + graph.size());
            }
    
            if (debug){
               System.out.println("Skip: " + skip);
               System.out.println("Run: " + nbrule);
                System.out.println("Graph: " + graph.size());
               tskip += skip;
               trun += nbrule;
            }
         
            
            
            if (graph.size() > size) {
                // There are new edges: entailment again
                size = graph.size();
                loop++;
                
                if (isOptimize){
                    // set loop number in newly entailed edges
                    for (Entity ent : list){
                        ent.getEdge().setIndex(loop);
                    }
                    // set loop number in result listener 
                    rw.setLoop(loop);
                }
            } else {
                go = false;
            }
            
//            if (trace){
//                Date d2 = new Date();
//                System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / ( 1000.0));
//            }
        }
        if (debug){
            System.out.println("Total Skip: " + tskip);
            System.out.println("Total Run: " + trun);
        }
        if (debug) {
            logger.debug("** Rule: " + (graph.size() - start));
        }
        
        return graph.size() - start;
    }
    
    
    
    
    
    public void trace() {
        for (Rule r : stable.sort()) {
            System.out.println(stable.get(r) + " " + r.getQuery().getAST());
        }
    }
    
    /**
     * Clean index of edges that are stored when isOptim=true
     */
    public void clean(){
        for (Entity ent : graph.getEdges()){
            ent.getEdge().setIndex(-1);
        }
    }

    /**
     * Process one rule Store created edges into list
     */
    int process(Rule rule, List<Entity> current, List<Entity> list) {
       boolean isConstruct = isOptimize &&  isConstructResult;
       
        Query qq = rule.getQuery();
        //qq.setEdgeList(current);
        Construct cons = Construct.create(qq, Entailment.RULE);
        cons.setRule(rule, rule.getIndex());
        cons.setInsertList(list);
        cons.setGraph(graph);
        
        List<Entity> le = null;
        
        if (isConstruct){
            // kgram Result Listener create edges in list
            // after query completes, edges are inserted in graoh
            // no Mappings are created by kgram
            le = new ArrayList<Entity>();
            cons.setBuffer(true);
            cons.setInsertList(le);
            cons.setDefaultGraph(graph.addGraph(Entailment.RULE));
            Mappings map = Mappings.create(qq);
            rw.setConstruct(cons);
            rw.setMappings(map);
        }

        int start = graph.size();
        
        Mappings map = exec.query(qq, null);        
        
        if (isConstruct){
            // kgram ResultListener has created edges in List el
            // insert these edges into graph
           for (Entity ee : le){
                Entity ent = graph.addEdge(ee);
                if (ent != null){
                    list.add(ent);
                }
            }
        }
        else {
            // create edges from Mappings as usual
            cons.insert(map, graph, null);
        }
        //qq.setEdgeList(null);

        if (debug || qq.isDebug()) {
            logger.info("** Mappings: " + map.size());
            logger.info("** Inserted: " + (graph.size() - start));
            if (map.size() > 0){
                System.out.println(rule.getQuery().getAST());
            }
        }

        return graph.size() - start;
    }

//    /**
//     * current: list of newly created edges
//     *
//     */
//    int process(Rule rule, List<Entity> list, List<Entity> current) {
//
//        int start = graph.size();
//        Query qq = rule.getQuery();
//        Environment env = EnvironmentImpl.create(qq);
//
//        for (Entity ent : current) {
//            Edge qEdge = match(qq, ent.getEdge(), env);
//            if (qEdge != null) {
//                Mapping map = Mapping.create(qEdge, ent.getEdge());
//                run(rule, map, list);
//            }
//        }
//
//        return graph.size() - start;
//    }
//
//    Edge match(Query q, Edge edge, Environment env) {
//        Exp body = q.getBody();
//        for (Exp exp : body) {
//            if (exp.isEdge()) {
//                if (exec.getMatcher().match(exp.getEdge(), edge, env)) {
//                    return exp.getEdge();
//                }
//            }
//        }
//        return null;
//    }

    /**
     * **************************************************
     *
     * Compute rule predicates Accept rule if some predicate has new triple in
     * graph
     *
     * *************************************************
     */
    /**
     * Compute table of rule predicates, for all rules
     */
    void start() {
        rtable = new RTable();

        for (Rule rule : rules) {
            init(rule);
        }
    }
    
    /**
     * Generate a table : predicate -> List of Rule
     */
    void start2() {
        ptable = new PTable();
        String top = null;
        for (Rule rule : rules) {
           for (Node pred : rule.getPredicates()) {
                ptable.add(pred.getLabel(), rule);
                if (pred.getLabel().equals(Graph.TOPREL)) {
                    top = pred.getLabel();
                }
            }
        }

        if (top != null) {
            ptable.setTop(top);
            List<Rule> l = ptable.get(top);
            for (String p : ptable.keySet()) {
                if (! p.equals(top)){
                     ptable.add(p, l);
                }
            }
        }
    }

    /**
     * Store list of predicates of this rule
     */
    void init(Rule rule) {
        rule.set(rule.getQuery().getNodeList());
    }

    /**
     * @return the isOptimization
     */
    public boolean isOptimization() {
        return isOptimization;
    }

    /**
     * @param isOptimization the isOptimization to set
     */
    public void setOptimization(boolean isOptimization) {
        this.isOptimization = isOptimization;
    }

    /**
     * @return the isConstructResult
     */
    public boolean isConstructResult() {
        return isConstructResult;
    }

    /**
     * @param isConstructResult the isConstructResult to set
     */
    public void setConstructResult(boolean isConstructResult) {
        this.isConstructResult = isConstructResult;
    }

    class PTable extends HashMap<String, List<Rule>>{
        
        String top;
        List<Rule> empty = new ArrayList<Rule>();
        
        void setTop(String n){
            top = n;
        }
        
        void add(String label, Rule r){
            List<Rule> l = get(label);
            if (l == null){
                l = new ArrayList<Rule>();
                put(label, l);
            }
            l.add(r);
        }
        
        void add(String label, List<Rule> lr){
           List<Rule> lp = get(label);
           for (Rule r : lr){
               if (! lp.contains(r)){
                   lp.add(r);
               }              
            }
        }
        
        List<Rule> getRules(String label){
            List<Rule> l = get(label);
            if (l == null){
                if (top != null){
                    return get(top);
                } 
                else {
                    return empty;
                }
            }
            else {
                return l;
            }
        }
        
        public String toString(){
            StringBuilder sb = new StringBuilder();
            for (String p : keySet()){
                sb.append(p);
                sb.append("\n");
               for (Rule r : getRules(p)){
                    sb.append(r.getAST());
                    sb.append("\n");
               }
               sb.append("\n");
            }
            return sb.toString();
        }

    }
    
    class ETable extends HashMap<Node, List<Entity>> {
        
        void add(Entity ent){
            Node p = ent.getEdge().getEdgeNode();
            List<Entity> l = get(p);
            if (l == null){
                l = new ArrayList<Entity>();
                put(p, l);
            }
            l.add(ent);
        }
    }
    
    class ITable extends Hashtable<String, Integer> {
    }

    class RTable extends Hashtable<Rule, ITable> {
    }
    
    class STable extends Hashtable<Rule, Integer> {
        
        void record(Rule r, int n){
            Integer i = get(r);
            if (i == null){
                i = 0;
            }
            put(r, i + n);
        }
        
        List<Rule> sort(){
            ArrayList<Rule> list = new ArrayList<Rule>();
            
            for (Rule r : keySet()){
                list.add(r);
            }
            
            Collections.sort(list, 
                    new Comparator<Rule>(){

                @Override
                public int compare(Rule o1, Rule o2) {
                    return get(o2).compareTo(get(o1));
                }
                    }
            );
    
            return list;
        }
    }

    /**
     * Record predicates cardinality in graph
     */
    ITable record(Rule r) {
        ITable itable = new ITable();

        for (Node pred : r.getPredicates()) {
            int size = graph.size(pred);
            itable.put(pred.getLabel(), size);
        }

        return itable;
    }

    /**
     * Rule is selected if one of its predicate has a new triple in graph
     */
    boolean accept(Rule rule, ITable told, ITable tnew) {
        for (Node pred : rule.getPredicates()) {
            String name = pred.getLabel();
            if (tnew.get(name) > told.get(name)) {
                return true;
            }
       }
       return false;
    }

    /**
     * Return previous record of rule predicate cardinality
     */
    ITable getRecord(Rule r) {
        return rtable.get(r);
    }

    void setRecord(Rule r, ITable t) {
        rtable.put(r, t);
    }

    public void init() {
    }

    public void onDelete() {
    }

    public void onInsert(Node gNode, Edge edge) {
    }

    public void onClear() {
    }

    public void setActivate(boolean b) {
        isActivate = b;
    }

    public boolean isActivate() {
        return isActivate;
    }

    public void remove() {
        graph.clear(Entailment.RULE, true);
    }

    public int type() {
        return RULE_ENGINE;
    }
}
