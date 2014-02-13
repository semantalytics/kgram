package fr.inria.acacia.corese.triple.update;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Statement;

/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Update  extends Statement {
	
	static final String[] NAME = 
	{"load", "clear", "drop", "create", "add", "move", "copy", "prolog",
	 "insert", "delete", "composite"};

	
	public static final int LOAD 	= 0;
	public static final int CLEAR 	= 1;
	public static final int DROP 	= 2;
	public static final int CREATE  = 3;
	public static final int ADD 	= 4;
	public static final int MOVE 	= 5;
	public static final int COPY 	= 6;
	public static final int PROLOG 	= 7;

	public static final int INSERT 		= 8;
	public static final int DELETE 		= 9;
	public static final int COMPOSITE 	= 10;

	
	int type;
	ASTUpdate astu;
	// Update operation may have a local prolog
	// otherwise use the global one
	private NSManager nsm;
	
	
	
	String title(){
		return NAME[type];
	}
	
	public int type(){
		return type;
	}
	
	void set(ASTUpdate a){
		astu = a;
	}
	
	public ASTUpdate getASTUpdate(){
		return astu;
	}
	
	public ASTQuery getASTQuery(){
		return astu.getASTQuery();
	}
	
	public String expand(String name){
		if (name == null) return null;
		String res = getNSM().toNamespaceB(name);
		return res;
	}
	
	public boolean isComposite(){
		return this instanceof Composite;
	}
	
	public boolean isBasic(){
		return this instanceof Basic;
	}
	
	public Composite getComposite(){
		return (Composite) this;
	}

	public Basic getBasic(){
		return (Basic) this;
	}
	
	public void setLocalNSM(NSManager nsm){
		this.nsm = nsm;
	}
	
	public NSManager getLocalNSM(){
		return nsm;
	}
	
	/**
	 * Local or global NSM
	 * 
	 */
	public NSManager getNSM(){
		if (nsm != null){
			return nsm;
		}
		else {
			return getGlobalNSM();
		}
	}
	
	public NSManager getGlobalNSM(){
		return astu.getNSM();
	}
}
