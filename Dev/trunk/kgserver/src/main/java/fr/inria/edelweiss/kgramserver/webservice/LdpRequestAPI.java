package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;

/**
 * A simple version of Linked Data Platform 1.0 (LDP) Server, according to W3C
 * recommendation (WD 11 March 2014) http://www.w3.org/TR/ldp/ it supports to
 * add triples as resources and return rdf resources by request-uri in
 * text/turtle format
 *
 * HTTP methods @GET @HEAD @OPTIONS @POST have been implemented.
 *
 * default base url: http://localhost:8080/kgram/ldp
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 avr. 2014
 */
@Path("ldp")
public class LdpRequestAPI {

    private final Logger logger = Logger.getLogger("==LDP== " + LdpRequestAPI.class);
    private static final TripleStore store = new TripleStore();
    private static final QueryProcess exec = QueryProcess.create(store.graph);
    private final String headerAccept = "Access-Control-Allow-Origin";
    private final static String SERVER = "http://localhost:8080/kgram/ldp/";

    //^(?!upload|create)(.*)$
    @GET
    @Path("{path:.+}")
    @Produces("text/turtle")
    public Response getResourceGET(@PathParam("path") String res) {
        try {
            return getResourceResonse(res, false);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError().header(headerAccept, "*").entity(ex).build();
        }
    }

    @GET
    @Produces("application/sparql-results+json")
    public Response getResourceList(@QueryParam("request") String cmd) {
        try {
            String query = "";
            if ("list".equalsIgnoreCase(cmd)) {
                query = "SELECT  ?resource (COUNT(?resource ) AS ?count ) { ?resource ?p ?o } GROUP BY ?resource ORDER BY DESC( ?count)";

            } else if ("count".equalsIgnoreCase(cmd)) {
                query = "SELECT (COUNT(DISTINCT ?s ) AS ?ResourceCount) { ?s ?p ?o }";

            } else {//null|empty| other command
                return Response.status(Response.Status.BAD_REQUEST).entity("Please specify correctly request type:list | count | resource name (ex. /Monaco)").build();
            }
            logger.info(query);
            String json = JSONFormat.create(exec.query(query)).toString();
            return Response.status(200).header(headerAccept, "*").entity(json).build();
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError().header(headerAccept, "*").entity(ex).build();
        }
    }

    @HEAD
    @Path("{path:.+}")
    public Response getResourceHEAD(@PathParam("path") String resource) {
        try {
            return getResourceResonse(resource, false);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError().header(headerAccept, "*").entity(ex).build();
        }
    }

    @OPTIONS
    @Path("{path:.+}")
    public Response getResourceOPTIONS(@PathParam("path") String resource) {
        return Response.ok().header(headerAccept, "*").header("Allow", "GET, HEAD, OPTIONS").build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesPOST(@FormParam("update") String query) {
        try {
            logger.info(query);
            if (query != null) {
                exec.query(query);
            } else {
                logger.warn("Null update query !");
            }

            return Response.ok().header(headerAccept, "*").build();
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError().header(headerAccept, "*").entity(ex).build();
        }
    }

    @OPTIONS
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesOPTIONS() {
        return Response.ok().header(headerAccept, "*").header("Allow", "POST, OPTIONS").build();
    }

    private Response getResourceResonse(String res, boolean head) throws EngineException {
        String content = "";
        if (!head) {
            //String tmp = "http://fr.dbpedia.org/resource/";
            String subject = "<" + SERVER + res + ">";
            String sparql = "construct where {" + subject + " ?p ?o}";
            Mappings m = exec.query(sparql);
            logger.info(sparql);

            if (m.size() > 0) {
                content = TripleFormat.create(m).toString();
            }
        }

        ResponseBuilder rb = Response.ok(content);
        rb.tag("" + res.hashCode());//eTag
        rb.header("Content-type", "text/turtle; charset=utf-8");
        //TODO, check the resource type:LDPR, LDP-NR, LDPC, etc..
        rb.header("Link", "<http://www.w3.org/ns/ldp#RDFResource>; rel = \"type\"");
        rb.header("Preference-Applied", "return=presentation");
        rb.header(headerAccept, "*");

        return rb.build();
    }

    // Inner class for ecapsulating a graph as a triple store
    // this part needs to be extended in future
    private static class TripleStore {

        private Graph graph = Graph.create();

        TripleStore() {

        }
    }
}
