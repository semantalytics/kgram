package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.ExpType;
import java.util.Collection;
import java.util.HashMap;

/**
 * Draft Graph Store where named graphs are hidden for system use 
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class GraphStore extends Graph {

   

    HashMap<String, Graph> store;


    GraphStore() {
        store = new HashMap<String, Graph>();
    }

    public static GraphStore create() {
        return new GraphStore();
    }

    public static GraphStore create(boolean b) {
        GraphStore gs = new GraphStore();
        if (b) {
            gs.setEntailment();
        }
        return gs;
    }


    public Graph getNamedGraph(String name) {
        return store.get(name);
    }

    public Graph createNamedGraph(String name) {
        Graph g = Graph.create();
        g.index();
        store.put(name, g);
        return g;
    }
    
    public Graph getCreateNamedGraph(String name) {
        Graph g = getNamedGraph(name);
        if (g != null){
            return g;
        }
        g = createNamedGraph(name);
        return g;
    }

    public void setNamedGraph(String name, Graph g) {
        store.put(name, g);
    }

    public Collection<Graph> getNamedGraphs() {
        return store.values();
    }

    public Graph getDefaultGraph() {
        return this;
    }
    
   
    
   
    
    
    
    
}
