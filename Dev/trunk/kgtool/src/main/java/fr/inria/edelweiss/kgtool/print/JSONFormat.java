package fr.inria.edelweiss.kgtool.print;

import java.text.NumberFormat;
import java.util.Vector;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * SPARQL JSON Result Format for KGRAM Mappings
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class JSONFormat extends XMLFormat {

    private static final String OHEADER = "{";
    private static final String CHEADER = "}";
    private static final String OHEAD = "\"head\": { \n\"vars\": [";
    private static final String CHEAD = "] },";
    private static final String OHEADASK = "\"head\": { } ,";
    private static final String CHEADASK = "";
    private static final String OVAR = "";
    private static final String CVAR = ",";
    private static final String ORESULTS = "\"results\": { \"bindings\": [";
    private static final String CRESULTS = "] }";
    private static final String ORESULT = "{";
    private static final String CRESULT = "\n}";
    private static final String BOOLEAN = "\"boolean\" : ";
    private static final String BLANK = "_:";

    int nBind = 0, nResult = 0;
    NumberFormat nf = NumberFormat.getInstance();

    JSONFormat(Mappings lm) {
        super(lm);
    }

    JSONFormat() {
    }

    public static JSONFormat create(Mappings lm) {
        Query q = lm.getQuery();
        return JSONFormat.create(q, (ASTQuery) q.getAST(), lm);
    }

    public static JSONFormat create(Query q, ASTQuery ast, Mappings lm) {
        JSONFormat res;
        res = new JSONFormat(lm);
        res.setQuery(q);
        res.setAST(ast);
        return res;
    }

    public String getTitle(Title t) {
        switch (t) {
            case OHEADER:
                return OHEADER;
            case CHEADER:
                return CHEADER;
            case OHEAD:
                if (ast.isAsk()) {
                    return OHEADASK;
                }
                return OHEAD;
            case CHEAD:
                if (ast.isAsk()) {
                    return CHEADASK;
                }
                return CHEAD;
            case OVAR:
                return OVAR;
            case CVAR:
                return CVAR;
            case ORESULTS:
                return ORESULTS;
            case CRESULTS:
                return CRESULTS;
            case ORESULT:
                return ORESULT;
            case CRESULT:
                return CRESULT;
            default:
                return "";
        }
    }

    void printVariables(Vector<String> select) {
        int n = 1;
        for (String var : select) {
            print("\"" + getName(var) + "\"");
            if (n++ < select.size()) {
                print(", ");
            }
        }
    }

    public void printAsk() {
        String res = "true";
        if (lMap == null || lMap.size() == 0) {
            res = "false";
        }
        print(BOOLEAN);
        println(res);
    }

    void display(String var, IDatatype dt) {
        if (dt == null) {
            // do nothing
            return;
        }
        int i = 0, n = 1;
        if (getnBind() > 0) {
            print(",\n");
        }
        incrnBind();
        String name = getName(var);

        String open = "";
        if (i == 0) {
            open = "\"" + name + "\": ";
            if (n > 1) {
                open += "[";
            }
        }

        open += "{ \"type\": ";
        print(open);
        String str = dt.getLabel();

        if (dt.isLiteral()) {
            if (dt.hasLang()) {
                print("\"literal\", \"xml:lang\": \"" + dt.getLang() + "\"");
            } else if (dt.getCode() == IDatatype.LITERAL) {
                print("\"literal\"");
            } else {
                if (DatatypeMap.isDouble(dt)) {
                    str = nf.format(dt.doubleValue());
                }
                print("\"typed-literal\", \"datatype\": \"" + dt.getDatatype().getLabel() + "\"");
            }
        } else if (dt.isBlank()) {
            print("\"bnode\"");
            if (str.startsWith(BLANK)) {
                str = str.substring(BLANK.length());
            }
        } else {
            print("\"uri\"");
        }

        print(", \"value\": \"" + Constant.addEscapes(str) + "\"}");

        if (n > 1 && i == n - 1) {
            print("]");
        }
    }

    void newResult() {
        nBind = 0;
        if (nResult++ > 0) {
            print(",\n");
        }
    }

    void incrnBind() {
        nBind++;
    }

    int getnBind() {
        return nBind;
    }

    void printLink(String name) {
        print("\"link\": [\"" + name + "\"],");
    }
}
