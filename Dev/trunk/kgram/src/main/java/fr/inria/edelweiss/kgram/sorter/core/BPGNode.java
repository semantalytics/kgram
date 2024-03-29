package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.O;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.P;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.S;
import java.util.ArrayList;
import java.util.List;

/**
 * The node for triple pattern graph, which encapsualtes an expression (contain
 * an object exp) with selectivity
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class BPGNode {

    // the expression that the node encapsulates
    private final Exp exp;
    //the value of the selectivity that the expression represents
    private double selectivity = -1;

    //tripe pattern ?-tuple (S P O G FV FN)
    private TriplePattern pattern = null;

    //private final BPGEdge edge = null;
    private final Node graph;

    //private Iestimate
    public BPGNode(Exp exp, Node gNode, List<Exp> bindings) {
        this.exp = exp;
        this.graph = gNode;

        if (exp.type() == EDGE) {
            pattern = new TriplePattern(this, exp.getEdge(), bindings);
        } else {
            //set the selectivity of filter very big
            //so that it can be postioned at the end 
            this.selectivity = Integer.MAX_VALUE;
        }
    }

    public TriplePattern getPattern() {
        return pattern;
    }

    public Exp getExp() {
        return this.exp;
    }

    public int getType() {
        return this.exp.type();
    }

    public Node get(int i) {
        switch (i) {
            case S:
                return getSubject();
            case P:
                return getPredicate();
            case O:
                return getObject();
            default:
                return null;
        }
    }

    public Node getSubject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(0) : null;
    }

    public Node getPredicate() {
        return getType() == EDGE ? this.exp.getEdge().getEdgeNode() : null;
    }

    public Node getObject() {
        return getType() == EDGE ? this.exp.getEdge().getNode(1) : null;
    }

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }

    /**
     * Check if two BP node share same variables
     *
     * @param n BP node
     * @return true: shared; false: not share
     */
    public boolean isShared(BPGNode n) {
        return shared(this, n).size() > 0;
    }

    public List<String> shared(BPGNode n){
        return this.shared(this, n);
    }
    
    
    public List<String> shared(BPGNode bpn1, BPGNode bpn2) {
        int type1 = bpn1.exp.type();
        int type2 = bpn2.exp.type();

        switch (type1) {
            case EDGE:
                switch (type2) {
                    case EDGE:
                        return isShared(bpn1.exp.getEdge(), bpn2.exp.getEdge());
                    default: ;
                }
                break;
            case FILTER:
                switch (type2) {
                    case EDGE:
                        return isShared(bpn1.exp.getFilter(), bpn2.exp.getEdge());
                    default: ;
                }
                break;
            case VALUES:
                switch (type2) {
                    case EDGE:
                        return this.isShared(bpn1.exp.getNodeList(), bpn2.exp.getEdge());
                    default:
                }
            default:
                break;
        }

        return new ArrayList();
    }

    public List<String> isShared(List<Node> values, Edge e) {
        List<String> l = new ArrayList<String>();
        Node n0 = e.getEdgeNode();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        for (Node node : values) {
            String var = node.getLabel();
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                l.add(var);
            }
        }

        return l;
    }

    //check between edge and filter
    public List<String> isShared(Filter f, Edge e) {
        List<String> l = new ArrayList<String>();
        Node n0 = e.getEdgeNode();
        Node n1 = e.getNode(0);
        Node n2 = e.getNode(1);

        //get list of variable names
        List<String> vars = f.getVariables();
        for (String var : vars) {
            if (var.equalsIgnoreCase(n0.toString())
                    || var.equalsIgnoreCase(n1.toString())
                    || var.equalsIgnoreCase(n2.toString())) {
                l.add(var);
            }
        }

        return l;
    }

    //check between two edges
    //!! to be checked / tested
    public List<String> isShared(Edge e1, Edge e2) {
        List<String> l = new ArrayList<String>();
        List<Node> l1 = getVariables(e1);
        List<Node> l2 = getVariables(e2);

        for (Node n1 : l1) {
            for (Node n2 : l2) {
                if (n1.same(n2)) {
                    l.add(n1.getLabel());
                    break;
                }
            }
        }
        return l;
    }

    private List<Node> getVariables(Edge e) {
        List l = new ArrayList<Edge>();
        if (e.getNode(O).isVariable()) {
            l.add(e.getNode(O));
        }
        if (e.getEdgeNode().isVariable()) {
            l.add(e.getEdgeNode());
        }
        if (e.getNode(1).isVariable()) {
            l.add(e.getNode(1));
        }

        return l;
    }

    /**
     * Check if two types of node can be compared and connected
     *
     * @param n
     * @return
     */
    public boolean isCompitable(BPGNode n) {
        if (n == null) {
            return false;
        }
        if (this.exp.type() == EDGE && n.exp.type() == EDGE) {
            return true;
        } else if (this.exp.type() == FILTER && n.exp.type() == EDGE) {
            return true;
        } else if (this.exp.type() == VALUES && n.exp.type() == EDGE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return exp.toString() + "," + this.selectivity;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BPGNode)) {
            return false;
        } else {
            return this.exp.equals(((BPGNode) obj).exp);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.exp != null ? this.exp.hashCode() : 0);
        return hash;
    }
}
