package eu.supersede.mdm.storage.service.impl;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.util.ServiceUtils;
import org.bson.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DeleteDataSourceServiceImpl {

    public void delete(String dataSourceID){

        Document dataSourceObject = ServiceUtils.getDataSource(new Document("dataSourceID",dataSourceID));

        DeleteWrapperServiceImpl delW = new DeleteWrapperServiceImpl();
        DeleteLavMappingServiceImpl delLAV = new DeleteLavMappingServiceImpl();
        ArrayList wrappers =  ((ArrayList)dataSourceObject.get("wrappers"));

        // For all involved wrappers, apply the same logic as the wrapper removal.
        for (int i = 0; i < wrappers.size(); i++) {
            String wrapperID = (String) wrappers.get(i);

            Document LAVMappingObj =  ServiceUtils.getLAVMapping(new Document("wrapperID",wrapperID));
            String wrapperIRI = ServiceUtils.getWrapper(new Document("wrapperID",wrapperID)).getString("iri");
            if(wrapperIRI != null)
                delLAV.removeNamedGraph(wrapperIRI);
            if(LAVMappingObj != null){

                delLAV.removeLAVMappingFromMongo(LAVMappingObj.getString("LAVMappingID"));
            }
            delW.removeWrapperMongo(wrapperID);
        }
        //Remove its named graph
        ServiceUtils.deleteGraph(dataSourceObject.getString("iri"));

        // Remove its metadata from MongoDB
        ServiceUtils.deleteDataSource(new Document("dataSourceID",dataSourceID));
    }
}
