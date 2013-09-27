package fr.inria.acacia.corese.triple.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.api.core.ExpType;
/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class is used to manage prefixes and namespaces.
 * <br>
 * @author Olivier Corby 
 */

public class NSManager 
{
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(NSManager.class);
	
	public static final String FPPN = "ftp://ftp-sop.inria.fr/wimmics/soft/pprint/";
        public static final String XSD = RDFS.XSD;
        public static final String RDF = RDFS.RDF;
        public static final String SPIN = "http://spinrdf.org/sp#";


	static final String FPPP = "fp";
        public static final String PPN = ExpType.KGRAM + "pprinter/";
	static final String PPP = "pp";


	/** prefix seed (ns1, ns2,...) */
	private static final String seed 	= "ns";
	private static final String DOT 	= ".";
	public static final String HASH 	= "#";
	static final String NL 			= System.getProperty("line.separator");

	static final char[] end				={'#', '/', '?', ':'}; // may end an URI ...
	static final String pchar=":";
	int count=0;
	Hashtable<String, String> def; // system namespace with prefered prefix
	Hashtable<String, Integer> index;  // namespace -> number
	Hashtable<String, String> tns;     // namespace -> prefix
	Hashtable<String, String> tprefix; // prefix -> namespace
	String base;	
	URI baseURI;	

	private String uri, exp;
	private Object object;
	private Object dt;
	private boolean isValid = true, ishtDoc = true;
	private Hashtable <String, Object> htDoc;
	private Hashtable <String, Boolean> htValid;
	
	/** 
	 * Corresponds to the namespaces declared by default 
	 * 
	 */
	private String defaultNamespaces = null;
	
	private NSManager(){
		def = 		new Hashtable<String, String>();
		tprefix = 	new Hashtable<String, String>();
		tns = 		new Hashtable<String, String>();
		index = 	new Hashtable<String, Integer>();
		define();
	}
	
	private NSManager(String defaultNamespaces) {
		this();
		this.defaultNamespaces = defaultNamespaces;
	}
	
	
	
	/**
	 * Warning: to be used only when we don't need default namespaces and their prefixes
	 * @return a NamespaceManager with defaultNamespaces = ""
	 */
	public static NSManager create() {
		NSManager nsm = new NSManager(null);
		nsm.init();
		return nsm;
	}
	
	/**
	 * 
	 * 	@deprecated
	 */
	public static NSManager create(String defaultNamespaces){
		NSManager nsm=new NSManager(defaultNamespaces);
		nsm.init();
		return nsm;
	}
	
	public NSManager copy(){
		NSManager nsm = create();
		nsm.setBase(getBase());
		for (String p : getPrefixSet()){
			nsm.definePrefix(p, getNamespace(p));
		}
		return nsm;
	}


	public void init(){
		initDefault();
		defNamespace();
	}
	
	public Enumeration<String> getNamespaces(){
		return tns.keys();
	}
	
	public Enumeration<String> getPrefixes(){
		return getPrefixEnum();
	}
	
	public void clear(){
		tns.clear();
		index.clear();
		tprefix.clear();
		count=0;
	}
	
	/**
	 * application specific namespace/prefix if any
	 */
	public void defNamespace(){
		if (defaultNamespaces != null) {
			String ns, p;
			StringTokenizer st=new StringTokenizer(defaultNamespaces);
			while (st.hasMoreTokens()){
				p=st.nextToken();
				ns=st.nextToken();
				defNamespace(ns, p);
			}
		}
	}
	
	// default system namespaces, not for application namespace
	 void define(){
		def.put(RDFS.XML,  RDFS.XMLPrefix);
		def.put(RDFS.RDF,  RDFS.RDFPrefix);
		def.put(RDFS.RDFS, RDFS.RDFSPrefix);
		def.put(RDFS.XSD,  RDFS.XSDPrefix);
		def.put(RDFS.OWL,  RDFS.OWLPrefix);
                
		def.put(ExpType.KGRAM, ExpType.KPREF);
		def.put(RDFS.COS,  RDFS.COSPrefix);
		def.put(FPPN,  	   FPPP);
		def.put(PPN,  	   PPP);
	}
	
	// add default namespaces
	 void initDefault(){
		for (String ns : def.keySet()){
			defNamespace(ns, def.get(ns));
		}
	}
	
	
	public boolean isSystem(String ns){
		return def.containsKey(ns);
	}
	
	public boolean isNamespace(String ns){
		return tns.containsKey(ns);
	}
	
