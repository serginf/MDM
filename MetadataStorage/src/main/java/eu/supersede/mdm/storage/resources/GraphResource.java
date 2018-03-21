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
public class GraphResource {

    /**
     * Get the content of the artifact, i.e. the triples
     */
    @GET @Path("graph/{iri}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_graph(@PathParam("iri") String iri) {
        System.out.println("[GET /graph/"+iri);
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);
        String out = "";
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o ?g WHERE { GRAPH <"+iri+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();
            out = ResultSetFormatter.asXMLString(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }
        dataset.end();
        dataset.close();
        JSONObject res = new JSONObject();
        res.put("rdf",out);
        return Response.ok(res.toJSONString()).build();
    }

    /**
     * Get the graphical representation of the graph
     */
    @GET @Path("graph/{artifactType}/{iri}/graphical")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content_graphical(@PathParam("artifactType") String artifactType, @PathParam("iri") String iri) {
        System.out.println("[GET /graph/"+artifactType+"/"+iri+"/graphical");
        List<Tuple3<Resource,Property,Resource>> triples = Lists.newArrayList();
        RDFUtil.runAQuery("SELECT * WHERE { GRAPH <"+iri+"> {?s ?p ?o} }",  iri).forEachRemaining(triple -> {
            triples.add(new Tuple3<>(new ResourceImpl(triple.get("s").toString()),
                    new PropertyImpl(triple.get("p").toString()),new ResourceImpl(triple.get("o").toString())));
        });
        String JSON = OWLtoD3.parse(artifactType, triples);
        return Response.ok((JSON)).build();
    }


    @POST @Path("graph/{iri}")
    @Consumes("text/plain")
    public Response POST_graph(@PathParam("iri") String iri, String RDF) {
        System.out.println("[POST /graph/"+iri);
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(iri);
        OntModel ontModel = ModelFactory.createOntologyModel();
        /* Store RDF into a temporal file */
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
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

    @POST @Path("graph/{iri}/triple/{s}/{p}/{o}")
    @Consumes("text/plain")
    public Response POST_triple(@PathParam("iri") String iri, @PathParam("s") String s, @PathParam("p") String p, @PathParam("o") String o) {
        System.out.println("[POST /graph/"+iri+"/triple");
        RDFUtil.addTriple(iri,s,p,o);
        return Response.ok().build();
    }

    /*
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
    */

}