package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;

/*
 * 
 * Olivier Corby, Wimmics INRIA 2013
 */
public class TemplateFormat {
		
	String printer;
	Mappings map;
	Graph graph;
	private NSManager nsm;
	PPrinter pp;
	boolean isTurtle = false;
	private boolean isDebug = false;
	private boolean isCheck = false;
	private boolean isHide  = false;
	private String start;

	
	TemplateFormat(Mappings m){
		map = m;
		graph = (Graph) map.getGraph();
		Query q = map.getQuery();
		if (q != null){
			if (q.hasPragma(Pragma.TEMPLATE)){
				printer = (String) q.getPragma(Pragma.TEMPLATE);
			}
			ASTQuery ast = (ASTQuery) q.getAST();
			setNSM(ast.getNSM());
		}		
	}
	

	TemplateFormat(Mappings m, String p){
		this(m);
		printer = p;
	}
	
	TemplateFormat(Graph g){
		graph = g;
	}
	
	TemplateFormat(Graph g, String p){
		this(g);
		printer = p;
	}
	
	public static TemplateFormat create(Mappings m){
		return new TemplateFormat(m);
	}

	public static TemplateFormat create(Mappings m, String pp){
		return new TemplateFormat(m, pp);
	}
	
	public static TemplateFormat create(Graph g){
		return new TemplateFormat(g);
	}

	public static TemplateFormat create(Graph g, String pp){
		return new TemplateFormat(g, pp);
	}
	
	public void setNSM(NSManager n) {
		nsm = n;
	}
	
	public void setPPrinter(String pp){
		printer = pp;
	}
	
	public void setTurtle(boolean b){
		isTurtle = b;
	}
	
	public void setDebug(boolean b){
		isDebug  = b;
	}
	
	void init(){
		
	}
	
	public String toString(){
		if (graph == null){
			return "";
		}
		PPrinter p = createPP();
		return p.toString();
	}
	
	PPrinter createPP(){
		pp = PPrinter.create(graph, printer);
		if (isCheck){
			pp.check();
		}
		pp.setDebug(isDebug);
		pp.setTurtle(isTurtle);
		pp.setHide(isHide);
		
		if (nsm != null){
			pp.setNSM(nsm);
		}
		if (start != null){
			pp.setStart(start);
		}
		return pp;
	}

	
	public StringBuilder toStringBuilder(){
		if (graph == null){
			return new StringBuilder();
		}
		PPrinter p = createPP();
		return p.toStringBuilder();
	}
	
	public PPrinter getPPrinter(){
		return pp;
	}
	
	public void write(String name) throws IOException {				
		FileWriter fw = new FileWriter(name);
		String str = toString();
		fw.write(str);
		fw.flush();
		fw.close();
	}


	public boolean isCheck() {
		return isCheck;
	}


	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}


	public boolean isHide() {
		return isHide;
	}


	public void setHide(boolean isHide) {
		this.isHide = isHide;
	}


	public void setStart(String str) {
		start = str;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
