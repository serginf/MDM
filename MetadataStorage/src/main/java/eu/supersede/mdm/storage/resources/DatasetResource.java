package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.util.ConfigManager;
import net.minidev.json.JSONArray;
import org.bson.Document;
import eu.supersede.mdm.storage.util.Utils;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class DatasetResource {

    private MongoCollection<Document> getDatasetsCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("datasets");
    }

    @GET @Path("datasets/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_datasets(@PathParam("username") String username) {
        System.out.println("[GET /datasets/"+username);

        MongoClient client = Utils.getMongoDBClient();
        List<String> allDatasets = Lists.newArrayList();
        JSONArray arr = new JSONArray();
        getDatasetsCollection(client).find().iterator().forEachRemaining(document -> arr.add(document));
        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
    }

    @GET @Path("datasets/{datasetID}/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataset(@PathParam("datasetID") String datasetID, @PathParam("username") String username) {
        System.out.println("[GET /datasets/"+datasetID+"/"+username);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("datasetID",datasetID);
        Document res = getDatasetsCollection(client).find(query).first();
        client.close();

        return Response.ok((res.toJson())).build();
    }

    @POST @Path("datasets/{username}")
    @Consumes("text/plain")
    public Response POST_datasets(@PathParam("username") String username, String JSON_dataset) {
        System.out.println("[POST /datasets/"+username+"] JSON_dataset = "+JSON_dataset);

        MongoClient client = Utils.getMongoDBClient();
        Document theDoc = Document.parse(JSON_dataset);
        theDoc.put("datasetID", UUID.randomUUID().toString());
        getDatasetsCollection(client).insertOne(theDoc);
        client.close();
        return Response.ok().build();
    }

    @DELETE @Path("datasets/{datasetID}/{username}")
    @Consumes("text/plain")
    public Response DELETE_artifacts(@PathParam("datasetID") String datasetID, @PathParam("username") String username) {
        System.out.println("[DELETE /datasets/"+datasetID+"/"+username);

        MongoClient client = Utils.getMongoDBClient();
        getDatasetsCollection(client).deleteOne(new Document("datasetID",datasetID));
        client.close();

        return Response.ok().build();
    }

}
