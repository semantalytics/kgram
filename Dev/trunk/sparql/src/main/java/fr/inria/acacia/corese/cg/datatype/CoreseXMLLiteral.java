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
 * An implementation of the rdf:XMLLiteral datatype used by Corese
 * <br>
 * @author Olivier Corby
 */

public class CoreseXMLLiteral extends CoreseStringLiteral {
    static int code=XMLLITERAL;
    static final CoreseURI datatype=new CoreseURI(RDF.XMLLITERAL);
    //  to store an object such as an XML DOM (see xslt() xpath())
	private Object object; 

    public CoreseXMLLiteral(String value) {
    	super(value);
    }
    
    public void setObject(Object obj){
    	object = obj;
    }
    
    public boolean isXMLLiteral(){
		return true;
	}
    
    public Object getObject(){
    	return object;
    }
    
    public  int getCode(){
    	return code;
    }
    
    public IDatatype getDatatype(){
    	return datatype;
    }
    
    // TBD: it should parse the XML content
    public IDatatype typeCheck(){
        if (getLabel().startsWith("<") 
                && ! getLabel().endsWith(">")){
            return DatatypeMap.createUndef(getLabel(), RDF.XMLLITERAL);
        }
        return this;
    }
    
    
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
    	switch (iod.getCode()){
    	case XMLLITERAL: return getLabel().equals(iod.getLabel());
    	case URI:
    	case BLANK: return false;
    	}
    	throw failure();
    }
  
}