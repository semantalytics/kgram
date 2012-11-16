package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Values;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

public class CompileService {
	

	
	Hashtable<String, Double> table;
	
	public CompileService(){
		table = new Hashtable<String, Double>();
	}

	/**
	 * Generate bindings for the service, if any
	 */
	public void compile(Node serv, Query q, Mappings lmap, Environment env, int start, int limit){
		ASTQuery ast = (ASTQuery) q.getAST();

		if (ast.getValues() != null){
			// subquery has its own bindings, skip compile
			return;
		}
		
		if (lmap == null){
			if (isSparql0(serv)){
				filter(q, env);
			}
			else {
				bindings(q, env);
			}
		}
		else if (isSparql0(serv)){
			filter(q, lmap, start, limit);
		}
		else {
			bindings(q, lmap, start, limit);
		}
		
	}
	
	public void prepare(Query q){
		Query g 	 = q.getOuterQuery();
		ASTQuery ast = (ASTQuery) q.getAST();
		ASTQuery ag  = (ASTQuery) g.getAST();
		ast.setPrefixExp(ag.getPrefixExp());
	}
	
	int slice(Query q){
		Query g  = q.getOuterQuery();
		return g.getSlice();
	}
	
	boolean isMap(Query q){
		Query g  = q.getOuterQuery();
		return g.isMap();
	}
	
	
	void set(String uri, double version){
		table.put(uri, version);
	}
	
	
	// everybody is 1.0 except localhost
	boolean isSparql0(Node serv){
		Double f = table.get(serv.getLabel());
		return (f == null || f == 1.0);
	}
	
	/**
	 * Search select variable of query that is bound in env
	 * Generate binding for such variable
	 * Set bindings in ASTQuery
	 */
	void bindings(Query q, Environment env){
		ASTQuery ast = (ASTQuery) q.getAST();
		ast.clearBindings();
		ArrayList<Variable> lvar = new ArrayList<Variable>();
		ArrayList<Constant> lval = new ArrayList<Constant>();

		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			Node val   = env.getNode(var);
			
			if (val != null){
				lvar.add(Variable.create(var));
				IDatatype dt = (IDatatype) val.getValue();
				Constant cst = Constant.create(dt);
				lval.add(cst);
			}
		}
		
		if (lvar.size()>0){
			Values values = Values.create(lvar, lval);
			ast.setValues(values);
		}
	}
	
	/**
	 * Generate bindings as bindings from Mappings
	 */
	void bindings(Query q, Mappings lmap, int start, int limit){
		ASTQuery ast = (ASTQuery) q.getAST();
		ast.clearBindings();
		ArrayList<Variable> lvar = new ArrayList<Variable>();
		ArrayList<Constant> lval; 
		Values values = Values.create();
		
		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			lvar.add(Variable.create(var));
		}
		
		values.setVariables(lvar);
				
		for (int j = start; j < lmap.size() && j < limit; j++){
			
			Mapping map = lmap.get(j);			
			boolean ok = false;
			lval = new ArrayList<Constant>();
			
			for (Node var : q.getSelect()){
				Node val = map.getNode(var);

				if (val != null){
					IDatatype dt = (IDatatype) val.getValue();
					Constant cst = Constant.create(dt);
					lval.add(cst);
					ok = true;
				}
				else {
					lval.add(null);
				}
			}
			
			if (ok){
				values.addValues(lval);
			}
		}
		
		if (values.getValues().size()>0){
			ast.setValues(values);
		}

	}
	
	
	/**
	 * Generate bindings as a string
	 *  values () {()} syntax 
	 */
	StringBuffer strBindings(Query q, Mappings map){
		String SPACE = " ";
		StringBuffer sb = new StringBuffer();
		
		sb.append("values (");
		
		for (Node qv : q.getSelect()){
			sb.append(qv.getLabel());
			sb.append(SPACE);
		}
		sb.append("){");
		
		for (Mapping m : map){
			sb.append("(");
			
			for (Node var : q.getSelect()){
				Node val = m.getNode(var);
				if (val == null){
					sb.append("UNDEF");
				}
				else {
					sb.append(val.getValue().toString());
				}
				sb.append(SPACE);
			}
			
			sb.append(")");
		}
		
		sb.append("}");
		return sb;

	}

	
	/**
	 * Search select variable of query that is bound in env
	 * Generate binding for such variable as filters
	 * Set filters in ASTQuery
	 */
	void filter(Query q, Environment env){
		ASTQuery ast = (ASTQuery) q.getAST();
		ArrayList<Term> lt = new ArrayList<Term>();

		for (Node qv : q.getSelect()){
			String var = qv.getLabel();
			Node val   = env.getNode(var);
			
			if (val != null){
				Variable v = Variable.create(var);
				IDatatype dt = (IDatatype) val.getValue();
				Constant cst = Constant.create(dt);
				Term t = Term.create(Term.SEQ, v, cst);
				lt.add(t);
			}
		}
				
		if (lt.size()>0){
			Term f = lt.get(0);
			for (int i = 1; i<lt.size(); i++){
				f = Term.create(Term.SEAND, f, lt.get(i));
			}
			
			if (ast.getSaveBody() == null){
				ast.setSaveBody(ast.getBody());
			}
			BasicGraphPattern body = BasicGraphPattern.create();
			body.add(ast.getSaveBody());
			body.add(Triple.create(f));
			ast.setBody(body);
		}
		
	}
	
	/**
	 * Generate bindings from Mappings as filter
	 */
	void filter(Query q, Mappings lmap, int start, int limit){
		ASTQuery ast = (ASTQuery) q.getAST();
		ArrayList<Term> lt;
		Term filter = null;
		//Group group =  Group.instance(q.getSelectFun());
		
		for (int j = start; j < lmap.size() && j < limit; j++){
			
			Mapping map = lmap.get(j);
			
//			if (! group.isDistinct(map)){
//				continue;
//			}
			
			lt = new ArrayList<Term>();
			
			for (Node qv : q.getSelect()){
				String var = qv.getLabel();
				Node val   = map.getNode(var);

				if (val != null){
					Variable v = Variable.create(var);
					IDatatype dt = (IDatatype) val.getValue();
					Constant cst = Constant.create(dt);
					Term t = Term.create(Term.SEQ, v, cst);
					lt.add(t);
				}
			}
		
				
			if (lt.size()>0){
				Term f = lt.get(0);
				
				for (int i = 1; i<lt.size(); i++){
					f = Term.create(Term.SEAND, f, lt.get(i));
				}
				
				if (filter == null){
					filter = f;
				}
				else {
					filter = Term.create(Term.SEOR, filter, f);
				}
			}
		
		}		
		
		if (ast.getSaveBody() == null){
			ast.setSaveBody(ast.getBody());
		}
		
		BasicGraphPattern body = BasicGraphPattern.create();
		body.add(ast.getSaveBody());
		if (filter != null) {
			body.add(Triple.create(filter));
		}
		ast.setBody(body);
		
	}


}
