package fr.inria.edelweiss.kgenv.parser;

import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;

/**
 * Compiler API for Transformer
 * Generate target Edge/Node/Filter
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */

public interface Compiler {
	
	void setAST(ASTQuery ast);
	
	boolean isFail();
			
	Node createNode(String name);
	
	Node createNode(Atom at);

	Node getNode();
	
	Edge getEdge();
	
	List<Filter> getFilters();
	
	Filter compile(Expression exp);

	List<Filter> compileFilter(Triple t);
	
	Edge compile(Triple t);
	
	Regex getRegex(Filter f);
		
	String getMode(Filter f);

	int getMin(Filter f);
	
	int getMax(Filter f);
	
	
	
	
	
	
	
	
	
	


}
