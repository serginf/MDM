package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.apache.jena.query.ResultSet;
import org.bson.Document;

import java.util.logging.Logger;

/**
 * Class with general queries for Jena TDB and Mongo.
 */
public class ServiceUtils {

    private static final Logger LOGGER = Logger.getLogger(ServiceUtils.class.getName());

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

    public static void deleteTriples(String graphIRI,String subjectIRI, String predicateIRI, String objectIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {<"+subjectIRI+"> <"+predicateIRI+"> <"+objectIRI+">} }");
    }

    public static void deleteTriplesByProperty(String graphIRI, String predicateIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {?s <"+predicateIRI+"> ?o} }");
    }

    public static void deleteGraph(String graphIRI){
        RDFUtil.runAnUpdateQuery("DELETE WHERE { GRAPH <" + graphIRI + ">" +
                " {?s ?p ?o} }");
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

    public static boolean deleteGlobalGraph(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> collectionGG = MongoCollections.getGlobalGraphCollection(client);
        DeleteResult result = collectionGG.deleteOne(filter);
        if (result.getDeletedCount() != 1) {
            LOGGER.warning("Error occurred while deleting transaction(deleted= "+ result.getDeletedCount()+").");
            client.close();
            return false;
        }
        client.close();
        return true;
    }


    public static boolean deleteLAVMapping(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> collectionLAV = MongoCollections.getLAVMappingCollection(client);
        DeleteResult result = collectionLAV.deleteOne(filter);
        if (result.getDeletedCount() != 1) {
            LOGGER.warning("Error occurred while deleting transaction(deleted= "+ result.getDeletedCount()+").");
            client.close();
            return false;
        }
        client.close();
        return true;
    }

    public static boolean deleteWrapper(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> collectionLAV = MongoCollections.getWrappersCollection(client);
        DeleteResult result = collectionLAV.deleteOne(filter);
        if (result.getDeletedCount() != 1) {
            LOGGER.warning("Error occurred while deleting transaction(deleted= "+ result.getDeletedCount()+").");
            client.close();
            return false;
        }
        client.close();
        return true;
    }

    public static void updateDataSource(Document query, Document update){
        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getDataSourcesCollection(client).updateOne(query,update);
        client.close();
    }

    public static Document getGlobalGraph(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        Document result =   MongoCollections.getGlobalGraphCollection(client).find(filter).first();
        client.close();
        return result;
    }

    public static Document getDataSource(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        Document result =   MongoCollections.getDataSourcesCollection(client).find(filter).first();
        client.close();
        return result;
    }

    public static Document getWrapper(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        Document result =   MongoCollections.getWrappersCollection(client).find(filter).first();
        client.close();
        return result;
    }

    public static Document getLAVMapping(Document filter){
        MongoClient client = Utils.getMongoDBClient();
        Document result =  MongoCollections.getLAVMappingCollection(client).find(filter).first();
        client.close();
        return result;
    }
}
