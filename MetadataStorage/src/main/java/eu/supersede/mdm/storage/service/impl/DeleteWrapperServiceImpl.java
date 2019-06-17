package eu.supersede.mdm.storage.service.impl;

import eu.supersede.mdm.storage.util.ServiceUtils;
import org.bson.Document;

public class DeleteWrapperServiceImpl {

    public void delete(String wrapperID){

        Document wrapperObject = ServiceUtils.getWrapper(new Document("wrapperID",wrapperID));
        Document dataSourceObject = ServiceUtils.getDataSource(new Document("dataSourceID",wrapperObject.get("dataSourceID")));

        // Remove the triples from the source graph
        ServiceUtils.deleteTriplesSubject(dataSourceObject.getString("iri"),wrapperObject.getString("iri"));
        ServiceUtils.deleteTriplesObject(dataSourceObject.getString("iri"),wrapperObject.getString("iri"));

        //Remove its LAV mapping if exists & Update the metadata for the affected data source in MongoDB
        Document LAVMappingObj =  ServiceUtils.getLAVMapping(new Document("wrapperID",wrapperID));
        if(LAVMappingObj != null){
            DeleteLavMappingServiceImpl delLAV = new DeleteLavMappingServiceImpl();
            delLAV.delete(LAVMappingObj,wrapperObject,dataSourceObject);
        }
        //Remove its metadata from MongoDB
        Document query = new Document("dataSourceID",wrapperObject.get("dataSourceID"));
        Document deleteData = new Document("wrappers",wrapperObject.getString("wrapperID"));
        Document update = new Document("$pull", deleteData);
        ServiceUtils.updateDataSource(query,update);

        ServiceUtils.deleteWrapper(wrapperObject);
    }


}
