package fr.inria.edelweiss.engine.model.api;

import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Triple;

public interface ExpFilter {
	
	
	public Expression getExpression();
	
	/**
	 * create a filter with the values of the variables in the object Bind
	 */
	public Triple createFilter(Bind bind);
	
	public Expression create(Bind bind);
	
	/**
	 * return true if the clause correspond to the expression filter
	 */
	public boolean isCorresponding(Clause clause, Bind bind);
	
	public boolean isCorresponding(Bind bind);

}