	/** Define a namespace, returns the prefix
	 */
	public String defNamespace(String ns){
		if (ns == null){
			return null;
		}
		else if (! tns.containsKey(ns)){
			defNamespace(ns, makePrefix(ns));
		}
		return getPrefix(ns);
	}
	
	
	String makePrefix(String ns){
		if (def.get(ns)!=null)
			return def.get(ns);
		else {
			return createPrefix(seed);
		}
	}
	
	String createPrefix(String p){
		if (! p.equals(seed)){
			return p;
		}
		String str= (count==0)?seed:(seed+ count);
		count++;
		return str;
	}
	
	public String definePrefix(String prefix, String ns){
		return defNamespace(ns, prefix);
	}

	
	/**
	 *  function://fr.inria.Extern should ends with a "."
	 */
	String prepare(String ns){
		if (ns.startsWith(KeywordPP.CORESE_PREFIX) && ! ns.endsWith(DOT)){
			ns += DOT;
		}
		return ns;
	}
	
	public String defNamespace(String ns, String prefix){
		if (ns!=null && prefix!=null){
			ns = prepare(ns);
			prefix = createPrefix(prefix);
			if (! tns.containsKey(ns)){
				tns.put(ns, prefix);
			}
			defPrefix(prefix, ns);
			index.put(ns, tns.size());
		}
		return prefix;
	}
	
	
	/** Returns the prefix of the namespace
	 */
	public String getPrefix(String ns) {
		return (String) tns.get(ns);
	}
	
	void defPrefix(String prefix, String ns) {
		tprefix.put(prefix, ns);
	}
	
	public String getNamespace(String prefix) {
		return tprefix.get(prefix);
	}
	
	Enumeration<String> getPrefixEnum(){
		return tprefix.keys();
	}
	
	public Set<String> getPrefixSet(){
		return tprefix.keySet();
	}
	
	public int getIndex(String ns){
		if (getPrefix(ns)==null){
			defNamespace(ns);
		}
		return (index.get(ns)).intValue();
	}
	
	public String toPrefix(String nsname){
		return toPrefix(nsname, false);
	}
	
	/**
	 * in XML, if the prefix is empty (:abc)
	 * do not add the ":"
	 */
	public String toPrefixXML(String nsname){
		return toPrefix(nsname, false, true);
	}
	
	/**
	 * If skip, if no prefix for this namespace, return nsname,
	 * else create a prefix
	 */
	
	public String toPrefix(String nsname, boolean skip){
		return toPrefix(nsname, skip, false);
	}
	
	public String toPrefix(String nsname, boolean skip, boolean xml){
		String ns = namespace(nsname);
		if (ns == null || ns.equals("")){
			return nsname;
		}
		String p = getPrefix(ns);
		if (p == null){
			if (skip) return nsname;
			else p = defNamespace(ns);
		}
		String str = p;
		if (! (xml && p.equals(""))){
			str += pchar;
		}
		str += nsname.substring(ns.length());
		return str;
	}
	
	
	
	/**
	 * pname is a QNAME, expand with namespace
	 * @param pname
	 * @return
	 */
	public String toNamespace(String pname){
		if (pname == null) return null;
		
		for  (String p : tprefix.keySet()){
			if (pname.startsWith(p) && pname.indexOf(pchar)==p.length()){
				return getNamespace(p) + pname.substring(p.length()+1);
			}
		}
		return pname;
	}
	
	/**
	 * With base
	 */
	
	public String toNamespaceB(String str){
		
		String pname = toNamespace(str);
		if (isBase()){
			try {
				URI uri = new URI(pname);
				if (! uri.isAbsolute()){
					pname = resolve(pname);
				}
			} catch (Exception e) {
                            logger.error(e);
			}
		} 
		
		return pname;
	}
	
	boolean isAbsoluteURI(String s){
		try {
			return new URI(s).isAbsolute();
		} catch (URISyntaxException e) {
		}
		return false;
	}

	String resolve(String str){
		URI uri = baseURI.resolve(str);
		String res = uri.toString();
		
		if (res.matches("file:/[^/].*")){
			// replace file:/ by file:///
			res = res.substring(5);
			res = "file://" + res;
		}
		
		return res;
	}
	
	public String toString(){
		return toString(false);
	}
	
	public String toString(boolean all){
		StringBuffer sb = new StringBuffer();
		if (base != null){
			sb.append ("base <");
			sb.append (base);
			sb.append (">");
			sb.append(NL);
		}
		for (String p : getPrefixSet()){
			String ns = getNamespace(p);
			if (all || ! isSystem(ns)){
				sb.append ("prefix ");
				sb.append(p);
				sb.append(": <");
				sb.append(getNamespace(p));
				sb.append(">");
				sb.append(NL);
			}
		}
		return sb.toString();
	}
	
