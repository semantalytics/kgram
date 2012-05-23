package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.cst.KeywordPP;

public class Join extends And {
	
	public static Join create(Exp e1, Exp e2){
		Join e = new Join();
		e.add(e1);
		e.add(e2);
		return e;
	}
	
	
	public boolean isJoin(){
		return true;
	}
	
	public StringBuffer toString(StringBuffer sb){
		sb.append(get(0));
		//sb.append(" " + KeywordPP.JOIN + " ");
		sb.append(get(1));
		return sb;
	}
	
	
}
