package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.SourceLevel;
import eu.supersede.mdm.storage.model.metamodel.SourceOntology;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
public class DataSourceResource {

    private MongoCollection<Document> getDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("dataSources");
    }

    @GET
    @Path("dataSource/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSource() {
        System.out.println("[GET /GET_dataSource/]");
        MongoClient client = Utils.getMongoDBClient();
        List<String> dataSources = Lists.newArrayList();
        getDataSourcesCollection(client).find().iterator().forEachRemaining(document -> dataSources.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(dataSources)).build();
    }

    @GET
    @Path("dataSource/{dataSourceID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dataSourceByID(@PathParam("dataSourceID") String dataSourceID) {
        System.out.println("[GET /dataSource/] dataSourceID = " + dataSourceID);
        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("dataSourceID", dataSourceID);
        Document res = getDataSourcesCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @POST
    @Path("dataSource/")
    @Consumes("text/plain")
    public Response POST_dataSource(String body) {
        System.out.println("[POST /dataSource/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();
        String dsName = objBody.getAsString("name").trim().replace(" ","");
        String iri = SourceOntology.DATA_SOURCE.val()+"/"+dsName;
        //Save metadata
        objBody.put("dataSourceID", UUID.randomUUID().toString());
        objBody.put("iri", iri);
        getDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        //Save RDF
        OntModel S = ModelFactory.createOntologyModel();
        RDFUtil.addTriple(S,iri, Namespaces.rdf.val()+"type", SourceOntology.DATA_SOURCE.val());
        objBody.put("rdf",RDFUtil.getRDFString(S));
        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

}