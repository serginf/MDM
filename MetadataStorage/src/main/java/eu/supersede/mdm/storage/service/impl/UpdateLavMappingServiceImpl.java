package eu.supersede.mdm.storage.service.impl;

import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.Utils;
import org.bson.Document;

public class UpdateLavMappingServiceImpl {

    /**
     * Updates the feature IRI from sameAs array of a LavMapping collection in MongoDB
     * @param LAVMappingID lavmapping id to be updated.
     * @param oldIRI actual iri.
     * @param newIRI new iri.
     */
    public void updateLavMapping(String LAVMappingID, String oldIRI, String newIRI){
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
