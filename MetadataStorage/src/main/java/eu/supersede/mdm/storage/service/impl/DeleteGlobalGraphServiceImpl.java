package eu.supersede.mdm.storage.service.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.errorhandling.exception.DeleteNodeGlobalGException;
import eu.supersede.mdm.storage.service.impl.model.LavObj;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.ServiceUtils;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class DeleteGlobalGraphServiceImpl {


    public void deleteGlobalGraph(String namedGraph){

        Document globalGraph = ServiceUtils.getGlobalGraph(new Document("namedGraph",namedGraph));
        Document LAVMapping = ServiceUtils.getLAVMapping(new Document("globalGraphID",globalGraph.get("globalGraphID")));

        ServiceUtils.deleteGlobalGraph(new Document("namedGraph",namedGraph));
        ServiceUtils.deleteLAVMapping(new Document("globalGraphID",globalGraph.get("globalGraphID")));

        //delete namedGraph in jena
        //delete wrapper in jena (subgraph)

        //delete sameAs from datasource in jena


    }

    /*
     * Deletes a node.
     * First check if node is contained in the mappings, wrapper, datasource. If yes, we cannot delete it and  throws DeleteNodeGlobalGException.
     * We delete it from globalgraph. Need to update graphical graph.
     * Returns true if node is deleted.
     */
    public boolean deleteNode(String namedGraph, String nodeIRI) throws DeleteNodeGlobalGException{

        List<String> ls = new ArrayList<>();
        ls.add(nodeIRI);
        List<LavObj> lsLavs = getLavMappingsRelated(namedGraph,ls,true);

        if (!lsLavs.isEmpty()) {
            //check wrapper and datasource
            lsLavs.forEach( obj -> {
                if (GraphContains(obj.getWrapperIRI(), nodeIRI) || GraphContains(obj.getDataSourceIRI(), nodeIRI)){
                    throw new DeleteNodeGlobalGException("Cannot be deleted",this.getClass().getName(),"The element is contained in mappings");
                }
            });
        }

        ServiceUtils.deleteTriplesSubject(namedGraph,nodeIRI);
        ServiceUtils.deleteTriplesObject(namedGraph, nodeIRI);

        return true;
    }


    public boolean deleteProperty(String namedGraph, String sIRI, String pIRI, String oIRI){

        List<String> ls = new ArrayList<>();
        ls.add(sIRI);
        ls.add(oIRI);
        List<LavObj> lsLavs = getLavMappingsRelated(namedGraph,ls,false);

        if (!lsLavs.isEmpty()) {
            lsLavs.forEach( obj -> {
                if (GraphContainsTriples(obj.getWrapperIRI(),sIRI,pIRI,oIRI) || GraphContainsTriples(obj.getDataSourceIRI(),sIRI,pIRI,oIRI)){
                    throw new DeleteNodeGlobalGException("Cannot be deleted",this.getClass().getName(),"The element is contained in mappings");
                }
            });
        }

        ServiceUtils.deleteTriples(namedGraph,sIRI,pIRI,oIRI);
        return true;
    }


    /**
     * Check if a graph contains a given iri.
     * @param graphIRI graph iri
     * @param nodeIRI iri node to look in the graph
     * @return true if the graph contains the iri. Otherwise false.
     */
    public Boolean GraphContains(String graphIRI, String nodeIRI){

        ResultSet rsS = ServiceUtils.getTriplesSubject(graphIRI, nodeIRI);
        ResultSet rsO = ServiceUtils.getTriplesObject(graphIRI, nodeIRI);

        if(((ResultSetMem) rsS).size() == 0 && ((ResultSetMem) rsO).size() == 0)
            return false;
        return true;
    }

    public Boolean GraphContainsTriples(String graphIRI, String sIRI, String pIRI, String oIRI){

        ResultSet rs = ServiceUtils.countTriples(graphIRI, sIRI, pIRI, oIRI);

        if(((ResultSetMem) rs).size() == 0)
            return false;
        return true;
    }

    /**
     * Gets the LavMappings IRI, wrapperIri and datasourceIRI related to a globalgraph
     *
     * @param namedGraph is the named Graph iri
     * @return a list for every lavmapping associated with the given globalgraph and need to be updated.
     */
    public List<LavObj> getLavMappingsRelated(String namedGraph, List<String> IRIs, Boolean verifyIRIs){

        MongoClient client = Utils.getMongoDBClient();
        // Array which contains the wrapper iri for the lavmapping.
        List<LavObj> listLWD = new ArrayList<>();

        String globalGraphID = MongoCollections.getGlobalGraphCollection(client).find
                (new Document("namedGraph",namedGraph)).first().getString("globalGraphID");

        try (MongoCursor<Document> cursor =  MongoCollections.getLAVMappingCollection(client).find
                (new Document("globalGraphID", globalGraphID)).iterator()) {

            while (cursor.hasNext()) {
                Document lavMappingObj = cursor.next();

                String lavMappingID = lavMappingObj.getString("LAVMappingID");
                String wrapperIri = MongoCollections.getWrappersCollection(client).find
                        (new Document("wrapperID", lavMappingObj.getString("wrapperID")))
                        .first().getString("iri");
                String datasourceIri = MongoCollections.getDataSourcesCollection(client).find(
                        new Document("wrappers", lavMappingObj.getString("wrapperID")))
                        .first().getString("iri");

                listLWD.add(new LavObj(lavMappingID,wrapperIri,datasourceIri));

                if(verifyIRIs){
                    //Gets related datasource, wrapper and mapping that also need to be updated
                    for (Object el : ((ArrayList)lavMappingObj.get("sameAs"))) {
                        String feature = ((Document) el).getString("feature");

                        if(IRIs.contains(feature)){
                            throw new DeleteNodeGlobalGException("Cannot be deleted",this.getClass().getName(),"The element is contained in mappings");
                        }
                    }
                }

            }
        } catch (DeleteNodeGlobalGException ex){
            throw ex;
        } catch (Exception e){
            e.printStackTrace();
        }
        client.close();
        return listLWD;
    }

}
