package fr.inria.acacia.corese.triple.cst;

import fr.inria.acacia.corese.cg.datatype.RDF;

 public interface RDFS extends RDF {

	 static final String COSNS    = "http://www.inria.fr/acacia/corese";
	 static final String COSPrefix =  "cos";
	 static final String COS      = COSNS+"#";
	 static final String GETPrefix =  "get";
	 static final String EGETPrefix =  "eget";
	 static final String CHECKPrefix =  "check";
	 static final String EGETNS    = COSNS + "/eeval#";

	 static final String RootPropertyQN =  COSPrefix + ":Property"; // cos:Property
	 static final String RootPropertyURI = COS + "Property"; //"http://www.inria.fr/acacia/corese#Property";
	 static final String COSPRAGMANS    = COSNS+"/pragma";
	 static final String COSPRAGMA    	= COSPRAGMANS+"#";
	 static final String ACCEPT 		= COS+"accept";
	 static final String FROM 		= COS+"from";
	 static final String GETNS    = COSNS + "/eval#";
	 static final String CHECKNS  = COSNS + "/check#";
	 static final String PPBN    = COSNS+"/bn#"; // pprint Blank Node
	 static final String COSSUBSTATEOF = COSNS+"#subStateOf";

	
	 static final String qxsdInteger 	= "xsd:integer";
	 static final String qxsdDouble 	= "xsd:double";
	 static final String qxsdDecimal 	= "xsd:decimal";
	 static final String qxsdString 	= "xsd:string";
	 static final String qxsdBoolean 	= "xsd:boolean";
	 static final String qrdfsLiteral	=  "rdfs:Literal";
	 static final String qrdfsResource 	= "rdfs:Resource";

	 static final String qrdfFirst 	= "rdf:first";
	 static final String qrdfRest	= "rdf:rest";
	 static final String qrdfNil 	= "rdf:nil";
	 static final String rdftype    = "rdf:type";

	 static final String RDFRESOURCE 	= RDF+"resource";
	 static final String RDFOBJECT 		= RDF+"object";
	 static final String RDFSUBJECT 	= RDF+"subject";
	 static final String RDFTYPE 		= RDF+"type";

}
