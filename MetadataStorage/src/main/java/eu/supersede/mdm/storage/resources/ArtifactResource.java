package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;
import org.bson.Document;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.Utils;
import scala.Tuple3;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class ArtifactResource {

    private MongoCollection<Document> getArtifactsCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("artifacts");
    }

    /** System Metadata **/
    @GET @Path("artifacts/{artifactType}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifacts(@PathParam("artifactType") String artifactType) {
        System.out.println("[GET /artifacts/"+artifactType);

        MongoClient client = Utils.getMongoDBClient();

        List<String> allArtifacts = Lists.newArrayList();
        Document query = new Document("type",artifactType);
        getArtifactsCollection(client).find(query).iterator().forEachRemaining(document -> allArtifacts.add(document.toJson()));
        client.close();

        return Response.ok((new Gson().toJson(allArtifacts))).build();
    }

    @POST @Path("artifacts/")
    @Consumes("text/plain")
    public Response POST_artifacts(String JSON_artifact) {
        System.out.println("[POST /artifacts/] JSON_artifact = "+JSON_artifact);

        MongoClient client = Utils.getMongoDBClient();
        getArtifactsCollection(client).insertOne(Document.parse(JSON_artifact));
        client.close();
        return Response.ok().build();
    }

    /**
     * Get the metadata of the artifact, e.g. name, type, ...
     */
    @GET @Path("artifacts/{artifactType}/{graph}/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifact/"+artifactType+"/"+graph);
        try {
            MongoClient client = Utils.getMongoDBClient();
            Document query = new Document("graph", graph);
            query.put("type", artifactType);
            Document res = getArtifactsCollection(client).find(query).first();
            client.close();
            return Response.ok((res.toJson())).build();
        } catch (Exception e ){
            String ret = "";
            for (StackTraceElement s : e.getStackTrace()) {
                ret += s.toString()+"\n";
            }
            return Response.notModified(ret).build();
        }
    }

    /**
     * Get the content of the artifact, i.e. the triples
     */
    @GET @Path("artifacts/{artifactType}/{graph}/content")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifacts/"+artifactType+"/"+graph+"/content");

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        String out = "";
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o ?g WHERE { GRAPH <"+graph+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();
            out = ResultSetFormatter.asXMLString(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }
        dataset.end();
        dataset.close();
        return Response.ok((out)).build();
    }

    /**
     * Get the graphical representation of the artifact
     */
    @GET @Path("artifacts/{artifactType}/{graph}/graphical")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content_graphical(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifacts/"+artifactType+"/"+graph+"/graphical");

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        List<Tuple3<Resource,Property,Resource>> triples = Lists.newArrayList();
        String out = "";
        OntModel theModel = ModelFactory.createOntologyModel();
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT * WHERE { GRAPH <"+graph+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();

            rs.forEachRemaining(triple -> {
                triples.add(new Tuple3<Resource,Property,Resource>(new ResourceImpl(triple.get("s").toString()),
                        new PropertyImpl(triple.get("p").toString()),new ResourceImpl(triple.get("o").toString())));
            });

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        String JSON = OWLtoD3.parse(artifactType, triples);

        dataset.end();
        dataset.close();
        return Response.ok((JSON)).build();
    }


    @POST @Path("artifacts/{graph}")
    @Consumes("text/plain")
    public Response POST_artifacts(@PathParam("graph") String graph, String RDF) {
        System.out.println("[POST /artifacts/"+graph);

        System.out.println("the RDF is "+RDF);

        Dataset dataset = Utils.getTDBDataset();
        System.out.println("Got TDB dataset");
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel(graph);
        System.out.println("Got Model");

        OntModel ontModel = ModelFactory.createOntologyModel();
        System.out.println("Got Ontmodel");

        /* Store RDF into a temporal file */
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        System.out.println("tempFileName = "+tempFileName);

        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            System.out.println("Tempfile = "+tempFile);
            filePath = tempFile.getAbsolutePath();
            System.out.println("artifact temp stored in "+filePath);
            Files.write(RDF.getBytes(),tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        model.add(FileManager.get().readModel(ontModel, filePath));
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        return Response.ok().build();
    }

    @POST @Path("artifacts/{graph}/triple/{s}/{p}/{o}")
    @Consumes("text/plain")
    public Response POST_triple(@PathParam("graph") String graph, @PathParam("s") String s, @PathParam("p") String p, @PathParam("o") String o) {
        System.out.println("[POST /artifacts/"+graph+"/triple");

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel(graph);
            RDFUtil.addTriple(model,s,p,o);

        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        return Response.ok().build();
    }


    @DELETE @Path("artifacts/{artifactType}/{graph}")
    @Consumes("text/plain")
    public Response DELETE_artifacts(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[DELETE /artifacts/"+artifactType+"/"+graph);

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);

        dataset.removeNamedModel(graph);

        dataset.commit();
        dataset.end();
        dataset.close();

        MongoClient client = Utils.getMongoDBClient();
        getArtifactsCollection(client).deleteOne(new Document("graph",graph));
        client.close();

        return Response.ok().build();
    }

    @POST @Path("artifacts/{graph}/graphicalGraph")
    @Consumes("text/plain")
    public Response POST_graphicalGraph(@PathParam("graph") String graph, String body) {
        System.out.println("[POST /artifacts/"+graph+"/graphicalGraph");

        MongoClient client = Utils.getMongoDBClient();
        MongoCollection<Document> artifacts = getArtifactsCollection(client);

        artifacts.findOneAndUpdate(
                new Document().append("graph",graph),
                new Document().append("$set", new Document().append("graphicalGraph",body))
        );

        client.close();

        return Response.ok().build();
    }

}