package eu.supersede.mdm.storage.resources;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.util.ConfigManager;
import org.bson.Document;
import eu.supersede.mdm.storage.util.Utils;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/**
 * @author Sergi Nadal - 17/05/2016
 *
 * Users resource (exposed at "metadataDataLayer/users" path)
 */
@Path("metadataStorage")
public class UserResource {

    private MongoCollection<Document> getUsersCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("users");
    }


    public boolean userExists(String username) {
        MongoClient client = Utils.getMongoDBClient();
        if (getUsersCollection(client).find(new Document("username",username)).limit(1).iterator().hasNext()) {
            client.close();
            return true;
        } else {
            client.close();
            return false;
        }
    }

    @GET @Path("users")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_users() {
        MongoClient client = Utils.getMongoDBClient();
        List<String> allUsers = Lists.newArrayList();
        getUsersCollection(client).find().iterator().forEachRemaining(document -> allUsers.add(document.toString()));
        client.close();
        return Response.ok(JSON.serialize(allUsers)).build();
    }

    @GET @Path("users/exists/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response GET_exists(@PathParam("username") String username) {
        System.out.println("[GET /users/exists] username = "+username);
        return userExists(username) ? Response.ok().build() : Response.status(404).build();
    }

    @GET @Path("users/findOne/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response GET_findOne(@PathParam("username") String username) {
        System.out.println("[GET /users/findOne] username = "+username);

        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = getUsersCollection(client).find(new Document("username",username)).iterator();
        boolean itIs = true;
        String out = "";
        if (!cursor.hasNext()) itIs = false;
        else out = cursor.next().toJson();
        client.close();

        if (itIs) return Response.ok(out).build();
        else return Response.status(404).build();
    }

    @POST @Path("users")
    @Consumes("text/plain")
    public Response POST_users(String JSON_user) {
        System.out.println("[POST /users] JSON_user = "+JSON_user);
        MongoClient client = Utils.getMongoDBClient();
        getUsersCollection(client).insertOne(Document.parse(JSON_user));
        client.close();
        return Response.ok().build();
    }
}
