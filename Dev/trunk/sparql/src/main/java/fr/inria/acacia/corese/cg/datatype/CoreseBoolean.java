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
 * An implementation of the xsd:boolean datatype used by Corese
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class CoreseBoolean extends CoreseStringLiteral {
  static int  code=BOOLEAN;
  static String STRUE="true";
  static String SFALSE="false";
  public static final CoreseBoolean TRUE =  new CoreseBoolean(true);
  public static final CoreseBoolean FALSE = new CoreseBoolean(false);
  //static String UNKNOWN="unknown";
  static final CoreseURI datatype=new CoreseURI(RDF.xsdboolean);
  boolean bvalue=true;

  /**
   * Construct a Corese boolean
   * @param value <code>true</code> or <code>false</code>
   */
  public CoreseBoolean(String value) throws CoreseDatatypeException {
      super(getNormalizedLabel(value));
      if (!parse(value))
      throw new CoreseDatatypeException("Boolean", value);
  }
  
  public CoreseBoolean(boolean b)  {
      super((b)?STRUE:SFALSE);
      bvalue=b;
  }
  
  boolean parse(String value){
	  if (value.equals(SFALSE) || value.equals("0")){
    	  bvalue=false;
    	  return true;
      }
      else if (value.equals(STRUE) || value.equals("1")){
          bvalue=true;
          return true;
      }
	  return false;
  }

  public boolean isTrue() {
     return bvalue;
   }

   public boolean isTrueAble() {
     return true;
   }
   
   int value(){
	   return (bvalue)?1:0;
   }

   /**
    * Cast a boolean to integer return 0/1
    */
   public IDatatype cast(IDatatype target, IDatatype javaType) {
	   String lab = target.getLabel();
	   if (lab.equals(RDF.xsdinteger)) {
		   return new CoreseInteger(value());
	   } else if (lab.equals(RDF.xsdfloat)) {
		   return new CoreseFloat(value());
	   } else if (lab.equals(RDF.xsddouble)) {
		   return new CoreseDouble(value());
	   } else if (lab.equals(RDF.xsddecimal)) {
		   return new CoreseDecimal(value());
	   }
	   else return super.cast(target, javaType);
   }


  public  int getCode(){
     return code;
   }

   public IDatatype getDatatype(){
        return datatype;
      }


  /**
   * Normalized following the W3C xsd specification the label
   * @param label may be <code>true</code>, <code>false</code> or <code>1</code> or <code>0</code>
   * @return <code>true</code> or <code>false</code>
   */
  public static String getNormalizedLabel(String label){
        if (label.equals(STRUE) || label.equals("1"))
          return STRUE;
        else if (label.equals(SFALSE) || label.equals("0"))
          return SFALSE;
        //else return UNKNOWN;
        else return null;
    }

  public boolean equals(IDatatype iod) 
  throws CoreseDatatypeException {
	  return iod.polymorphEquals(this);
  }
  
  public boolean polymorphEquals(CoreseBoolean icod) 
  throws CoreseDatatypeException {
	  return   getValue().compareTo(icod.getValue()) == 0;
  }


}
