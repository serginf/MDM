package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.model.omq.wrapper_implementations.*;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import jdk.nashorn.internal.runtime.Source;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
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
        //Metadata for the wrapper
        objBody.put("wrapperID", UUID.randomUUID().toString());
        String wrapperName = objBody.getAsString("name").trim().replace(" ","");
        String wIRI = SourceGraph.WRAPPER.val()+"/"+wrapperName;
        objBody.put("iri",wIRI);

        getWrappersCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        //Update the data source with the new wrapper
        getDataSourcesCollection(client).findOneAndUpdate(
                new Document().append("dataSourceID",objBody.getAsString("dataSourceID")),
                new Document().append("$push", new Document().append("wrappers",objBody.getAsString("wrapperID")))
        );

        //RDF - we use as named graph THE SAME as the data source
        String dsIRI = getDataSourcesCollection(client).
                find(new Document().append("dataSourceID",objBody.getAsString("dataSourceID"))).first()
                .getString("iri");

        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model S = ds.getNamedModel(dsIRI);

        try {

            RDFUtil.addTriple(S, wIRI, Namespaces.rdf.val() + "type", SourceGraph.WRAPPER.val()); S.commit();
            RDFUtil.addTriple(S, dsIRI, SourceGraph.HAS_WRAPPER.val(), wIRI); S.commit();
            ((JSONArray) objBody.get("attributes")).forEach(attribute -> {
                String attName = ((JSONObject) attribute).getAsString("name");
                String attIRI = SourceGraph.ATTRIBUTE.val() + "/" + attName.trim().replace(" ", "");
                RDFUtil.addTriple(S, attIRI, Namespaces.rdf.val() + "type", SourceGraph.ATTRIBUTE.val()); S.commit();
                RDFUtil.addTriple(S, wIRI, SourceGraph.HAS_ATTRIBUTE.val(), attIRI); S.commit();
                //if (Boolean.parseBoolean(((JSONObject)attribute).getAsString("isID"))) {
                //    RDFUtil.addTriple(S,attIRI,Namespaces.rdfs.val()+"subClassOf",Namespaces.sc.val()+"identifier");
                //}
            });
        } catch (Exception exc) {
            System.out.println("erroraco");
            exc.printStackTrace();
        } finally {
            S.close();
        }

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

        switch (ds.getString("type")) {
            case "avro":
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("avro_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(query);
                break;
            case "mongodb":
                w = new MongoDB_Wrapper("preview");

                break;
            case "neo4j":
                w = new Neo4j_Wrapper("preview");

                break;
            case "plaintext":
                w = new PlainText_Wrapper("preview");

                break;
            case "parquet":
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("parquet_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(query);
                break;
            case "restapi":
                w = new REST_API_Wrapper("preview");

                break;
            case "sql":
                w = new SQL_Wrapper("preview");

                break;

        }

        client.close();
        return Response.ok((w.preview())).build();
    }

    /*
    @GET
    @Path("wrapper/{namedGraph}/features")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_featuresForGlobalGraph(@PathParam("namedGraph") String namedGraph) {
        System.out.println("[GET /globalGraph/features/] namedGraph = "+namedGraph);
        JSONArray features = new JSONArray();
        String SPARQL = "SELECT ?f WHERE { ?f <"+Namespaces.rdf.val()+"type> <"+ GlobalGraph.FEATURE.val()+"> }";
        RDFUtil.runAQuery(SPARQL,Utils.getTDBDataset().getNamedModel(namedGraph)).forEachRemaining(t -> {
            features.add(t.get("f").asNode().getURI());
        });
        return Response.ok(features.toJSONString()).build();
    }
    */

}