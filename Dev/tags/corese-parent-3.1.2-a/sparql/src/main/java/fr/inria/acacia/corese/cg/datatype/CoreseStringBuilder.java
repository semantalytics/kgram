package fr.inria.acacia.corese.cg.datatype;

public class CoreseStringBuilder extends CoreseString {
	
	StringBuilder sb;
	
	CoreseStringBuilder(StringBuilder s){
		sb = s;
		value = null;
	}
	
	public static CoreseStringBuilder create(StringBuilder s){
		return new CoreseStringBuilder(s);
	}
	
	public String getLabel(){
		if (value == null){
			value = sb.toString();
		}
		return value;
	}
	
	public StringBuilder getStringBuilder(){
		return sb;
	}

}
