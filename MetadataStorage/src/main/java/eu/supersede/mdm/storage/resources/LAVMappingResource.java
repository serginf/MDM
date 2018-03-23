package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
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
public class LAVMappingResource {

    @GET
    @Path("LAVMapping/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_LAVMapping() {
        System.out.println("[GET /LAVMapping/]");

        MongoClient client = Utils.getMongoDBClient();
        List<String> LAVMappings = Lists.newArrayList();
        MongoCollections.getLAVMappingCollection(client).find().iterator().forEachRemaining(document -> LAVMappings.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(LAVMappings)).build();
    }

    @GET
    @Path("LAVMapping/{LAVMappingID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_LAVMappingByID(@PathParam("LAVMappingID") String LAVMappingID) {
        System.out.println("[GET /LAVMapping/] LAVMappingID = "+LAVMappingID);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("LAVMappingID",LAVMappingID);
        Document res = MongoCollections.getLAVMappingCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @POST @Path("LAVMapping/sameAs")
    @Consumes("text/plain")
    public Response POST_LAVMappingMapsTo(String body) {
        System.out.println("[POST /LAVMapping/mapsTo/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();
        objBody.put("LAVMappingID", UUID.randomUUID().toString());
        MongoCollections.getLAVMappingCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        Document wrapper = MongoCollections.getWrappersCollection(client)
            .find(new Document("wrapperID",objBody.getAsString("wrapperID"))).first();
        Document dataSource = MongoCollections.getDataSourcesCollection(client)
            .find(new Document("dataSourceID",wrapper.getString("dataSourceID"))).first();
        String dsIRI = dataSource.getString("iri");

        ((JSONArray)objBody.get("sameAs")).forEach(mapping -> {
            JSONObject objMapping = (JSONObject)mapping;
            RDFUtil.addTriple(dsIRI,objMapping.getAsString("attribute"),Namespaces.owl.val()+"sameAs",objMapping.getAsString("feature"));
        });

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @POST @Path("LAVMapping/subgraph")
    @Consumes("text/plain")
    public Response POST_LAVMappingSubgraph(String body) {
        System.out.println("[POST /LAVMapping/subgraph/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();

        Document objMapping = MongoCollections.getLAVMappingCollection(client).find
                (new Document("LAVMappingID",objBody.getAsString("LAVMappingID"))).first();

        Document wrapper = MongoCollections.getWrappersCollection(client)
                .find(new Document("wrapperID",objMapping.getString("wrapperID"))).first();
        Document globalGraph = MongoCollections.getDataSourcesCollection(client)
                .find(new Document("globalGraphID",objMapping.getString("globalGraphID"))).first();
        String wIRI = wrapper.getString("iri");

        ((JSONArray)objBody.get("selection")).forEach(selectedElement -> {
            JSONObject objSelectedElement = (JSONObject)selectedElement;
            if (objSelectedElement.containsKey("target")) {
                String sourceIRI = ((JSONObject)objSelectedElement.get("source")).getAsString("iri");
                String relIRI = objSelectedElement.getAsString("name");
                String targetIRI = ((JSONObject)objSelectedElement.get("target")).getAsString("iri");
                RDFUtil.addTriple(wIRI,sourceIRI,relIRI,targetIRI);
            }
        });

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

}
