/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * TODO To be cleaned / tested exist or !exist
 *
 * @author gaignard
 */
public class Util {
    
//    public static boolean provEnabled = true;
    public static String provPrefix = "http://www.w3.org/ns/prov#";

    public static List<Filter> getApplicableFilter(Environment env, Edge edge) {
        // KGRAM exp for current edge
        Exp exp = env.getExp();;
        List<Filter> matchingFilters = new ArrayList<Filter>();
        for (Filter f : exp.getFilters()) {
            // filters attached to current edge
            if (f.getExp().isExist()) {
                // skip exists { PAT }
                continue;
            }

            if (exp.bind(f)) {
                matchingFilters.add(f);
            }
        }

//         workaround for non gathered filters
        if (matchingFilters.isEmpty()) {
            for (Exp e : env.getQuery().getBody()) {
                if (e.isFilter()) {
                    Filter kgFilter = e.getFilter();
//                    System.out.println("\t\tFilter " + kgFilter.toString() + " -> " + edge);
                    if (bound(edge, kgFilter)) {
//                        System.out.println("\t\t\t BOUND");
                        matchingFilters.add(kgFilter);
                    }
                }
            }
        }

        return matchingFilters;
    }

    public static List<fr.inria.acacia.corese.triple.parser.Exp> getApplicableFilter(List<fr.inria.acacia.corese.triple.parser.Exp> filters, BasicGraphPattern bgp) {
        //TODO handle conjunctive/disjunctive filters
        List<fr.inria.acacia.corese.triple.parser.Exp> matchingFilters = new ArrayList<fr.inria.acacia.corese.triple.parser.Exp>();

        for (fr.inria.acacia.corese.triple.parser.Exp filter : filters) {
            for (fr.inria.acacia.corese.triple.parser.Exp exp : bgp.getBody()) {
                if (bound(exp, filter)) {
                    matchingFilters.add(filter);
                }
            }
        }

        return matchingFilters;
    }

    /*
     * ?x p ?y FILTER ((?x > 10) && (?z > 10))
     * 
     * !!!!!!
     * ENV :: ?z = {0,2}
     * TODO ?x p ?y FILTER ((?x > ?z) && (?z > 10))
     * !!!!!!
     */
    public static boolean bound(Edge edge, Filter filter) {
        List<String> varsEdge = new ArrayList<String>();
        if (edge.getNode(0).isVariable()) {
            varsEdge.add(edge.getNode(0).toString());
        }
        if (edge.getNode(1).isVariable()) {
            varsEdge.add(edge.getNode(1).toString());
        }

        List<String> varsFilter = filter.getVariables();
        for (String var : varsFilter) {
            if (!varsEdge.contains(var)) {
                return false;
            }
        }
        return true;
    }

    public static boolean bound(fr.inria.acacia.corese.triple.parser.Exp triple, fr.inria.acacia.corese.triple.parser.Exp filter) {
        List<String> varsTriple = new ArrayList<String>();
        Triple t = triple.getTriple();
        if (t.getSubject().isVariable()) {
            varsTriple.add(t.getSubject().toSparql());
        }
        if (t.getPredicate().isVariable()) {
            varsTriple.add(t.getPredicate().toSparql());
        }
        if (t.getObject().isVariable()) {
            varsTriple.add(t.getObject().toSparql());
        }

        //TODO not working
//        List<String> varsFilter = filter.getVariables();
        List<String> varsFilter = new ArrayList<String>();

        for (Expression arg : filter.getTriple().getFilter().getArgs()) {
            if (arg.isVariable()) {
                varsFilter.add(arg.toSparql());
            }
        }


        for (String var : varsFilter) {
            if (!varsTriple.contains(var)) {
                return false;
            }
        }
        return true;
    }

    public static String prettyPrintCounter(ConcurrentHashMap<String, Long> map) {
        String res = "";
        Long sum = 0L;
        res+=("=========\n\t");
        for (String key : map.keySet()) {
            sum+=map.get(key);
            res+=(key)+"\n\t";
            res+=(map.get(key))+"\n";
            res+=("--\n\t");
        }
        res+=(">====  Sum = "+sum+"  =====\n");
        return res;
    }
}