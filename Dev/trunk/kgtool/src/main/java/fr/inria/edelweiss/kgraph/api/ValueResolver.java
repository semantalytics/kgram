/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.api;

import fr.inria.acacia.corese.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 */
public interface ValueResolver {

     String getKey(String str);

     String getKey(IDatatype dt);

     void setValue(String key, IDatatype dt);

     IDatatype getValue(String key);   
    
}
