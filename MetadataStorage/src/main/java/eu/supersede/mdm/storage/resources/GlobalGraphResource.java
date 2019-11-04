package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.db.mongo.models.GlobalGraphModel;
import eu.supersede.mdm.storage.db.mongo.repositories.GlobalGraphRepository;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.parsers.ImportOWLtoGlobalGraph;
import eu.supersede.mdm.storage.service.impl.DeleteGlobalGraphServiceImpl;
import eu.supersede.mdm.storage.service.impl.UpdateGlobalGraphServiceImpl;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import eu.supersede.mdm.storage.validator.GlobalGraphValidator;
import io.swagger.annotations.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
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

    @Inject
    GlobalGraphRepository globalGraphR;

    @ApiOperation(value = "Gets all global graphs registered",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @GET
    @Path("globalGraph/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_globalGraph() {
        LOGGER.info("[GET /GET_globalGraph/]");

        //TODO: (Javier) test when collection is empty
        String json = UtilsMongo.serializeListJsonAsString(globalGraphR.findAll());
        return Response.ok(json).build();
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

        GlobalGraphModel globalGraph = globalGraphR.findByGlobalGraphID(globalGraphID);
        if(globalGraph != null )
            return Response.ok(UtilsMongo.ToJsonString(globalGraph)).build();
        return Response.status(404).build();
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

        GlobalGraphModel globalGraph = globalGraphR.findByNamedGraph(namedGraph);
        if(globalGraph != null )
            return Response.ok(UtilsMongo.ToJsonString(globalGraph)).build();
        return Response.status(404).build();
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


    @ApiOperation(value = "Gets all features with its concept related for the given namedGraph",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK",examples = @Example(value = {@ExampleProperty(value = "[\"http:/www.essi.upc.edu/us/SportsUML/feature1\",\"http:/www.essi.upc.edu/us/SportsUML/feature2\"]")}))})
    @GET
    @Path("globalGraph/{namedGraph}/featuresAndConcepts")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_FeaturesAndConceptsForGlobalGraph(
            @ApiParam(value = "global graph name", required = true)
            @PathParam("namedGraph") String namedGraph) {

        LOGGER.info("[GET /globalGraph/features/] namedGraph = "+namedGraph);
        JSONObject featureConcept = new JSONObject();
        String SPARQL = "SELECT ?c ?f WHERE { GRAPH <"+namedGraph+"> { ?c <"+GlobalGraph.HAS_FEATURE.val()+"> ?f } }";
        RDFUtil.runAQuery(SPARQL,namedGraph).forEachRemaining(t -> {

            featureConcept.put(t.get("f").asNode().getURI(), t.get("c").asNode().getURI());
//            features.add(featureConcept);
        });
        return Response.ok(featureConcept.toJSONString()).build();
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

        objBody.put("globalGraphID", UUID.randomUUID().toString().replace("-",""));

        String namedGraph =
                objBody.getAsString("defaultNamespace").charAt(objBody.getAsString("defaultNamespace").length()-1) == '/' ?
                objBody.getAsString("defaultNamespace") : objBody.getAsString("defaultNamespace") + "/";

        objBody.put("namedGraph", namedGraph+UUID.randomUUID().toString().replace("-",""));

        globalGraphR.create(objBody.toJSONString());

        return Response.ok(objBody.toJSONString()).build();
    }


    @ApiOperation(value = "Create a new global graph from a OWL file",produces = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "body is missing")})
    @POST @Path("globalGraph/import")
    @Consumes("text/plain")
    public Response POST_importGlobalGraph(
            @ApiParam(value = "json object2 with global graph information", required = true)
                    String body) {

        LOGGER.info("[POST /globalGraph/import] body = "+body);
        validator.validateGeneralBody(body,"POST /globalGraph");
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        ImportOWLtoGlobalGraph parser = new ImportOWLtoGlobalGraph();
        parser.convert(objBody.getAsString("path"), objBody.getAsString("name"));
        return Response.ok(objBody.toJSONString()).build();
    }

    @ApiOperation(value = "Save a triple for a given named graph",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK", examples = @Example(value = {@ExampleProperty(value = "{\"namedGraph\":\"http:\\/namespace\\/example\\/8bb55f0d76514e3182adcef3ac7a2a2f\",\"defaultNamespace\":\"http:\\/namespace\\/example\\/\",\"name\":\"example\",\"globalGraphID\":\"467257310adf4aeb98bd2bd4a83be86e\"}")})),
            @ApiResponse(code = 400, message = "triples are missing")})
    @POST @Path("globalGraph/{namedGraph}/triple/")
    @Consumes("text/plain")
    public Response POST_triple(@PathParam("namedGraph") String namedGraph, String body) {
        LOGGER.info("[POST /globalGraph/"+namedGraph+"/triple] body = "+body);
        validator.validateBodyTriples(body,"POST /globalGraph/"+namedGraph+"/triple");
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        RDFUtil.addTriple(namedGraph,objBody.getAsString("s"),objBody.getAsString("p"),objBody.getAsString("o"));
        return Response.ok().build();
    }

    @ApiOperation(value = "Save and update graph in turtle format",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "body is missing")})
    @POST @Path("globalGraph/{namedGraph}/TTL")
    @Consumes("text/plain")
    public Response POST_TTL(@PathParam("namedGraph") String namedGraph, String body) {
        LOGGER.info("[POST /globalGraph/"+namedGraph+"/ ttl] body = "+body);
        validator.validateGeneralBody(body,"POST /globalGraph/"+namedGraph+"/triple");

        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        JSONObject objMod = (JSONObject) objBody.get("modified");
        if(objMod.getAsString("isModified").equals("true")){
            UpdateGlobalGraphServiceImpl u = new UpdateGlobalGraphServiceImpl();
            u.updateTriples(objMod,namedGraph);
        }else{
            RDFUtil.loadTTL(namedGraph,objBody.getAsString("ttl"));
        }
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

    @ApiOperation(value = "Delete a node from the global graph",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 409, message = "node cannot be deleted")})
    @DELETE @Path("globalGraph/{namedGraph}/node")
    @Consumes("text/plain")
    public Response DELETE_nodeGlobalGraph(@PathParam("namedGraph") String namedGraph, String body) {
        LOGGER.info("[DELETE /globalGraph/ "+namedGraph+" /node");

        DeleteGlobalGraphServiceImpl del = new DeleteGlobalGraphServiceImpl();
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        del.deleteNode(namedGraph,objBody.getAsString("iri"));
        return Response.ok().build();
    }

    @ApiOperation(value = "Delete a property from the global graph",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 409, message = "node cannot be deleted")})
    @DELETE @Path("globalGraph/{namedGraph}/property")
    @Consumes("text/plain")
    public Response DELETE_propertyGlobalGraph(@PathParam("namedGraph") String namedGraph, String body) {
        LOGGER.info("[DELETE /globalGraph/ "+namedGraph+" /property");

        DeleteGlobalGraphServiceImpl del = new DeleteGlobalGraphServiceImpl();
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        del.deleteProperty(namedGraph,objBody.getAsString("sIRI"),objBody.getAsString("pIRI"),objBody.getAsString("oIRI"));
        return Response.ok().build();
    }

    @ApiOperation(value = "Delete a global graph and its related LAVMappings, if exist",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK")})
    @DELETE @Path("globalGraph/{globalGraphID}")
    @Consumes("text/plain")
    public Response DELETE_GlobalGraph(@PathParam("globalGraphID") String globalGraphID) {
        LOGGER.info("[DELETE /globalGraph/ "+globalGraphID);

        DeleteGlobalGraphServiceImpl del = new DeleteGlobalGraphServiceImpl();
        del.deleteGlobalGraph(globalGraphID);
        return Response.ok().build();
    }

//    @ApiOperation(value = "Delete property from the global graph",consumes = MediaType.TEXT_PLAIN)
//    @ApiResponses(value ={
//            @ApiResponse(code = 200, message = "OK"),
//            @ApiResponse(code = 409, message = "property cannot be deleted")})
//    @DELETE @Path("globalGraph/{globalGraphID}/property")
//    @Consumes("text/plain")
//    public Response DELETE_propertyGlobalGraph(@PathParam("globalGraphID") String globalGraphID, String body) {
//        LOGGER.info("[DELETE /globalGraph/"+globalGraphID+"/property");
//        validator.validateGraphicalGraphBody(body,"POST /globalGraph/"+globalGraphID+"/graphicalGraph");
//        MongoClient client = Utils.getMongoDBClient();
//        MongoCollection<Document> globalGraphCollection = MongoCollections.getGlobalGraphCollection(client);
//        globalGraphCollection.findOneAndUpdate(
//                new Document().append("globalGraphID",globalGraphID),
//                new Document().append("$set", new Document().append("graphicalGraph",body))
//        );
//        client.close();
//        return Response.ok().build();
//    }

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
