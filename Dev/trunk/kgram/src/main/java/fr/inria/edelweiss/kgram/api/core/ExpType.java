package fr.inria.edelweiss.kgram.api.core;

/**
 * Types of expression of KGRAM query language
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface ExpType {
	
	public static final String KGRAM = "http://ns.inria.fr/edelweiss/2010/kgram/";
	public static final String KPREF = "kg";

	static final int EMPTY 	= 0;
	static final int AND 	= 1;
	static final int UNION 	= 2;
	static final int GRAPH 	= 3;
	static final int OPTION = 4; 
	static final int EDGE 	= 5; 
	static final int FILTER = 6; 
	static final int NODE 	= 7; 
	
	static final int NOT 		= 8; 
	static final int WATCH 		= 9; 
	static final int CONTINUE 	= 10; 
	static final int BACKJUMP 	= 11; 
	static final int EXTERN 	= 12;
	static final int QUERY 		= 13;
	static final int FORALL 	= 14;
	static final int EXIST  	= 15;
	static final int GRAPHNODE 	= 16;
	static final int OPTIONAL 	= 17; 
	static final int SCAN 		= 18; 
	static final int IF 		= 19; 
	static final int PATH 		= 20; 
	static final int XPATH 		= 21; 
	static final int ACCEPT 	= 22; 
	static final int BIND 		= 23; 
	static final int EVAL 		= 24; 
	static final int SCOPE 		= 25; 
	static final int TEST 		= 26; 
	static final int NEXT 		= 27; 
	static final int MINUS 		= 28; 
	static final int POP 		= 29; 
	static final int SERVICE 	= 30; 
	static final int RESTORE 	= 31; 



	
	static final String[] TITLE = {
		"EMPTY", "AND", "UNION", "GRAPH", "OPTION", "EDGE", "FILTER", "NODE", 
		"NOT", "WATCH", "CONTINUE", "BACKJUMP", "EXTERN", "QUERY", "FORALL", "EXIST",
		"GRAPHNODE", "OPTIONAL", "SCAN", "IF", "PATH", "XPATH", "ACCEPT", "BIND", 
		"EVAL", "SCOPE", "TEST", "NEXT", "MINUS", "POP", "SERVICE", "RESTORE"
	};
	
}
