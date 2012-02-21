package fr.inria.edelweiss.kgtool.print;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;

public class TSVFormat extends CSVFormat {
	
	// The end-of-line in TSV is EOL i.e. Unicode codepoint 10 (0x0A).
	static final String EOL = "\n";
	static final String SEP = "	";

	TSVFormat(Mappings m){
		super(m);
	}
	
	public static TSVFormat create(Mappings m){
		return new TSVFormat(m);
	}
	
	String getLabel(Node node){
		if (node.getValue() instanceof IDatatype){
			IDatatype dt = (IDatatype) node.getValue();
			if (dt.isNumber()){
				return dt.getLabel();
			}
			if (dt.getCode() == IDatatype.LITERAL && ! dt.hasLang()){
				// untyped plain literal
				return QUOTE + dt.getLabel() + QUOTE;
			}
			return dt.toSparql(false);
		}
		return node.toString();
	}
	
	String eol(){
		return EOL;
	}
	
	String sep(){
		return SEP;
	}
	
	String getVariable(String var){
		return var;
	}

}
