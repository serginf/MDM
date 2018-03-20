package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class GlobalGraphResource {

    private MongoCollection<Document> getGlobalGraphCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("globalGraphs");
    }

    @GET
    @Path("globalGraph/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraph() {
        System.out.println("[GET /GET_globalGraph/]");

        MongoClient client = Utils.getMongoDBClient();
        List<String> globalGraphs = Lists.newArrayList();
        getGlobalGraphCollection(client).find().iterator().forEachRemaining(document -> globalGraphs.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(globalGraphs)).build();
    }

    @GET
    @Path("globalGraph/{globalGraphID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraphByID(@PathParam("globalGraphID") String globalGraphID) {
        System.out.println("[GET /globalGraph/] globalGraphID = "+globalGraphID);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("globalGraphID",globalGraphID);
        Document res = getGlobalGraphCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @GET
    @Path("globalGraph/namedGraph/{namedGraph}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraphFromNamedGraph(@PathParam("namedGraph") String namedGraph) {
        System.out.println("[GET /globalGraph/namedGraph/] namedGraph = "+namedGraph);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("namedGraph",namedGraph);
        Document res = getGlobalGraphCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @GET
    @Path("globalGraph/{namedGraph}/features")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_featuresForGlobalGraph(@PathParam("namedGraph") String namedGraph) {
        System.out.println("[GET /globalGraph/features/] namedGraph = "+namedGraph);
        JSONArray features = new JSONArray();
        String SPARQL = "SELECT ?f WHERE { ?f <"+Namespaces.rdf.val()+"type> <"+GlobalGraph.FEATURE.val()+"> }";
        RDFUtil.runAQuery(SPARQL,Utils.getTDBDataset().getNamedModel(namedGraph)).forEachRemaining(t -> {
            features.add(t.get("f").asNode().getURI());
        });
        return Response.ok(features.toJSONString()).build();
    }

    @POST @Path("globalGraph/")
    @Consumes("text/plain")
    public Response POST_globalGraph(String body) {
        System.out.println("[POST /globalGraph/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        objBody.put("globalGraphID", UUID.randomUUID().toString());

        String namedGraph =
                objBody.getAsString("defaultNamespace").charAt(objBody.getAsString("defaultNamespace").length()-1) == '/' ?
                objBody.getAsString("defaultNamespace") : objBody.getAsString("defaultNamespace") + "/";

        objBody.put("namedGraph", namedGraph+UUID.randomUUID().toString());

        getGlobalGraphCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    //@POST @Path("globalGraph/{namedGraph}/triple/{s}/{p}/{o}")
    @POST @Path("globalGraph/{namedGraph}/triple/")
    @Consumes("text/plain")
    public Response POST_triple(@PathParam("namedGraph") String namedGraph, String body/*, @PathParam("s") String s, @PathParam("p") String p, @PathParam("o") String o*/) {
        System.out.println("[POST /globalGraph/"+namedGraph+"/triple] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(namedGraph);
        RDFUtil.addTriple(model,objBody.getAsString("s"),objBody.getAsString("p"),objBody.getAsString("o"));
        model.commit(); model.close();
        dataset.commit(); dataset.end(); dataset.close();
        return Response.ok().build();
    }

    @POST @Path("globalGraph/{globalGraphID}/graphicalGraph")
    @Consumes("text/plain")
    public Response POST_graphicalGraph(@PathParam("globalGraphID") String globalGraphID, String body) {
        System.out.println("[POST /globalGraph/"+globalGraphID+"/graphicalGraph");
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> globalGraphCollection = getGlobalGraphCollection(client);
        globalGraphCollection.findOneAndUpdate(
                new Document().append("globalGraphID",globalGraphID),
                new Document().append("$set", new Document().append("graphicalGraph",body))
        );
        client.close();
        return Response.ok().build();
    }

/*
    @POST
    @Path("globalGraph/sparQLQuery")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response POST_BDI_ontology_SparQLQuery(String body) {
        System.out.println("Query: " + body);
        return Response.ok(new Gson().toJson("SparQL Query")).build();
    }
*/
}
