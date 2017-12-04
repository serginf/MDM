package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;
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
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class GlobalLevelResource {

    private MongoCollection<Document> getArtifactsCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("artifacts");
    }

    /**
     * Get the graphical representation of the artifact
     */
    @GET @Path("global_level/{graph}/features")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_allFeatures(@PathParam("graph") String graph) {
        System.out.println("[GET /global_level/"+graph+"/features");

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

        String d3Representation = OWLtoD3.parse("GLOBAL", triples);
        JSONObject d3 = (JSONObject) JSONValue.parse(d3Representation);

        JSONArray features = new JSONArray();
        ((JSONArray)d3.get("nodes")).forEach(feature -> {
            JSONObject iri = new JSONObject();
            iri.put("name", ((JSONObject)feature).get("name").toString());
            iri.put("iri", ((JSONObject)feature).get("iri").toString());
            features.add(iri);
        });

        dataset.end();
        dataset.close();
        return Response.ok(features.toJSONString()).build();
    }
}