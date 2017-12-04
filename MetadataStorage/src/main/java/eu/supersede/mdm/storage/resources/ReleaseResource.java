package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import scala.Tuple3;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class ReleaseResource {

    private MongoCollection<Document> getReleasesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("releases");
    }

    @GET
    @Path("release/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_release() {
        System.out.println("[GET /release/]");

        MongoClient client = Utils.getMongoDBClient();
        List<String> allReleases = Lists.newArrayList();
        JSONArray arr = new JSONArray();
        //getReleasesCollection(client).find().iterator().forEachRemaining(document -> allReleases.add(document.toJson()));
        getReleasesCollection(client).find().iterator().forEachRemaining(document -> arr.add(document));
        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
    }

    @GET
    @Path("release/{releaseID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_release(@PathParam("releaseID") String releaseID) {
        System.out.println("[GET /release/]");

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("releaseID",releaseID);
        Document res = getReleasesCollection(client).find(query).first();
        client.close();

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        String out = "";
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o ?g WHERE { GRAPH <"+res.getString("graph")+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();
            out = ResultSetFormatter.asXMLString(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }
        dataset.end();
        dataset.close();
        System.out.println(out);

        return Response.ok((res.toJson())).build();
    }


    /**
     * Generation of the Source Level based on a sample dataset
     * For details see:
     *      Nadal, S., Romero, O., Abelló, A., Vassiliadis, P., Vansummeren, S.
     *      An Integration-Oriented Ontology to Govern Evolution in Big Data Ecosystems.
     *      DOLAP 2017
     */
    @POST @Path("release/")
    @Consumes("text/plain")
    public Response POST_release(String body) throws IOException {
        System.out.println("[POST /release/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        JSONObject content = new JSONObject();
        try {
            content = eu.supersede.mdm.storage.model.bdi_ontology.Release.newRelease(objBody.getAsString("event"),objBody.getAsString("schemaVersion"),objBody.getAsString("jsonInstances"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (content.containsKey("kafkaTopic")) {
            if (!objBody.getAsString("kafkaTopic").isEmpty()) {
                objBody.put("kafkaTopic", objBody.getAsString("kafkaTopic"));
            } else {
                objBody.put("kafkaTopic", content.getAsString("kafkaTopic"));
            }

            objBody.put("releaseID", UUID.randomUUID().toString());

            // If we have to dispatch to the Data Lake, generate a random path (.txt for now)
            if (Boolean.parseBoolean(objBody.getAsString("dispatch"))) {
                // TODO replace with path to HDFS
                String dispatcherPath = "/home/snadal/Bolster/DispatcherData/"+UUID.randomUUID().toString()+".txt";
                //Files.touch(new File(dispatcherPath));
                objBody.put("dispatcherPath", dispatcherPath);
            } else {
                objBody.put("dispatcherPath", "");
            }

            getReleasesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        }

        client.close();
        return Response.ok(content.toJSONString()).build();
    }


    @GET
    @Path("release/{graph}/attributes")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_releaseAttributes(@PathParam("graph") String graph) {
        System.out.println("[GET /release/"+graph+"/attributes");

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        List<Tuple3<Resource,Property,Resource>> triples = Lists.newArrayList();
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT * WHERE { GRAPH <"+graph+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();

            rs.forEachRemaining(triple -> {
                String p = triple.get("p").toString();
                String o = triple.get("o").toString();
                System.out.println(p);
                if (p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && o.equals("http://www.BDIOntology.com/source/Attribute")) {
                    triples.add(new Tuple3<Resource, Property, Resource>(new ResourceImpl(triple.get("s").toString()), new PropertyImpl(p), new ResourceImpl(o)));
                }
            });
            System.out.println(triples);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        String d3Representation = OWLtoD3.parse("GLOBAL", triples);
        JSONObject d3 = (JSONObject) JSONValue.parse(d3Representation);

        JSONArray attributes = new JSONArray();
        ((JSONArray)d3.get("nodes")).forEach(attribute -> {
            JSONObject iri = new JSONObject();
            iri.put("name", ((JSONObject)attribute).get("name").toString());
            iri.put("iri", ((JSONObject)attribute).get("iri").toString());
            attributes.add(iri);
        });

        dataset.end();
        dataset.close();
        return Response.ok(attributes.toJSONString()).build();
    }
}
