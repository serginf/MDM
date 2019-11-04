package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.db.mongo.models.LAVMappingModel;
import eu.supersede.mdm.storage.db.mongo.repositories.LAVMappingRepository;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.service.impl.DeleteGlobalGraphServiceImpl;
import eu.supersede.mdm.storage.service.impl.DeleteLavMappingServiceImpl;
import eu.supersede.mdm.storage.service.impl.UpdateLavMappingServiceImpl;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class LAVMappingResource {

    private static final Logger LOGGER = Logger.getLogger(LAVMappingResource.class.getName());

    @Inject
    LAVMappingRepository LAVMappingR;

    @GET
    @Path("LAVMapping/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_LAVMapping() {
        LOGGER.info("[GET /LAVMapping/]");

        //TODO: (Javier) test when collection is empty
        String json = UtilsMongo.serializeListJsonAsString(LAVMappingR.findAll());
        return Response.ok(json).build();
    }

    @GET
    @Path("LAVMapping/{LAVMappingID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_LAVMappingByID(@PathParam("LAVMappingID") String LAVMappingID) {
        LOGGER.info("[GET /LAVMapping/] LAVMappingID = " + LAVMappingID);

        LAVMappingModel mappping = LAVMappingR.findByLAVMappingID(LAVMappingID);
        if(mappping != null )
            return Response.ok(UtilsMongo.ToJsonString(mappping)).build();
        return Response.status(404).build();
    }

    @POST
    @Path("LAVMapping/sameAs")
    @Consumes("text/plain")
    public Response POST_LAVMappingMapsTo(String body) {
        LOGGER.info("[POST /LAVMapping/mapsTo/] body = " + body);
        JSONObject objBody = createLAVMappingMapsTo(body);
        return Response.ok(objBody.toJSONString()).build();
    }

    @POST
    @Path("LAVMapping/subgraph")
    @Consumes("text/plain")
    public Response POST_LAVMappingSubgraph(String body) {
        LOGGER.info("[POST /LAVMapping/subgraph/] body = " + body);
        JSONObject objBody = createLAVMappingSubgraph(body);
        return Response.ok(objBody.toJSONString()).build();
    }


    public static JSONObject createLAVMappingMapsTo(String body) {
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();

        Document wrapper = MongoCollections.getWrappersCollection(client)
                .find(new Document("wrapperID", objBody.getAsString("wrapperID"))).first();
        Document dataSource = MongoCollections.getDataSourcesCollection(client)
                .find(new Document("dataSourceID", wrapper.getString("dataSourceID"))).first();

        if (objBody.getAsString("isModified").equals("false")) {
            objBody.put("LAVMappingID", UUID.randomUUID().toString().replace("-", ""));
            MongoCollections.getLAVMappingCollection(client).insertOne(Document.parse(objBody.toJSONString()));

            String dsIRI = dataSource.getString("iri");

            ((JSONArray) objBody.get("sameAs")).forEach(mapping -> {
                JSONObject objMapping = (JSONObject) mapping;
                RDFUtil.addTriple(dsIRI, objMapping.getAsString("attribute"), Namespaces.owl.val() + "sameAs", objMapping.getAsString("feature"));
            });

        } else {
            UpdateLavMappingServiceImpl updateLAVM = new UpdateLavMappingServiceImpl();
            updateLAVM.updateTriples(((JSONArray) objBody.get("sameAs")), objBody.getAsString("LAVMappingID"),
                    wrapper.getString("iri"), dataSource.getString("iri"));
        }

        client.close();
        return objBody;
    }

    public static JSONObject createLAVMappingSubgraph(String body) {
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();

        Document objMapping = MongoCollections.getLAVMappingCollection(client).find
                (new Document("LAVMappingID", objBody.getAsString("LAVMappingID"))).first();

        Document filter = new Document("_id", objMapping.get("_id"));
        Document updateOperationDocument = new Document("$set", new Document("graphicalSubGraph", objBody.get("graphicalSubGraph")));
        MongoCollections.getLAVMappingCollection(client).updateOne(filter, updateOperationDocument);

        Document wrapper = MongoCollections.getWrappersCollection(client)
                .find(new Document("wrapperID", objMapping.getString("wrapperID"))).first();
        Document globalGraph = MongoCollections.getGlobalGraphCollection(client)
                .find(new Document("globalGraphID", objMapping.getString("globalGraphID"))).first();
        String globalGraphIRI = globalGraph.getString("namedGraph");

        String wIRI = wrapper.getString("iri");
        RDFUtil.deleteTriplesNamedGraph(wIRI);
        ((JSONArray) objBody.get("selection")).forEach(selectedElement -> {
            JSONObject objSelectedElement = (JSONObject) selectedElement;
            if (objSelectedElement.containsKey("target")) {
                String sourceIRI = ((JSONObject) objSelectedElement.get("source")).getAsString("iri");
                String relIRI = objSelectedElement.getAsString("name");
                String targetIRI = ((JSONObject) objSelectedElement.get("target")).getAsString("iri");
                RDFUtil.addTriple(wIRI, sourceIRI, relIRI, targetIRI);

                //Extend to also incorporate the type of the added triple. This is obtained from the original global graph
                String typeOfSource = RDFUtil.runAQuery("SELECT ?t WHERE { GRAPH <" + globalGraphIRI + "> { <" + sourceIRI + "> <"
                        + Namespaces.rdf.val() + "type> ?t } }", globalGraphIRI).next().get("t").toString();
                String typeOfTarget = RDFUtil.runAQuery("SELECT ?t WHERE { GRAPH <" + globalGraphIRI + "> { <" + targetIRI + "> <"
                        + Namespaces.rdf.val() + "type> ?t } }", globalGraphIRI).next().get("t").toString();

                RDFUtil.addTriple(wIRI, sourceIRI, Namespaces.rdf.val() + "type", typeOfSource);
                RDFUtil.addTriple(wIRI, targetIRI, Namespaces.rdf.val() + "type", typeOfTarget);

                //Check if the target is an ID feature
                if (RDFUtil.runAQuery("SELECT ?sc WHERE { GRAPH <" + globalGraphIRI + "> { <" + targetIRI + "> <" +
                        Namespaces.rdfs.val() + "subClassOf> <" + Namespaces.sc.val() + "identifier> } }", globalGraphIRI).hasNext()) {
                    RDFUtil.addTriple(wIRI, targetIRI, Namespaces.rdfs.val() + "subClassOf", Namespaces.sc.val() + "identifier");
                }

            }
        });

        client.close();
        return objBody;
    }


    @ApiOperation(value = "Delete a LAVMapping",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK")})
    @DELETE @Path("LAVMapping/{LAVMappingID}")
    @Consumes("text/plain")
    public Response DELETE_LAVMappingByID(@PathParam("LAVMappingID") String LAVMappingID) {
        LOGGER.info("[DELETE /LAVMapping/ "+LAVMappingID);
        DeleteLavMappingServiceImpl service = new DeleteLavMappingServiceImpl();
        service.delete(LAVMappingID);
        return Response.ok().build();
    }

}
