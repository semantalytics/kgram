/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class CoreseInt extends CoreseInteger {
    
    	static final CoreseURI datatype=new CoreseURI(RDF.xsdint);

    
       public CoreseInt(String value) {
		super(value);
	}


	public CoreseInt(int value) {
		super(value);
	}
	
	
	public IDatatype getDatatype(){
		return datatype;
	}

}
