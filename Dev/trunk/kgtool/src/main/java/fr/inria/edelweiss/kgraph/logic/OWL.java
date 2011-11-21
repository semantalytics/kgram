package fr.inria.edelweiss.kgraph.logic;

public interface OWL {
	
	public static final String OWL   =  "http://www.w3.org/2002/07/owl#";

	public static final String CLASS  	 	= OWL + "Class";
	public static final String THING  		= OWL + "Thing";
	
	public static final String INTERSECTIONOF   = OWL + "intersectionOf";
	public static final String UNIONOF    		= OWL + "unionOf";
	public static final String INVERSEOF 	= OWL + "inverseOf";
	public static final String SYMMETRIC  	= OWL + "SymmetricProperty";
	public static final String TRANSITIVE 	= OWL + "TransitiveProperty";
	public static final String REFLEXIVE  	= OWL + "ReflexiveProperty";
	public static final String TOPOBJECTPROPERTY  = OWL + "topObjectProperty";
	public static final String TOPDATAPROPERTY    = OWL + "topDataProperty";
	

}
