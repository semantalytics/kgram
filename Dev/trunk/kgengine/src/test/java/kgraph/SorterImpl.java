package kgraph;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Sort KGRAM edges in connected order before query process
 * Take cardinality into account
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class SorterImpl extends Sorter {
	
	Graph graph;
	
	SorterImpl(){}
	
	SorterImpl(Graph g){
		graph = g;
	}
	
//	public static SorterImpl create(){
//		return new SorterImpl();
//	}
	
	public static SorterImpl create(Graph g){
		return new SorterImpl(g);
	}
	

	public boolean leaveFirst(){
		return false;
	}
	
	

}
