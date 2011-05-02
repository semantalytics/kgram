package fr.inria.edelweiss.kgram.api.query;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 * Interface for the connector that evaluates filters
 * 
* @author Olivier Corby, Edelweiss, INRIA 2010
*
*/
public interface Evaluator {
	
	static final int KGRAM_MODE 	= 0;
	static final int SPARQL_MODE  	= 1;

	
	void setMode(int mode);
	
	int getMode();

	
	/**
	 * Evaluate a filter 
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	boolean test(Filter f, Environment e);
	
	/**
	 * Evaluate a filter and return a Node
	 * use case: select fun(?x) as ?y
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	Node eval(Filter f, Environment e);
	
	Object eval(Expr f, Environment e);

	
	/**
	 * Evaluate a filter and return a list of Node
	 * use case: ?doc xpath('/book/title') ?title
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	List<Node> evalList(Filter f, Environment e);
	
	/**
	 * Evaluate an extension function filter and return Mappings
	 * use case: select sql('select from where') as (?x ?y) where {}
	 * TODO: should be an interface instead of Mappings
	 * 
	 * @param f
	 * @param e
	 * @param nodes
	 * @return
	 */
	Mappings eval(Filter f, Environment e, List<Node> nodes);


}
