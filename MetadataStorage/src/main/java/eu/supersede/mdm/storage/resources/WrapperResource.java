package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.model.omq.wrapper_implementations.SparkSQL_Wrapper;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class WrapperResource {

    private MongoCollection<Document> getWrappersCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("wrappers");
    }

    private MongoCollection<Document> getDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("dataSources");
    }

    @GET
    @Path("wrapper/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_wrapper() {
        System.out.println("[GET /wrapper/]");

        MongoClient client = Utils.getMongoDBClient();
        List<String> wrappers = Lists.newArrayList();
        getWrappersCollection(client).find().iterator().forEachRemaining(document -> wrappers.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(wrappers)).build();
    }

    @GET
    @Path("wrapper/{wrapperID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_wrapperByID(@PathParam("wrapperID") String wrapperID) {
        System.out.println("[GET /wrapper/] wrapperID = " + wrapperID);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("wrapperID", wrapperID);
        Document res = getWrappersCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @POST
    @Path("wrapper/")
    @Consumes("text/plain")
    public Response POST_wrapper(String body) {
        System.out.println("[POST /wrapper/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();
        objBody.put("wrapperID", UUID.randomUUID().toString());
        getWrappersCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        getDataSourcesCollection(client).findOneAndUpdate(
                new Document().append("dataSourceID",objBody.getAsString("dataSourceID")),
                new Document().append("$push", new Document().append("wrappers",objBody.getAsString("wrapperID")))
        );
        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @GET
    @Path("wrapper/preview/{dataSourceID}/{query}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_preview(@PathParam("dataSourceID") String dataSourceID,@PathParam("query") String query) throws Exception {
        System.out.println("[GET /GET_preview/] dataSourceID = " + dataSourceID+", query = "+query);
        MongoClient client = Utils.getMongoDBClient();

        Document ds = getDataSourcesCollection(client).find(new Document("dataSourceID", dataSourceID)).first();

        Wrapper w = null;

        if (ds.getString("type").equals("file")) {
            if (ds.getString("file_format").equals("parquet")) {
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("file_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(query);
            }

        }
        /*else if (ds.getString("type").equals("mongodb")) {

        }
        else if (ds.getString("type")) {

        }*/

        client.close();
        return Response.ok((w.preview())).build();
    }


}