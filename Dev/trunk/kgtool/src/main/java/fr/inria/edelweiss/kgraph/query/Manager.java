package fr.inria.edelweiss.kgraph.query;


import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;


/**
 * SPARQL 1.1 Update
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public interface Manager {

        // basic operations: load, copy, etc.
	boolean process(Query q, Basic b, Dataset ds);

        /**
         * For each Mapping
         *   instantiate delete/insert template and delete/insert it 
         * For insert with blank, each mapping generates a new blank 
         * template may contain a graph pattern
         * if there is a dataset with from and no graph pattern, delete edges in dataset from
         * (sparql compliance)
         * 
         */
        void delete(Query q, Mappings map, Dataset ds);

        void insert(Query q, Mappings map, Dataset ds);
        	

}
