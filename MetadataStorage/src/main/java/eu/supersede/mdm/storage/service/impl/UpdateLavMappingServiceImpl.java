package eu.supersede.mdm.storage.service.impl;

import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.ServiceUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bson.Document;

public class UpdateLavMappingServiceImpl {

    /**
     * Updates feature iri in datasource, lavmapping and deletes triples from wrapper.
     * @param features array of modified features.
     * @param LAVMappingID id of the LAVMapping to be updated in mongodb.
     * @param wrapperIRI IRI of the wrapper to be deleted in jena.
     * @param datasourceIRI IRI of the datasource to be updated in jena.
     */
    public void updateTriples(JSONArray features,String LAVMappingID, String wrapperIRI, String datasourceIRI){

        for (Object selectedElement : features) {
            JSONObject objSelectedElement = (JSONObject) selectedElement;
            String oldIRI = objSelectedElement.getAsString("featureOld");
            String newIRI = objSelectedElement.getAsString("featureNew");

            updateLavMappingSameAsFeature(LAVMappingID,oldIRI,newIRI);
            ServiceUtils.updateNodeIri(datasourceIRI,oldIRI,newIRI);
        }
        RDFUtil.deleteTriplesNamedGraph(wrapperIRI);
        deleteGraphicalSubgraph(LAVMappingID);
    }

    public void deleteGraphicalSubgraph(String LAVMappingID){
        Document query = new Document();
        query.append("LAVMappingID",LAVMappingID);

        Document setData = new Document();
        setData.append("graphicalSubGraph", "");

        Document update = new Document();
        update.append("$unset", setData);

        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getLAVMappingCollection(client).updateOne(query,update);

        client.close();
    }

    /**
     * Updates the feature IRI from sameAs array of a LavMapping collection in MongoDB
     * @param LAVMappingID lavmapping id to be updated.
     * @param oldIRI actual iri.
     * @param newIRI new iri.
     */
    public void updateLavMappingSameAsFeature(String LAVMappingID, String oldIRI, String newIRI){
        Document query = new Document();
        query.append("LAVMappingID",LAVMappingID)
                .append("sameAs.feature", oldIRI);

        Document setData = new Document();
        setData.append("sameAs.$.feature", newIRI);

        Document update = new Document();
        update.append("$set", setData);

        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getLAVMappingCollection(client).updateOne(query,update);

        client.close();
    }
}
