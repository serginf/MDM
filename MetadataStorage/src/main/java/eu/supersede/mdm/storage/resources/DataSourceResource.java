package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.db.mongo.models.DataSourceModel;
import eu.supersede.mdm.storage.db.mongo.repositories.DataSourceRepository;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.wrapper_implementations.SQL_Wrapper;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.service.impl.DeleteDataSourceServiceImpl;
import eu.supersede.mdm.storage.service.impl.DeleteWrapperServiceImpl;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Path("metadataStorage")
public class DataSourceResource {

    private static final Logger LOGGER = Logger.getLogger(DataSourceResource.class.getName());

    @Inject
    DataSourceRepository dataSourceR;

    @GET
    @Path("dataSource/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_dataSource/]");

        //TODO: (Javier) test when collection is empty
        String json = UtilsMongo.serializeListJsonAsString(dataSourceR.findAll());
        return Response.ok(json).build();
    }

    @GET
    @Path("dataSource/{dataSourceID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSourceByID(@PathParam("dataSourceID") String dataSourceID) {
        System.out.println("[GET /dataSource/] dataSourceID = " + dataSourceID);

        DataSourceModel dataSource = dataSourceR.findByDataSourceID(dataSourceID);
        if(dataSource != null )
            return Response.ok(UtilsMongo.ToJsonString(dataSource)).build();
        return Response.status(404).build();
    }

    @POST
    @Path("dataSource/")
    @Consumes("text/plain")
    public Response POST_dataSource(String body) {
        System.out.println("[POST /dataSource/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        String dsName = objBody.getAsString("name").trim().replace(" ","");
        String iri = SourceGraph.DATA_SOURCE.val()+"/"+dsName;
        //Save metadata
        objBody.put("dataSourceID", UUID.randomUUID().toString().replace("-",""));
        objBody.put("iri", iri);
        objBody.put("bootstrappingType", "manual");

        dataSourceR.create(objBody.toJSONString());

        RDFUtil.addTriple(iri, iri, Namespaces.rdf.val()+"type", SourceGraph.DATA_SOURCE.val());

        objBody.put("rdf",RDFUtil.getRDFString(iri));

        return Response.ok(objBody.toJSONString()).build();
    }

    @POST
    @Path("dataSource/test/connection")
    @Consumes("text/plain")
    public Response POST_dataSourceTestConnection(String body) {
        System.out.println("[POST /dataSource/test/connection] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        SQL_Wrapper w = new SQL_Wrapper("preview");
        w.setURL_JDBC(objBody.getAsString("sql_jdbc"));
        Boolean result = w.testConnection();

        if(result)
            return Response.ok().build();
        return Response.serverError().build();
    }

    @ApiOperation(value = "Delete a Data Source and its related Wrappers and LAVMappings, if exist.",consumes = MediaType.TEXT_PLAIN)
    @ApiResponses(value ={
            @ApiResponse(code = 200, message = "OK")})
    @DELETE @Path("dataSource/{dataSourceID}")
    @Consumes("text/plain")
    public Response DELETE_DataSourceByID(@PathParam("dataSourceID") String dataSourceID) {
        LOGGER.info("[DELETE /dataSource/ "+dataSourceID);
        DeleteDataSourceServiceImpl del = new DeleteDataSourceServiceImpl();
        del.delete(dataSourceID);
        return Response.ok().build();
    }

    /**
     * Get the graphical representation of the data source
     */
    /*
    @GET @Path("dataSource/{iri}/graphical")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content_graphical(@PathParam("iri") String iri) {
        System.out.println("[GET /dataSource/"+iri+"/graphical");
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        List<Tuple3<Resource,Property,Resource>> triples = Lists.newArrayList();
        RDFUtil.runAQuery("SELECT * WHERE { GRAPH <"+iri+"> {?s ?p ?o} }",  dataset).forEachRemaining(triple -> {
            triples.add(new Tuple3<>(new ResourceImpl(triple.get("s").toString()),
                    new PropertyImpl(triple.get("p").toString()),new ResourceImpl(triple.get("o").toString())));
        });
        String JSON = OWLtoD3.parse(artifactType, triples);
        dataset.end();
        dataset.close();
        return Response.ok((JSON)).build();
    }
    */
}