package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.InputStream;

public interface Loader {
    static final int RDFXML_FORMAT = 0;
    static final int RDFA_FORMAT   = 1;
    static final int TURTLE_FORMAT = 2;
    static final int NT_FORMAT     = 3;
    static final int JSONLD_FORMAT = 4;
    static final int RULE_FORMAT   = 5;
    static final int QUERY_FORMAT  = 6;
    static final int UNDEF_FORMAT  = 7;

	void init(Object o);
	
	boolean isRule(String path);
	
	void load(String path);
	
	void load(String path, String source);
        
        void load(String path, String base, String source, int format) throws LoadException;

        void load(InputStream stream, String str) throws LoadException;
	
	void loadWE(String path) throws LoadException;
	
	void loadWE(String path, String source) throws LoadException;
	
	RuleEngine getRuleEngine();
        
        int getFormat(String path);


}
