/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.sparqlendpoint;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public interface SparqlEndpointInterface {

    /**
     *
     * @param query
     * @return
     */
    public String getEdges(String query);
    
    /**
     *
     * @param query
     * @return
     */
    public String query(String query);

    /**
     *
     * @return
     */
    public String getEndpoint();
}
