package fr.inria.edelweiss.kgenv.eval;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.filter.Proxy;

/**
 * Interpreter that perfom aggregate over current group list of Mapping The eval
 * function is called by Mappings The eval() function can only compute
 * aggregates
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
class Walker extends Interpreter {

    static IDatatype ZERO = DatatypeMap.ZERO;
    Expr exp;
    Node qNode, tNode;
    IDatatype dtres;
    int num = 0, count = 0;
    boolean isError = false, first = true, and = true, isTemplateAgg = false;
    StringBuilder sb;
    String sep = " ";
    Group group;
    Distinct distinct;
    TreeData tree;
    Evaluator eval;
    private static final String NL = System.getProperty("line.separator");

    Walker(Expr exp, Node qNode, Proxy p, Environment env, Producer prod) {
        super(p);
        
        Query q = env.getQuery();
        if (q != null 
                && q.isNumbering()
                && q.isTemplate()
                && q.getTemplateGroup().getFilter().getExp() == exp){
             // final aggregate of template   
            isTemplateAgg = true;
        }
               
        eval = p.getEvaluator();
        this.exp = exp;
        this.qNode = qNode;

        if (exp.getArg() != null) {
            // separator as an evaluable expression: st:nl()
            IDatatype dt = (IDatatype) eval.eval(exp.getArg(), env, prod);
            sep = dt.getLabel() ;
        } 
        else {
            if (exp.getModality() != null) {
                sep = exp.getModality();
            }

            if (env.getQuery().isTemplate()) {
                if (sep.equals("\n") || sep.equals("\n\n")) {
                    // get the indentation by evaluating a predefined st:nl()
                    // computed by PluginImpl/Transformer
                    // same as: separator = 'st:nl()'
                    Expr nl = env.getQuery().getTemplateNL().getFilter().getExp();
                    IDatatype dt = (IDatatype) eval.eval(nl, env, prod);
                    String str = dt.getLabel();
                    if (sep.equals("\n\n")) {
                        str = NL + str;
                    }
                    sep = str;
                }
            }
        }
        
        sb = new StringBuilder();
        if (exp.isDistinct()) {
            // use case: count(distinct ?name)
            List<Node> nodes;
            if (exp.arity() == 0) {
                // count(distinct *)
                //nodes = env.getQuery().getNodes();
                nodes = env.getQuery().getSelectNodes();
            } else {
                nodes = env.getQuery().getAggNodes(exp.getFilter());
                // group_concat:
                distinct = new Distinct();
            }
            group = Group.create(nodes);
            group.setDistinct(true);
            tree = new TreeData();
        }
    }

    Object getResult() {
        if (isError) {
            return null;
        }

        switch (exp.oper()) {

            case SAMPLE:
            case MIN:
            case MAX:
                return dtres;


            case SUM:
                if (dtres == null) {
                    return ZERO;
                } else {
                    return dtres;
                }

            case AVG:
                if (dtres == null) {
                    return ZERO;
                }

                try {
                    Object dt = dtres.div(DatatypeMap.newInstance(num));
                    return dt;
                } catch (java.lang.ArithmeticException e) {
                    return null;
                }


            case COUNT:
                return proxy.getValue(num);

            case GROUPCONCAT:
            case STL_GROUPCONCAT:
                return DatatypeMap.newStringBuilder(sb);

            case AGGAND:
                return DatatypeMap.newInstance(and);

        }

        return null;
    }

    /**
     * if aggregate is distinct, check that map is distinct
     */
    boolean accept(Filter f, Mapping map) {
        if (f.getExp().isDistinct()) {
            boolean b = group.isDistinct(map);
            return b;
        }
        return true;
    }

    boolean accept(Filter f, IDatatype dt) {
        if (f.getExp().isDistinct()) {
            boolean b = tree.add(dt);
            return b;
        }
        return true;
    }

    boolean accept(Filter f, Tuple t) {
        if (f.getExp().isDistinct()) {
            boolean b = distinct.add(t);
            return b;
        }
        return true;
    }

    /**
     * map is a Mapping
     */
    public Node eval(Filter f, Environment env, Producer p) {
        Mapping map = (Mapping) env;

        switch (exp.oper()) {

            case GROUPCONCAT:
            case STL_GROUPCONCAT:
                boolean isDistinct = f.getExp().isDistinct();
                IDatatype[] value = null;
                Tuple t = null;
                if (isDistinct) {
                    value = new IDatatype[exp.getExpList().size()];
                    t = new Tuple(value);
                }

                StringBuffer res = new StringBuffer();

                if (count++ > 0) {
                    res.append(sep);
                }

                int i = 0;
                for (Expr arg : exp.getExpList()) {
                    
                    if (arg.oper() != ExprType.GROUPBY) {
                        // skip group_concat(?x, ?y, groupBy(?z))
                        IDatatype dt = (IDatatype) eval.eval(arg, map, p);
                        
                        if (dt != null && dt.isFuture()){
                                Expr ee = (Expr) dt.getObject();
                                // template ?out = future(concat(str, st:number(), str))
                                // eval(concat(str, st:number(), str))
                                dt = (IDatatype) eval.eval(ee, map, p);                           
                        }
                        
                        if (isDistinct) {
                            value[i++] = dt;
                        }

                        if (dt != null) {                              
                            if (dt.getStringBuilder() != null) {
                               res.append(dt.getStringBuilder());                               
                            } else {
                                res.append(dt.getLabel());
                            }
                        }
                    }
                }


                if (accept(f, t)) {
                    //res.append(sep);
                    sb.append(res);
                }

                return null;


            case COUNT:

                if (exp.arity() == 0) {
                    // count(*)
                    if (accept(f, map)) {
                        num++;
                    }
                    return null;
                }
        }


        if (exp.arity() == 0) {
            return null;
        }

        Expr arg = exp.getExp(0);
        Node node = null;
        IDatatype dt = null;

        if (arg.isVariable()) {
            // sum(?x)
            // get value from Node Mapping
            node = map.getTNode(qNode);
            if (node == null) {
                return null;
            }
            dt = (IDatatype) node.getValue();
        } else {
            // sum(?x + ?y)
            // eval ?x + ?y
            dt = (IDatatype) eval.eval(arg, map, p);
        }
        
        if (dt != null) {

            switch (exp.oper()) {

                case MIN:
                    if (dt.isBlank()) {
                        isError = true;
                        break;
                    }
                    if (dtres == null) {
                        dtres = dt;
                    } else {
                        try {
                            if (dt.less(dtres)) {
                                dtres = dt;
                            }

                        } catch (CoreseDatatypeException e) {
                        }
                    }
                    break;

                case MAX:
                    if (dt.isBlank()) {
                        isError = true;
                        break;
                    }
                    if (dtres == null) {
                        dtres = dt;
                    } else {
                        try {
                            if (dt.greater(dtres)) {
                                dtres = dt;
                            }
                        } catch (CoreseDatatypeException e) {
                        }
                    }
                    break;

                case COUNT:
                    if (accept(f, dt)) {
                        num++;
                    }
                    break;

                case SUM:
                case AVG:
                    if (!dt.isNumber()) {
                        isError = true;
                    } else if (accept(f, dt)) {
                        if (dtres == null) {
                            dtres = dt;
                        } else {
                            dtres = dtres.plus(dt);
                        }

                        num++;
                    }
                    break;

                case SAMPLE:
                    if (dtres == null && dt != null) {
                        dtres = dt;
                    }
                    break;

                case AGGAND:

                    if (dt == null) {
                        isError = true;
                    } else {
                        try {
                            boolean b = dt.isTrue();
                            and &= b;

                        } catch (CoreseDatatypeException ex) {
                            isError = true;                        
                       }
                    }

                    break;


            }
        }

        return null;
    }

    class TreeData extends TreeMap<IDatatype, IDatatype> {

        boolean hasNull = false;

        TreeData() {
            super(new Compare());
        }

        boolean add(IDatatype dt) {

            if (dt == null) {
                if (hasNull) {
                    return false;
                } else {
                    hasNull = true;
                    return true;
                }
            }

            if (containsKey(dt)) {
                return false;
            }
            put(dt, dt);
            return true;
        }
    }

    class Compare implements Comparator<IDatatype> {

        @Override
        public int compare(IDatatype o1, IDatatype o2) {
            return o1.compareTo(o2);
        }
    }

    class Distinct extends TreeMap<Tuple, Boolean> {

        Distinct() {
        }

        public boolean add(Tuple map) {

            if (containsKey(map)) {
                return false;
            }
            put(map, true);
            return true;
        }
    }
}