	public void setBase(String s) {
		base = s;
		if (s == null){
			baseURI = null;
		}
		else try {
			baseURI = new URI(s);
		} catch (URISyntaxException e) {
			baseURI = null;
			logger.error(e);
		}
	}
	
	
	public boolean isBase() {
		return (baseURI != null);
	}
	
	public String getBase() {
		return base;
	}
	
	

	/**
	 * Return the namespace of this QName
	 */
	public String getQNamespace(String pname) {
		for (String p : tprefix.keySet()) {
			if (pname.startsWith(p) && pname.indexOf(pchar) == p.length()) {
				return getNamespace(p);
			}
		}
		return null;
	}
	
	public String getPackage(String qname){
		String ns = getQNamespace(qname);
		int ind = ns.indexOf(KeywordPP.CORESE_PREFIX);
		if (ind != 0) return ns;
		ns = ns.substring(KeywordPP.CORESE_PREFIX.length());
		if (ns.endsWith(".")){
			ns = ns.substring(0, ns.length()-1);
		}
		return ns;
		
	}


	public String toNamespaceBN(String str) {
		return toNamespaceB(str);
	}
	
	
	public int size(){
		return tns.size();
	}
	
	
	public String stripns(String name, String namespace, boolean refp){
		// if namespace not null, removes it
		// if refp add a #
		return ((namespace!=null) && (inNamespace(name, namespace)))?
				((refp)?HASH+strip(name):strip(name)):name;
	}
	
	public String strip(String name){
		// remove namespace and #
		return  nstrip(name);
	}
	
	
	public String strip(String name, String ns){
		// remove ns from name
		return  name.substring(ns.length());
	}
	
	
	public static String nstrip(String name){
		// remove namespace
		int index;
		for (int i=0; i<end.length; i++){
			index=name.lastIndexOf(end[i]);// ???
			if (index!=-1)
				return  name.substring(index+1);
		}
		return name;
	}
	
	public boolean sysNamespace(String name){
		for (String ns : def.keySet()){
			if (inNamespace(name, ns))
				return true;
		}
		return false;
	}
	
	public boolean inNamespace(String type, String namespace){
	// retourne si un type appartient au namespace
		if (namespace==null) return true;
		else return type.startsWith(namespace);
	};
	
	
	public  static String namespace(String type){  //retourne le namespace d'un type
		if (type.startsWith(HASH))
			return "";
		int index;
		for (int i=0; i<end.length; i++){
			index=type.lastIndexOf(end[i]);
			if (index!=-1){
				String str= type.substring(0, index+1);
				return str;
			}
		}
		return "";
	}
	
	/*
	 return last occurrence of pat (e.g. '/') in str
	 if pat is last, find preceding occurrence
	 */
	static int getIndex(String str, char pat){
		int index=str.lastIndexOf(pat);
		if (index == str.length()-1){
			logger.debug(str + " " + index + " " + str.lastIndexOf(pat, index));
			return str.lastIndexOf(pat, index-1);
		}
		else return index;
	}
	
	public static String putNamespace(String ns, String label){
		return ns+label;
	}

	public String getDefaultNamespaces() {
		return defaultNamespaces;
	}
	
	// xpath document:
	public void set(String name, Object obj){
		uri = name;
		object = obj;
		isValid = obj != null;
		if (ishtDoc){
			htValid.put(name, (isValid) ? Boolean.TRUE : Boolean.FALSE);
			if (isValid) htDoc.put(name, obj);
		}
	}
	
	public Object get(String name){
		if (uri != null && name.equals(uri)){
			return object;
		}
		else if (ishtDoc){
			uri = name;
			exp = null;
			object = htDoc.get(name);
			return object;
		}
		else return null;
	}
	
	public boolean isValid(String name){
		if (ishtDoc){
			if (htDoc == null){
				htDoc = new Hashtable<String,Object>();
				htValid = new Hashtable<String,Boolean>();
			}
			Boolean valid = htValid.get(name);
			return (valid == null || valid);
		}
		if (uri != null && name.equals(uri)){
			return isValid;
		}
		else return true;
	}
	
	// xpath expression:
	public void put(String name, String ee, Object val){
		uri = name;
		exp = ee;
		dt = val;
	}
	
	public Object pop(String name, String ee){
		if (exp != null && exp.equals(ee) && uri != null && uri.equals(name)){
			return dt;
		}
		else return null;
	}
	
	
	
	
	
	
	
	

}
