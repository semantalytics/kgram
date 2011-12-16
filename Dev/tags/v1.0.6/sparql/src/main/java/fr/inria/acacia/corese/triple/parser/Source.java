package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class implements graph ?src { PATTERN }
 * <br>
 * @author Olivier Corby
 */

public class Source extends And {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	String source;
	Atom asource;
	boolean state = false;
	boolean leaf = false;
	private boolean isRec = false;

  public Source() {}

  /**
   * Model the scope of graph ?src { pattern }
   *
   */
  public Source(Atom src, Exp exp) {
    super(exp);
    source = src.getName();
    asource = src;
  }
  
  public static Source create(Atom src, Exp exp){
	  if (! exp.isAnd()){
		  exp = new BasicGraphPattern(exp);
	  }
	  Source s = new Source(src, exp);
	  return s;
  }
  
  public Atom getSource(){
	  return asource;
  }
  
  public void setState(boolean s){
	  state = s;
  }
  
  public void setLeaf(boolean s){
	  leaf = s;
  }
  
  boolean isState(){
	  return state;
  }
  
  public boolean isGraph(){
	  return true;
  }
  
  public void setRec(boolean b){
	  isRec = b;
  }
  
  public boolean isRec(){
	  return isRec;
  }
  
 
  Exp duplicate(){
    Source exp = new Source();
    exp.asource = asource;
    exp.source = source;
    exp.state = state;
    exp.leaf = leaf;
    exp.isRec = isRec;
    return exp;
  }


    public StringBuffer toString(StringBuffer sb) {
        sb.append(KeywordPP.GRAPH + KeywordPP.SPACE);
        if (state) sb.append(KeywordPP.STATE + KeywordPP.SPACE);
        sb.append(asource);
        sb.append(KeywordPP.SPACE);
        for (int i=0; i<size(); i++){
        	sb.append(eget(i).toString());
        }
        return sb;       
    }

 
  public boolean validateData(){
	  if (asource.isVariable()) return false;

	  Exp ee = this;
	  if (size() == 1 && get(0) instanceof And){
		  // dive into {}
		  ee = get(0);
	  }

	  for (Exp exp : ee.getBody()){
		  if (! (exp.isTriple() && exp.validateData())){
			  return false;
		  }
	  }
	  return true;
  }

  

}