package fr.inria.edelweiss.kgram.core;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;

/**
 * Alternative implementation of select distinct *
 * Used by RuleEngine ResultWatcher 
 * @author Olivier Corby, Wimmics, Inria, I3S,  2014
 *
 */
public class Distinct {
	
	TreeMapping table;
        List<Node> list;
        
	class TreeMapping extends TreeMap<Node[], Node[]> {	
		
		TreeMapping(){
			super(new Compare());
		}
	}
	

	class Compare implements Comparator<Node[]> {
					
		Compare(){						
		}

		@Override
		public int compare(Node[] m1, Node[] m2){
						
			for (int i = 0; i<m1.length; i++){
				int res = compare(m1[i], m2[i]);
				if (res != 0) return res;
			}
			return 0;
		}
		
		
		
		int compare(Node n1, Node n2){
			if (n1 == n2){
				return 0;
			}
			else if (n1 == null){
				return -1;
			}
			else if (n2 == null){
				return +1;
			}
			else {
				return n1.compare(n2);
			}
		}
	}

        Distinct(){
            table = new TreeMapping();
        }
	
	Distinct(List<Node> l){
            this();
            list = l;
	}
        
        public static Distinct create(List<Node> l){
            return new Distinct(l);
        }
	
	public static Distinct create(){
            return new Distinct();
        }
            
	public boolean isDistinct(Node[] key){
										
		if (table.containsKey(key)){
			return false;
		}
		table.put(key, key);		
		return true;
	}
        
        public boolean isDistinct(Environment env){
            Node [] key = new Node[list.size()];
            int i = 0;
            for (Node node : list){
                key[i++] = env.getNode(node);
            }
            return isDistinct(key);
        }
        
        public boolean isDistinct(Node n1, Node n2){
            Node [] key = new Node[2];
            key[0] = n1;
            key[1] = n2;
            return isDistinct(key);
        }


	
}

