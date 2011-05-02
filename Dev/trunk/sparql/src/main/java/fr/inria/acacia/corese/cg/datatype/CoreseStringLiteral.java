package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * Subsume String Literal XMLLiteral UndefLiteral and Boolean
 * In Corese they compare with <= < >= >  but not with = !=
 */

public class CoreseStringLiteral extends CoreseStringableImpl{
  static int code=STRING;


  public CoreseStringLiteral() {}

  public CoreseStringLiteral(String value) {
      super(value);

  }
  
  public String toString2(){
      String str = getNormalizedLabel();
      if (getDatatype() != null) str += "^^" + getDatatype();
      if (getLang() != null)     str += "@"  + getLang();
      
      return str;
    }

  /**
   * semiEquals do not look at @ lang for 2 String Literal  only
   * otherwise compute equals
   */
  public boolean semiEquals(IDatatype iod) {
    if (iod instanceof CoreseStringLiteral){
      CoreseStringLiteral lit=(CoreseStringLiteral)iod;
      boolean b2 = getValue().compareTo(lit.getValue()) == 0;
      return b2;
    }
    else return sameTerm(iod);
  }
  
  
  public int compare(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polyCompare(this);
  }
  
  public int polyCompare(CoreseStringLiteral icod) throws CoreseDatatypeException {
	  return   icod.intCompare(this);
  }


  public boolean less(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polymorphGreater(this);
  }
  
  public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
	  return iod.polymorphGreaterOrEqual(this);
  }
  
  public boolean greater(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polymorphLess(this);
  }
  
  public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polymorphLessOrEqual(this);
  }
  
  
  public boolean polymorphGreaterOrEqual(CoreseStringLiteral icod) throws
  CoreseDatatypeException {
	  return intCompare(icod) >= 0;
  }
  
  public boolean polymorphGreater(CoreseStringLiteral icod)
  throws CoreseDatatypeException {
	  return intCompare(icod) > 0;
  }
  
  public boolean polymorphLessOrEqual(CoreseStringLiteral icod)
  throws CoreseDatatypeException {
	  return intCompare(icod) <= 0;
  }
  
  public boolean polymorphLess(CoreseStringLiteral icod)
  throws CoreseDatatypeException {
	  return intCompare(icod) < 0;
  }

 

}