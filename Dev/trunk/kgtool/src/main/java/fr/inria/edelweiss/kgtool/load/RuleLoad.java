package fr.inria.edelweiss.kgtool.load;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;

/**
 * Rule Loader as construct-where SPARQL Queries
 * Can also load Corese rule format
 * 
 * Olivier Corby, Edelweiss INRIA 2011
 * 
 */

public class RuleLoad {
	private static Logger logger = Logger.getLogger(Load.class);	

	static final String NS 		= "http://ns.inria.fr/edelweiss/2011/rule#";
	static final String BRUL 	= "http://ns.inria.fr/corese/2008/rule#";
	static final String COS 	= "http://www.inria.fr/acacia/corese#";
	
	static final String BODY 	= "body";
	static final String RULE 	= "rule";
	static final String VALUE 	= "value";
	static final String PREFIX1 	= "prefix";
	static final String PREFIX2 	= "PREFIX";

	static final String IF 		= "if";
	static final String THEN 	= "then";
	static final String CONST 	= "construct";
	static final String WHERE 	= "where";

	

	
	RuleEngine engine;
	
	RuleLoad(RuleEngine e){
		engine = e;
	}
	
	public static RuleLoad create(RuleEngine e){
		return new RuleLoad(e);
	}
	
	public void load(String file){
		Document doc;
		try {
			doc = parse(file);
			load(doc);
		} catch (LoadException e) {
			logger.error(e);
		}
	}
	
	public void loadWE(String file) throws LoadException{
		Document doc = parse(file);
		load(doc);
	}
	
	public void loadWE(InputStream stream) throws LoadException{
		Document doc = parse(stream);
		load(doc);
	}

	public void load(InputStream stream) {
		try {
			Document doc = parse(stream);
			load(doc);
		} catch (LoadException e) {
			logger.error(e);
		}
	}
	
	public void loadWE(Reader stream) throws LoadException{
		Document doc = parse(stream);
		load(doc);
	}

	public void load(Reader stream) {
		try {
			Document doc = parse(stream);
			load(doc);
		} catch (LoadException e) {
			logger.error(e);
		}
	}
		
	void load(Document doc){	
		
		NodeList list = doc.getElementsByTagNameNS(NS, BODY);
		
		if (list.getLength() == 0){
			list = doc.getElementsByTagNameNS(BRUL, VALUE);
		}
		
		if (list.getLength() == 0){
			list = doc.getElementsByTagNameNS(COS, RULE);
			if (list.getLength() == 0){
				error();
				return;
			}
			loadCorese(list);
			return;
		}
		
		for (int i=0; i<list.getLength(); i++){
			Node node = list.item(i);
			String rule = node.getTextContent();
			try {
				engine.defRule(rule);
			} catch (EngineException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Corese format
	 */
	public void loadCorese(String file){
		Document doc;
		try {
			doc = parse(file);
			NodeList list = doc.getElementsByTagNameNS(COS, RULE);
			loadCorese(list);
		} catch (LoadException e) {
			e.printStackTrace();
		}

	}
		
	void loadCorese(NodeList list){
		
		for (int i=0; i<list.getLength(); i++){
			Element node = (Element) list.item(i);
			NodeList lconst = node.getElementsByTagNameNS(COS, THEN);
			NodeList lwhere = node.getElementsByTagNameNS(COS, IF);

			String rule = getRule(((Element) lconst.item(0)), ((Element) lwhere.item(0)));

			try {
				engine.defRule(rule);
			} catch (EngineException e) {
				e.printStackTrace();
			}
		}
	}
	
	void error(){
		logger.error("Rule Namespace should be one of:");
		logger.error(NS);
		logger.error(BRUL);
	}

	
	String getRule(Element econst, Element ewhere){
		String sconst = econst.getTextContent().trim();
		String swhere = ewhere.getTextContent().trim();
		String pref = "";

		if (swhere.startsWith(PREFIX1) || swhere.startsWith(PREFIX2)){
			int ind = swhere.indexOf("{");
			pref = swhere.substring(0, ind) ;
			swhere = swhere.substring(ind);
		}
		
		String rule = pref  + CONST + sconst + "\n" + WHERE + swhere;

		return rule;

	}
	
	
	private  Document parse(InputStream stream) throws LoadException{
		return parse(new InputSource(stream));
	}
	
	private  Document parse(Reader stream) throws LoadException{
		return parse(new InputSource(stream));
	}
	
	
	private  Document parse(InputSource stream) throws LoadException{
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = fac.newDocumentBuilder();
			Document doc = builder.parse(stream);
			return doc;
		} catch (ParserConfigurationException e) {
			throw LoadException.create(e);
		} catch (SAXException e) {
			throw LoadException.create(e);
		} catch (IOException e) {
			throw LoadException.create(e);
		}
	}
	
	
	private  Document parse(String xmlFileName) throws LoadException{
	     
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = fac.newDocumentBuilder();
			Document doc = builder.parse(xmlFileName);
			return doc;
		} catch (ParserConfigurationException e) {
			throw LoadException.create(e);
		} catch (SAXException e) {
			throw LoadException.create(e);
		} catch (IOException e) {
			throw LoadException.create(e);
		}
	}

}
