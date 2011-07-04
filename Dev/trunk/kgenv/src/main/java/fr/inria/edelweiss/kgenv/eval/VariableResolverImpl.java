package fr.inria.edelweiss.kgenv.eval;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import fr.inria.acacia.corese.cg.datatype.function.VariableResolver;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;

/**
 * Variable resolver for XPath 
 * see Processor and XPathFunction
 * @author corby
 *
 */
class VariableResolverImpl implements VariableResolver {
	Document document;
	QName var;
	Text value;
	Environment env;
	
	VariableResolverImpl(Environment e){
		env = e;
	}
			
	public void start(org.w3c.dom.Node doc){
		if (doc instanceof Document) {
			document = (Document)doc;
		}
		var = null;
		value = null;
	}
	
	public Object resolveVariable(QName name) {
		if (var != null && value != null && name.equals(var)){
			return value;
		}
		var = name;
		if (env == null) return null;
		Node node = env.getNode("?" + name.getLocalPart());
		if (node != null){
			value = document.createTextNode(node.getLabel()); //name.getLocalPart());
			return value;
		}
		else {
			return null;
		}
	}
}


