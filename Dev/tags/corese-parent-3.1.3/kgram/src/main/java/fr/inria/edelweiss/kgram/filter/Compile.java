package fr.inria.edelweiss.kgram.filter;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Filter Exp Compiler for Optimizations
 * If Filter leads to optimization, we tag the Filter Exp 
 * with e.g. a BIND(?x = ?y)
 * Query.bind() will handle later this tag BIND
 * 
 * (1)
 * x p t . y p z . filter(y = x)
 * ->
 * x p t . bind(y, x) . y p z
 * 
 * (2)
 * x p t filter(?x = <uri>)
 * ->
 * bind(x, <uri>) . x p t
 * 
 * (3)
 * x p t filter(?x = <uri> || ?x = <uri2>)
 * ->
 * bind(x, (<uri>, <uri2>)) . x p t
 * 
 * (4)
 * optional{} !bound(?x)
 * !bound() backjump before optional{}
 * if there is no optional, !bound() backjump to edge before where ?x is bound
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Compile implements ExprType {
	
	Query query;
	Matcher matcher;
	Checker checker;
	
	public Compile(Query q){
		query = q;
		matcher = new Matcher();
		checker = new Checker(query);
	}
	
	public boolean check(String v1, String v2, Exp exp){
		return checker.check(v1, v2, exp.getFilter().getExp());
	}
	
	public boolean check(Exp exp){
		return checker.check(exp.getFilter().getExp());
	}
	
	public boolean check(Exp exp1, Exp exp2){
		return checker.check(exp1.getFilter().getExp(), exp2.getFilter().getExp());
	}
	
	public void test(Regex exp){
		if (exp instanceof Expr)
			checker.test((Expr) exp);
	}

	/**
	 * exp is a FILTER Exp
	 * when !bound(?x) : get Node ?x for optimizing backjump with optional
	 * when ?x = ?y : get list Node, for optimizing edge 
	 * TODO:
	 * assign filter to edge and put edge first in its component:
	 * x p t . y p z filter(y = x)
	 * we can bind y = x before enumerate y p z
	 */
	public void process(Query q, Exp exp){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();
				
		switch (ee.oper()){

		case NOT: 
                    if (! q.isOptional()){
                        // no use to detect !bound(?x) with SPARQL semantics
                        // because std backtrack is OK in this case
			not(exp);
                    }
			break;
						
		case OR:
			or(exp);
			break;
			
		case EQ:
			eq(exp);
			break;
			
			
		default:
			if (matcher.isGL(ee.oper())){
				gl(exp);
			}
			
		}
		
	}
	
	/**
	 * ! bound(?x)
	 */
	void not(Exp exp){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();
		Pattern pat = new Pattern(BOOLEAN, NOT, new Pattern(FUNCTION, BOUND, VARIABLE));
		if (matcher.match(pat, ee)){ 
			// ! bound(?x)
			List<String> list = ff.getVariables();
			if (list.size()>0){
				Node node = query.getProperAndSubSelectNode(list.get(0));
				if (node != null){
					exp.setNode(node);
					exp.status(true);
				}
			}
		}
	}
	

	
	/**
	 *  ?x = ?y
	 *  ?x = cst
	 */
	void eq(Exp exp){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();

		// ?x = ?y
		Pattern pat = new Pattern(TERM, EQ, VARIABLE, VARIABLE);
		if (matcher.match(pat, ee)){
			// compute Node list corresponding to variables
			List<Node> lNode = query.getNodes(exp);
			if (lNode.size()==2){
				Exp bind = Exp.create(Exp.BIND);
				for (Node qNode : lNode){
					Exp var = Exp.create(Exp.NODE, qNode);
					bind.add(var);
				}
				exp.add(bind);
			}
		}
		// ?x = 'constant'
		else if (matcher.match(new Pattern(TERM, EQ, VARIABLE, CONSTANT), ee)){ 
			Exp bind = buildCst(exp, Exp.BIND);
			if (bind != null){
				exp.add(bind);		
			}
		}
	}
	

	
	Exp buildCst(Exp exp, int type){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();
		Node node = query.getProperAndSubSelectNode(ff.getVariables().get(0));
		if (node != null){
			// variable ?x
			Exp bind = Exp.create(type, Exp.create(Exp.NODE, node));
			List<Expr> list = new ArrayList<Expr>();
			list.add(getConstants(ee).get(0));
			bind.setObject(list);
			return bind;
		}
		return null;
	}
	
	Exp buildVar(Exp exp, int type){
		List<Node> lNode = query.getNodes(exp);
		if (lNode.size()==2){
			Exp bind = Exp.create(type);
			for (Node node : lNode){			
				bind.add(Exp.create(Exp.NODE, node));
			}
			return bind;
		}
		return null;
	}
	
	
	
	/**
	 * VAR < CST 
	 */
	void gl(Exp exp){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();
		Pattern pat = new Pattern(TERM, GL, VARIABLE, CONSTANT);
		if (matcher.match(pat, ee)){
			Exp  test = buildCst(exp, Exp.TEST);
			if (test!=null)
				exp.add(test);
		}
		else {
			pat = new Pattern(TERM, GL, VARIABLE, VARIABLE);
			if (matcher.match(pat, ee)){
				Exp  test = buildVar(exp, Exp.TEST);
				if (test!=null)
					exp.add(test);
			}
		}
		
	}
	
	 /**  
	  * ?x = cst1 || ?x = cst2 
	  */
	void or(Exp exp){
		Filter ff = exp.getFilter();
		Expr ee = ff.getExp();
		Pattern pat = new Pattern(BOOLEAN, OR, new Pattern(TERM, EQ, VARIABLE, CONSTANT));
		pat.setRec(true);
		pat.setMatchConstant(false);
		if (matcher.match(pat, ee)) {
			Node node = query.getProperAndSubSelectNode(ff.getVariables().get(0));
			if (node != null){
				List<Expr> list = getConstants(ee);
				Exp bind = Exp.create(Exp.BIND, Exp.create(Exp.NODE, node));
				bind.setObject(list);
				exp.add(bind);
			}
		}
		
	}

	
	List<Expr> getConstants(Expr exp){
		List<Expr> list = new ArrayList<Expr>();
		return getConstants(exp, list);
	}
	
	
	List<Expr> getConstants(Expr exp, List<Expr> list){
		switch (exp.type()){
		
		case CONSTANT:  			//System.out.println(exp);
			list.add(exp); break;
			
		case VARIABLE: break;
		
		default:
			for (Expr ee : exp.getExpList()){
				getConstants(ee, list);
			}
		}
		return list;
	}
	
	
	
	public boolean isLocal(Filter f){
		return f.isBound() || 
			f.getExp().oper()  == DEBUG;
	}
}
