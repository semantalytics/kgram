package fr.inria.edelweiss.kgram.filter;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.List;

public interface Proxy {
	
	void setMode(int mode);
	
	void setEvaluator(Evaluator eval);
	
	Evaluator getEvaluator();

	boolean isTrueAble(Object value);

	boolean isTrue(Object value);

	// Query Node value (e.g. KGRAM IDatatype)  to target proxy value
	Object getConstantValue(Object value);

	Object getValue(boolean b);

	Object getValue(int value);
	
	Object getValue(float value);
	
	Object getValue(long value);
	
	Object getValue(double value);
	
	Object getValue(double value, String datatype);

	
	Object getValue(String value);



	// terms = <=
	Object eval(Expr exp, Environment env, Producer p, Object o1, Object o2);

	// functions isURI regex
	Object eval(Expr exp, Environment env, Producer p, Object[] args);
	
	Object function(Expr exp, Environment env, Producer p);

	Object function(Expr exp, Environment env, Producer p, Object o1);

	Object function(Expr exp, Environment env, Producer p, Object o1, Object o2);

	
	// apply sum(?x) over env mappings
	Object aggregate(Expr exp, Environment env, Producer p, Node qNode);

	// type operators <=:
	void setPlugin(Proxy p);

        Expr createFunction(String name, List<Object> args, Environment env);
}
