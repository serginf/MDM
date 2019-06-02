package eu.supersede.mdm.storage.util;

import org.apache.jena.query.ResultSet;

public class ServiceUtils {


    /**
     * Delete triple with oldIri and insert new triple with newIri in jena graph
     * @param graphIRI iri of the graph that needs to be updated.
     * @param oldIRI actual iri that appears in the triples.
     * @param newIRI new iri that is going to replace the actual iri.
     */
    public static void updatePropertyIri(String graphIRI, String oldIRI, String newIRI){
        RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+"> DELETE {?s <"+oldIRI+"> ?o} " +
                "INSERT {?s <"+newIRI+"> ?o } WHERE {  ?s <"+oldIRI+"> ?o }");
    }

    /**
     * Delete triple with oldIri and insert new triple with newIri in jena graph
     * @param graphIRI iri of the graph that needs to be updated.
     * @param oldIRI actual iri that appears in the triples.
     * @param newIRI new iri that is going to replace the actual iri.
     */
    public static void updateNodeIri(String graphIRI, String oldIRI, String newIRI){
        // Look and update triples where oldIRI is object.
        RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+">  DELETE {?s ?p <"+oldIRI+">} " +
                "INSERT {?s ?p <"+newIRI+">} WHERE {  ?s ?p <"+oldIRI+"> }");
        // Look and update triples where oldIRI is subject.
        RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+">  DELETE {<"+oldIRI+"> ?p ?o} " +
                "INSERT {<"+newIRI+"> ?p ?o} WHERE {  <"+oldIRI+"> ?p ?o }");
    }

    public static void deleteTriplesSubject(String graphIRI, String subjectIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {<"+subjectIRI+"> ?p ?o} }");
    }

    public static void deleteTriplesObject(String graphIRI, String objectIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {?s ?p <"+objectIRI+"> } }");
    }

    public static void deleteTriplesProperty(String graphIRI,String subjectIRI, String predicateIRI, String objectIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {<"+subjectIRI+"> <"+predicateIRI+"> <"+objectIRI+">} }");
    }

    public static ResultSet getTriplesSubject(String graphIRI, String subjectIRI){
        return RDFUtil.runAQuery("SELECT DISTINCT ?p ?o WHERE { GRAPH <" + graphIRI + "> " +
                "{<"+subjectIRI+"> ?p ?o} }",graphIRI);
    }

    public static ResultSet getTriplesObject(String graphIRI, String objectIRI){
        return RDFUtil.runAQuery("SELECT DISTINCT ?p ?o WHERE { GRAPH <" + graphIRI + ">" +
                " {?s ?p <"+objectIRI+"> } }",graphIRI);
    }

    public static ResultSet countTriples(String graphIRI, String subjectIRI, String predicateIRI, String objectIRI){
        return RDFUtil.runAQuery("SELECT (COUNT(*) AS ?count) WHERE { GRAPH <" + graphIRI + "> " +
                "{<"+subjectIRI+"> <"+predicateIRI+"> <"+objectIRI+">} }",graphIRI);
    }
}
