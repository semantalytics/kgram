package fr.inria.edelweiss.kgram.api.query;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EventManager;

/**
 * Interface of the binding environment provided by KGRAM
 * e.g. for filter Evaluator
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Environment {
	
	
	/**
	 * Return current query
	 * @return
	 */
	Query getQuery();
	
	/**
	 * 
	 * @return current graph node (only for filter interpreter)
	 */
	Node getGraphNode();
	
	/**
	 * Return the target node of variable var
	 */ 
	Node getNode(Expr var);
	
	/**
	 * Return the target node bound to query node with label 
	 * @param label
	 * @return
	 */
	Node getNode(String label);

	/**
	 * Return the target node bound to query node with label 
	 * @param label
	 * @return
	 */
	Node getNode(Node qNode);
	
	/**
	 * Return the query node at index n
	 * @param n
	 * @return
	 */
	Node getQueryNode(int n);

	/**
	 * Return the query node with label
	 * @param n
	 * @return
	 */
	Node getQueryNode(String label);

	/**
	 * Test whether query node is bound 
	 * @param qNode
	 * @return
	 */
	boolean isBound(Node qNode);
	
		
	/**
	 * Return the path length corresponding to query node
	 * 
	 * @param qNode
	 * @return
	 */
	int pathLength(Node qNode);

	 // aggregates

	/**
	 * Count the number of non null values of query node
	 * count duplicates
	 */
	
	int count();
	

	/**
	 * Return the max value of query node in every mapping.
	 * Take group by into account if any
	 * @param qNode
	 * @return
	 */
	//Node max(Node qNode);
	
	/**
	 * Return the min value of query node in every mapping.
	 * Take group by into account if any
	 * @param qNode
	 * @return
	 */
	//Node min(Node qNode);
	
	/**
	 * Run the eval function of the evaluator with filter f on every Mapping 
	 * use case: select sum(?x) as ?sum
	 * 
	 * @param eval
	 * @param f
	 */
	void aggregate(Evaluator eval, Filter f);
	
	EventManager getEventManager();
	
	boolean hasEventManager();
	
	void setObject(Object o);
	
	Object getObject();
	
	
}
