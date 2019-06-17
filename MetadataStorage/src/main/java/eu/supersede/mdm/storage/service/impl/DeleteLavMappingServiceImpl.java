package eu.supersede.mdm.storage.service.impl;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.util.ServiceUtils;
import org.bson.Document;

import java.util.ArrayList;

public class DeleteLavMappingServiceImpl {


    public void delete(String LAVMappingID){

        Document LAVMappingObject = ServiceUtils.getLAVMapping(new Document("LAVMappingID",LAVMappingID));
        Document wrapperObject = ServiceUtils.getWrapper(new Document("wrapperID",LAVMappingObject.get("wrapperID")));
        Document dataSourceObject = ServiceUtils.getDataSource(new Document("dataSourceID",wrapperObject.get("dataSourceID")));

        delete(LAVMappingObject,wrapperObject,dataSourceObject);
    }

    public void delete(Document LAVMappingObject, Document wrapperObject, Document dataSourceObject){
        // Remove the sameAs edges
        for (Object el : ((ArrayList)LAVMappingObject.get("sameAs"))) {
            String feature = ((Document) el).getString("feature");
            String attribute = ((Document) el).getString("attribute");
            ServiceUtils.deleteTriples(dataSourceObject.getString("iri"), attribute,Namespaces.owl.val() + "sameAs",feature);
        }

        //Remove the named graph of that mapping
        ServiceUtils.deleteGraph(wrapperObject.getString("iri"));

        //Remove the associated metadata from MongoDB
        ServiceUtils.deleteLAVMapping(LAVMappingObject);
    }
}
