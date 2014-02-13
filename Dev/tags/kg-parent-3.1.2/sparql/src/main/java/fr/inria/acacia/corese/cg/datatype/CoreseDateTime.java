package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

public class CoreseDateTime extends CoreseDate {
	
	static final CoreseURI datatype=new CoreseURI(RDF.xsddateTime);
	
	public CoreseDateTime(String label) throws CoreseDatatypeException{
		super(label);
	}
	
	public CoreseDateTime()throws CoreseDatatypeException{
		this(TODAY);
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}

}
