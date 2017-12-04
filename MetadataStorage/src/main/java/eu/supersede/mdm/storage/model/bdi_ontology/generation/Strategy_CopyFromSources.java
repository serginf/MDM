package eu.supersede.mdm.storage.model.bdi_ontology.generation;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.Release;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.GlobalLevel;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.Mappings;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.SourceLevel;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.TempFiles;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.bson.Document;

import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by snadal on 19/01/17.
 */
public class Strategy_CopyFromSources {

    public static JSONObject copyFromSourcesStrategy(MongoCollection<Document> releasesCollection, JSONArray releases) throws FileNotFoundException {
        OntModel G = ModelFactory.createOntologyModel();
        OntModel M = ModelFactory.createOntologyModel();

        Model O = ModelFactory.createOntologyModel();

        for  (int i = 0; i < releases.size(); ++i) {
            Object r = releases.get(i);
        //releases.forEach(r -> {
            String releaseID = (String)r;

            Document query = new Document("releaseID",releaseID);
            Document res = releasesCollection.find(query).first();

            /**
             * Generate new S and put it to O
             */
            OntModel S = Release.newRelease_toModel(res.getString("event"), res.getString("schemaVersion"), res.getString("jsonInstances"));
            O = O.union(S);

            String sourceLevel = res.getString("graph");

            Dataset dataset = Utils.getTDBDataset();
            dataset.begin(ReadWrite.READ);
            String out = "";
            try(QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o ?g WHERE { GRAPH <"+sourceLevel+"> {?s ?p ?o} }",  dataset)) {
                ResultSet rs = qExec.execSelect();
                while (rs.hasNext()) {
                    QuerySolution q = rs.next();
//                rs.forEachRemaining(q -> {
                    if (q.get("?o").toString().equals(SourceLevel.ATTRIBUTE.val())) {
                        Pattern p = Pattern.compile(SourceLevel.SCHEMA_VERSION.val().replace("/","\\/").replace(".","\\.")+"\\/(.*).*");
                        String uri = q.get("?s").asResource().getURI();
                        Matcher m = p.matcher(uri);
                        String globalElement = "";
                        if (m.find()) {
                            globalElement = "/"+m.group(1).substring(m.group(1).split("/")[0].length()+1);
                        }
                        //String globalElement = "/"+q.get("?s").asResource().getLocalName();
                        // Use the local name as name for G
                        RDFUtil.addTriple(G, GlobalLevel.FEATURE.val()+globalElement, Namespaces.rdf.val()+"type", GlobalLevel.FEATURE.val());
                        RDFUtil.addTriple(O, GlobalLevel.FEATURE.val()+globalElement, Namespaces.rdf.val()+"type", GlobalLevel.FEATURE.val());
                        // Create the mapping G->S
                        RDFUtil.addTriple(M, q.get("?s").asResource().getURI(), Mappings.MAPS_TO.val(), GlobalLevel.FEATURE.val()+globalElement);
                        RDFUtil.addTriple(O, q.get("?s").asResource().getURI(), Mappings.MAPS_TO.val(), GlobalLevel.FEATURE.val()+globalElement);
                    }
                    /*if (!q.get("?o").toString().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
                        System.out.println("Inserting from S "+q.get("?s").toString()+", "+q.get("?p").toString()+", "+q.get("?o").toString());
                        RDFUtil.addTriple(O, q.get("?s").toString(), q.get("?p").toString(), q.get("?o").toString());
                    }*/
                }
                out = ResultSetFormatter.asXMLString(rs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataset.end();
            dataset.close();

        }

        JSONObject out = new JSONObject();

        // Output RDF
        String tempFileForG = TempFiles.getTempFile();
        G.write(new FileOutputStream(tempFileForG),"RDF/XML-ABBREV");
        String contentG = "";
        try {
            contentG = new String(java.nio.file.Files.readAllBytes(new java.io.File(tempFileForG).toPath()));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        String tempFileForM = TempFiles.getTempFile();
        M.write(new FileOutputStream(tempFileForM),"RDF/XML-ABBREV");
        String contentM = "";
        try {
            contentM = new String(java.nio.file.Files.readAllBytes(new java.io.File(tempFileForM).toPath()));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        String tempFileForO = TempFiles.getTempFile();
        O.write(new FileOutputStream(tempFileForO),"RDF/XML-ABBREV");
        String contentO = "";
        try {
            contentO = new String(java.nio.file.Files.readAllBytes(new java.io.File(tempFileForO).toPath()));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        out.put("G", contentG);
        out.put("M", contentM);
        out.put("O", contentO);

        return out;
    }

}
