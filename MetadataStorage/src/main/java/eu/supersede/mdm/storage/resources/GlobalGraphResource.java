package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.errorhandling.exception.ValidationException;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import eu.supersede.mdm.storage.validator.GlobalGraphValidator;
import io.swagger.annotations.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by snadal on 22/11/16.
 */
@Api(value = "metadataStorage")
@Path("metadataStorage")
public class GlobalGraphResource {

    private static final Logger LOGGER = Logger.getLogger(GlobalGraphResource.class.getName());
    private static final String LOG_MSG =
            "{} request finished with inputs: {} and return value: {} in {}ms";
    GlobalGraphValidator validator = new GlobalGraphValidator();

    @ApiOperation(value = "Gets all global graphs registered",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @GET
    @Path("globalGraph/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraph() {
        LOGGER.info("[GET /GET_globalGraph/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> globalGraphs = Lists.newArrayList();
        MongoCollections.getGlobalGraphCollection(client).find().iterator().forEachRemaining(document -> globalGraphs.add(document.toJson()));
        client.close();

        if (new Random().nextBoolean())
            throw new ValidationException("details test", "globalgraph","none");

        return Response.ok(new Gson().toJson(globalGraphs)).build();
    }

    @ApiOperation(value = "Gets the information related for the given globalgraphid",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @GET
    @Path("globalGraph/{globalGraphID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraphByID(
            @ApiParam(value = "global graph identifier", required = true)
            @PathParam("globalGraphID") String globalGraphID) {

        LOGGER.info("[GET /globalGraph/] globalGraphID = "+globalGraphID);
        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("globalGraphID",globalGraphID);
        Document res = MongoCollections.getGlobalGraphCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @ApiOperation(value = "Gets the information related for the given namedGraph",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK",examples = @Example(value = {@ExampleProperty(value = "{ \"namedGraph\" : \"http:/www.essi.upc.edu/us/SportsUML/d4c3dbea56d5493aad50788bd419552d\", \"defaultNamespace\" : \"http:/www.essi.upc.edu/us/SportsUML/\", \"name\" : \"SportsUML\", \"globalGraphID\" : \"60f4c99f29fd40d88c9842199b456e1a\", \"graphicalGraph\" : \"\" }\n")}))})
    @GET
    @Path("globalGraph/namedGraph/{namedGraph}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraphFromNamedGraph(
            @ApiParam(value = "global graph name", required = true)
            @PathParam("namedGraph") String namedGraph) {

        LOGGER.info("[GET /globalGraph/namedGraph/] namedGraph = "+namedGraph);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("namedGraph",namedGraph);
        Document res = MongoCollections.getGlobalGraphCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @ApiOperation(value = "Gets all features related for the given namedGraph",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK",examples = @Example(value = {@ExampleProperty(value = "[\"http:/www.essi.upc.edu/us/SportsUML/feature1\",\"http:/www.essi.upc.edu/us/SportsUML/feature2\"]")}))})
    @GET
    @Path("globalGraph/{namedGraph}/features")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_featuresForGlobalGraph(
            @ApiParam(value = "global graph name", required = true)
            @PathParam("namedGraph") String namedGraph) {

        LOGGER.info("[GET /globalGraph/features/] namedGraph = "+namedGraph);
        JSONArray features = new JSONArray();
        String SPARQL = "SELECT ?f WHERE { GRAPH <"+namedGraph+"> { ?f <"+Namespaces.rdf.val()+"type> <"+GlobalGraph.FEATURE.val()+"> } }";
        RDFUtil.runAQuery(SPARQL,namedGraph).forEachRemaining(t -> {
            features.add(t.get("f").asNode().getURI());
        });
        return Response.ok(features.toJSONString()).build();
    }

    @ApiOperation(value = "Create a new global graph",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK", examples = @Example(value = {@ExampleProperty(value = "{\"namedGraph\":\"http:\\/namespace\\/example\\/8bb55f0d76514e3182adcef3ac7a2a2f\",\"defaultNamespace\":\"http:\\/namespace\\/example\\/\",\"name\":\"example\",\"globalGraphID\":\"467257310adf4aeb98bd2bd4a83be86e\"}")})),
            @ApiResponse(code = 400, message = "body is missing")})
    @POST @Path("globalGraph/")
    @Consumes("text/plain")
    public Response POST_globalGraph(
            @ApiParam(value = "json object2 with global graph information", required = true,example ="{\"name\":\"example\",\"defaultNamespace\":\"http:/namespace/example/\"}")
            String body) {

        LOGGER.info("[POST /globalGraph/] body = "+body);
        validator.validateGeneralBody(body,"POST /globalGraph");
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        objBody.put("globalGraphID", UUID.randomUUID().toString().replace("-",""));

        String namedGraph =
                objBody.getAsString("defaultNamespace").charAt(objBody.getAsString("defaultNamespace").length()-1) == '/' ?
                objBody.getAsString("defaultNamespace") : objBody.getAsString("defaultNamespace") + "/";

        objBody.put("namedGraph", namedGraph+UUID.randomUUID().toString().replace("-",""));

        MongoCollections.getGlobalGraphCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @ApiOperation(value = "Save a triple for a given named graph",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK", examples = @Example(value = {@ExampleProperty(value = "{\"namedGraph\":\"http:\\/namespace\\/example\\/8bb55f0d76514e3182adcef3ac7a2a2f\",\"defaultNamespace\":\"http:\\/namespace\\/example\\/\",\"name\":\"example\",\"globalGraphID\":\"467257310adf4aeb98bd2bd4a83be86e\"}")})),
            @ApiResponse(code = 400, message = "triples are missing")})
    //@POST @Path("globalGraph/{namedGraph}/triple/{s}/{p}/{o}")
    @POST @Path("globalGraph/{namedGraph}/triple/")
    @Consumes("text/plain")
    public Response POST_triple(@PathParam("namedGraph") String namedGraph, String body/*, @PathParam("s") String s, @PathParam("p") String p, @PathParam("o") String o*/) {
        LOGGER.info("[POST /globalGraph/"+namedGraph+"/triple] body = "+body);
        validator.validateBodyTriples(body,"POST /globalGraph/"+namedGraph+"/triple");
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        RDFUtil.addTriple(namedGraph,objBody.getAsString("s"),objBody.getAsString("p"),objBody.getAsString("o"));
        return Response.ok().build();
    }

    @ApiOperation(value = "Save graph in turtle format",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "body is missing")})
    @POST @Path("globalGraph/{namedGraph}/TTL")
    @Consumes("text/plain")
    public Response POST_TTL(@PathParam("namedGraph") String namedGraph, String body) {
        LOGGER.info("[POST /globalGraph/"+namedGraph+"/triple] body = "+body);
        validator.validateGeneralBody(body,"POST /globalGraph/"+namedGraph+"/triple");
        RDFUtil.loadTTL(namedGraph,body);
        return Response.ok().build();
    }

    @ApiOperation(value = "Save graphical graph",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "graph cannot be empty")})
    @POST @Path("globalGraph/{globalGraphID}/graphicalGraph")
    @Consumes("text/plain")
    public Response POST_graphicalGraph(@PathParam("globalGraphID") String globalGraphID, String body) {
        LOGGER.info("[POST /globalGraph/"+globalGraphID+"/graphicalGraph");
        validator.validateGraphicalGraphBody(body,"POST /globalGraph/"+globalGraphID+"/graphicalGraph");
        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> globalGraphCollection = MongoCollections.getGlobalGraphCollection(client);
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
        LOGGER.info("Query: " + body);
        return Response.ok(new Gson().toJson("SparQL Query")).build();
    }
*/
}
