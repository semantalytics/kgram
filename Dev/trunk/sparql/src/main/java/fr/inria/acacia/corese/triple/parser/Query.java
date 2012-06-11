package fr.inria.acacia.corese.triple.parser;

public class Query extends Exp {
	
	ASTQuery ast;
	
	Query(){}
	
	Query(ASTQuery a){
		ast = a;
		add(ast.getBody());
	}
	
	public StringBuffer toString(StringBuffer sb){
		sb.append(ast.toString());
		return sb;
	}
	
	public static Query create(ASTQuery a){
		return new Query(a);
	}
	
	public ASTQuery getQuery(){
		return ast;
	}
	
	public boolean isQuery(){
		return true;
	}
	
	
	/**
	 * If Subquery is a bind, check scope.
	 */
	public boolean validate(ASTQuery a, boolean exist){
		
		if (ast.isBind()){
			Variable var = ast.getSelectVar().get(0);
			if (a.isBound(var)){
				a.addError("Scope error: " + var);
				a.setCorrect(false);
				return false;
			}
		}
				
		boolean b = ast.validate();
		
		if (! b){
			a.setCorrect(false);
		}
		
		for (Variable var : ast.getSelectVar()){
			a.bind(var);
			a.defSelect(var);
		}
		// select *
		for (Variable var : ast.getSelectAllVar()){
			a.bind(var);
			a.defSelect(var);
		}
				
		return b;
	}


}
