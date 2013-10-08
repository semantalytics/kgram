package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.api.IDatatype;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;

/**
 * Equivalent of RuleEngine for Query
 * Run a set of query
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryEngine implements Engine {
	private static Logger logger = Logger.getLogger(QueryEngine.class);	
	
	Graph graph;
	QueryProcess exec;
	ArrayList<Query> list;
	HashMap<String, Query> table;
        Index index;       

	boolean isDebug = false,
			isActivate = true,
			isWorkflow = false;
        
        // focus type -> templates
        class Index extends HashMap<String, List<Query>> {
        
            void add(Query q){
                for (Exp exp : q.getBody()){
                    if (exp.isEdge()){
                        Edge edge = exp.getEdge();
                        Node type = edge.getNode(1);
                        if (type.isConstant()){
                            IDatatype dt = (IDatatype) type.getValue();
                            List<Query> list = get(dt.getLabel());
                            if (list == null){
                                list = new ArrayList<Query>();
                                put(dt.getLabel(), list);
                            }
                            list.add(q);
                        }
                    }
                }
            }
            
            public String toString(){
                StringBuilder sb = new StringBuilder();
                for (String dt : keySet()){
                    List<Query> l = get(dt);
                    sb.append(dt);
                    sb.append(System.getProperty("line.separator"));
                    sb.append(l);
                    sb.append(System.getProperty("line.separator"));
               }
                return sb.toString();
            }
            
        }
	
	QueryEngine(Graph g){
		graph = g;
		exec = QueryProcess.create(g);
		list = new ArrayList<Query>();
		table = new HashMap<String, Query>();
                index = new Index();
	}
	
	public static QueryEngine create(Graph g){
		return new QueryEngine(g);
	}

	public void setDebug(boolean b){
		isDebug = b;
	}
	
	public void addQuery(String q)  {
		 try {
			defQuery(q);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Query defQuery(String q) throws EngineException {
		//System.out.println("** QE: \n" + q);
		Query qq = exec.compile(q);
		if (qq != null) {
			ASTQuery ast = (ASTQuery) qq.getAST();
			defQuery(qq);
			return qq;
		}
		return null;
	}
	
	public void defQuery(Query q){
		if (q.isTemplate()){
			defTemplate(q);
		}
		else {
			list.add(q);
		}
	}
	
	/**
	 * Named templates are stored in a table, not in the list
	 */
	public void defTemplate(Query q){
                q.setPrinterTemplate(true);
		if (q.hasPragma(Pragma.NAME)){
			table.put((String) q.getPragma(Pragma.NAME), q);
		}
		else {
			list.add(q);
                       // index.add(q);
		}
	}
        
        public void trace(){
            System.out.println(index.toString());
        }
        
	public List<Query> getQueries(){
		return list;
	}
	
	public List<Query> getTemplates(){
		return list;
	}
        
        public List<Query> getTemplates(IDatatype dt){
            if (dt == null){
		return list;
            }
            else {
                return get(dt);
            }
	}
        
        List<Query> get(IDatatype dt){
            List<Query> l = index.get(dt.getLabel());
            if (l != null){
                return l;
            }
            return list;
        }

	
	public Query getTemplate(String name){
		return table.get(name);
	}
	
	public Collection<Query> getNamedTemplates(){
		return table.values();
	}
	
	
	public boolean  process(){
		if (! isActivate){
			return false;
		}
		
		boolean b = false;
		
		for (Query q : list){
			// TRICKY:
			// This engine is part of a workflow which is processed by graph.init()
			// hence it is synchronized by graph.init() 
			// We are here because a query is processed, hence a (read) lock has been taken
			// tell the query that it is already synchronized to prevent QueryProcess synUpdate
			// to take a write lock that would cause a deadlock
			q.setSynchronized(isWorkflow);
			if (isDebug){
				q.setDebug(isDebug);
				System.out.println(q.getAST());
			}
			Mappings map = exec.query(q);
			b = map.nbUpdate() > 0 || b;
			if (isDebug){
				logger.debug(map + "\n");
			}
		}
		return b;
	}
	
	
	
	public Mappings process(Query q, Mapping m){
		try {
			Mappings map = exec.query(q, m, null);
			return map;
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Mappings.create(q);
	}

	/**
	 * pname is property name
	 * queries are construct where
	 * find a query with construct {?x pname ?y}
	 * process the query
	 * use case: ProducerImpl getEdges() computed by construct-where 
	 */
	Mappings process(Node start, String pname, int index){
		for (Query q : getQueries()){
			
			if (q.isConstruct()){
				Exp cons = q.getConstruct();
				for (Exp ee : cons.getExpList()){

					if (ee.isEdge()){
						Edge edge = ee.getEdge();
						if (edge.getLabel().equals(pname)){

							Mapping bind = null;
							if (start != null) {
								bind = Mapping.create(edge.getNode(index), start);
							}

							Mappings map = process(q, bind);
							return map;
						}
					}
				}
			}
		}
		return null;
	}

	
	public void setActivate(boolean b) {
		isActivate = b;
	}

	
	public boolean isActivate() {
		return isActivate;
	}

	/**
	 * This method is called by a workflow where this engine is submitted
	 */
	public void init() {	
		isWorkflow = true;
	}

	
	public void remove() {			
	}

	
	public void onDelete() {			
	}

	
	public void onInsert(Node gNode, Edge edge) {				
	}

	
	public void onClear() {				
	}

	public int type() {
		return Engine.QUERY_ENGINE;
	}

	
	public void sort(){
		Collections.sort(list, new Comparator<Query>(){
			public int compare(Query q1, Query q2){
				int p1 = getLevel(q1);
				int p2 = getLevel(q2);
				return compare(p1, p2);
			}
			
			int compare(int x, int y) {
		        return (x < y) ? -1 : ((x == y) ? 0 : 1);
		    }
		});
	}
	
	 
	
	int getLevel(Query q){
		ASTQuery ast = (ASTQuery) q.getAST();
		return ast.getPriority();
	}
	
	public void clean(){
		ArrayList<Query> l = new ArrayList<Query>();
		for (Query q : list){
			if (! q.isFail()){
				l.add(q);
			}
		}
		list = l;
	}
	

}
