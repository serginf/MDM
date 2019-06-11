package eu.supersede.mdm.storage.resources.bdi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

@Path("metadataStorage")
public class DataSourcesResource {
    @GET
    @Path("bdiIntegratedDataSources/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_integrated_dataSource() {
        System.out.println("[GET /GET_bdiIntegratedDataSources/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> integratedDataSources = Lists.newArrayList();
        MongoCollections.getIntegratedDataSourcesCollection(client).find().iterator().forEachRemaining(document -> integratedDataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(integratedDataSources)).build();
    }


    @GET
    @Path("bdiDataSource/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_dataSources/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> dataSources = Lists.newArrayList();
        MongoCollections.getDataSourcesCollection(client).find().iterator().forEachRemaining(document -> dataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(dataSources)).build();
    }

    @GET
    @Path("bdiIntegratedDataSources/{integratedIRI}")
    @Consumes("text/plain")
    public Response GET_IntegratedDataSourceWithIRI(@PathParam("integratedIRI") String iri) {
        System.out.println("[GET /bdiIntegratedDataSources" + "/" + iri);
        String ids = getIntegratedDataSourceInfo(iri);
        JSONObject idsInfo = new JSONObject();

        if (!ids.isEmpty())
            idsInfo = (JSONObject) JSONValue.parse(ids);
        return Response.ok(new Gson().toJson(idsInfo)).build();
    }

    @GET
    @Path("bdiDataSource/{dataSourceID}")
    @Consumes("text/plain")
    public Response GET_DataSourceWithIRI(@PathParam("dataSourceID") String iri) {
        System.out.println("[GET /bdiDataSource" + "/" + iri);
        String ids = getDataSourceInfo(iri);
        JSONObject idsInfo = new JSONObject();

        if (!ids.isEmpty())
            idsInfo = (JSONObject) JSONValue.parse(ids);
        return Response.ok(new Gson().toJson(idsInfo)).build();
    }

    @GET
    @Path("bdiDeleteDataSource/{dataSourceID}")
    @Consumes("text/plain")
    public Response GET_DeleteDataSource(@PathParam("dataSourceID") String id) {
        System.out.println("[GET /bdiDeleteDataSource" + "/" + id);
        String dataSourceInfo = "";
        String collectionType = "";
        String flag = "ERROR";
        SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
        if (id.contains("INTEGRATED-")) {
            collectionType = "INTEGRATED";
            dataSourceInfo = schemaIntegrationHelper.getIntegratedDataSourceInfo(id);
        } else {
            collectionType = "DATA-SOURCE";
            dataSourceInfo = schemaIntegrationHelper.getDataSourceInfo(id);
        }
        JSONObject dsInfo = new JSONObject();

        if (!dataSourceInfo.isEmpty())
            dsInfo = (JSONObject) JSONValue.parse(dataSourceInfo);

        if (collectionType.equals("DATA-SOURCE")) {
            if (new File(dsInfo.getAsString("parsedFileAddress")).delete() &&
                    new File(dsInfo.getAsString("sourceFileAddress")).delete()) {
                schemaIntegrationHelper.deleteDataSourceInfo(id, collectionType);
                RDFUtil.removeNamedGraph(dsInfo.getAsString("schema_iri"));
                System.out.println("Deleted : " + dsInfo.getAsString("parsedFileAddress") + "\n");
                flag = "DELETED";
            } else {
                System.out.println("Error deleting");
            }
        }

        if (collectionType.equals("INTEGRATED")) {
            if (new File(dsInfo.getAsString("parsedFileAddress")).delete()) {
                schemaIntegrationHelper.deleteDataSourceInfo(id, collectionType);
                RDFUtil.removeNamedGraph(dsInfo.getAsString("schema_iri"));
                System.out.println("Deleted : " + dsInfo.getAsString("parsedFileAddress") + "\n");
                flag = "DELETED";
            } else {
                System.out.println("Error deleting");
            }
        }
        return Response.ok(new Gson().toJson(flag)).build();
    }


    private String getIntegratedDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoCollections.getIntegratedDataSourcesCollection(client).
                find(new Document("dataSourceID", dataSourceId)).iterator();
        return MongoCollections.getMongoObject(client, cursor);
    }
    private String getDataSourceInfo(String dataSourceId) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoCollections.getDataSourcesCollection(client).
                find(new Document("dataSourceID", dataSourceId)).iterator();
        return MongoCollections.getMongoObject(client, cursor);
    }


}
