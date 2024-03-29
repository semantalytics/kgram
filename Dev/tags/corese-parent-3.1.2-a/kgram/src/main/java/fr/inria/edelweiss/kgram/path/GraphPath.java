package fr.inria.edelweiss.kgram.path;


import fr.inria.edelweiss.kgram.api.query.Environment;

/**
 * Draft to compute path in the graph
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 */
public class GraphPath extends Thread {
	

	//private Buffer buf;
	private Buffer mbuf;
	private Environment mem;
	private PathFinder finder;


	
	/**
	 * ?x %path ?y
	 * case:
	 * ?x or ?y is bound/unbound
	 * filter on ?x ?y
	 * Relation type on %path : ?x c:related::%path ?y
	 * 
	 */
	
	public GraphPath(PathFinder pc, Environment mem, Buffer buf){
		this.finder  = pc;
		this.mem = mem;
		this.mbuf  = buf;
	}
	
	void process(){
		 finder.process(mem);
	}
	
	
	public void run(){
		process();
	}
}
