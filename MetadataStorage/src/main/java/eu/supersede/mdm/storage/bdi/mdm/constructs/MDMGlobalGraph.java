package eu.supersede.mdm.storage.bdi.mdm.constructs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import org.semarglproject.vocab.RDF;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.UUID;

public class MDMGlobalGraph {
    private String bdiGgIri = "";
    private String mdmGgIri = "";
    private String bdiGgName = "";
    private String mdmGgGraphicalGraph = "";

    MDMGlobalGraph(String name, String iri, String id) {
        this.bdiGgIri = iri;
        this.bdiGgName = name;
        this.mdmGgIri = Namespaces.G.val() + id;
        run();
    }

    private void run() {
        constructGlobalGraph();
        getGraphicalVowlGraph();
        insertMdmGgInMongo();
    }

    private void getGraphicalVowlGraph() {
        try {
            SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();

            //String ttlFileName = bdiGgName.replace(" ", "") + RandomStringUtils.randomAlphanumeric(4).replace("-", "");
            String ttlFileName = bdiGgName.replace(" ", "");

            // This method will return JSONObject of containing two elements 'vowlJsonFileName' and 'vowlJsonFilePath'
            JSONObject vowlObj = Utils.oWl2vowl(ConfigManager.getProperty("output_path") + schemaIntegrationHelper.writeToFile(ttlFileName, mdmGgIri));
            /*JSONObject vowlObj = Utils.oWl2vowl(ConfigManager.getProperty("output_path") + schemaIntegrationHelper.writeToFile("MDMGOOGLE", "https://www.google.com/ba1028029c184d06bdcd6eaa00f6a316"));*/

            Gson gson = new Gson();
            File jsonFile = Paths.get(vowlObj.getAsString("vowlJsonFilePath")).toFile();
            JsonObject jsonObject = gson.fromJson(new FileReader(jsonFile), JsonObject.class);

            mdmGgGraphicalGraph = "\" " + StringEscapeUtils.escapeJava(jsonObject.toString()) + "\"";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertMdmGgInMongo() {
        JSONObject objBody = new JSONObject();
        MongoClient client = Utils.getMongoDBClient();
        objBody.put("globalGraphID", UUID.randomUUID().toString().replace("-", ""));
        objBody.put("namedGraph", mdmGgIri);
        objBody.put("defaultNamespace", Namespaces.G.val());
        objBody.put("name", bdiGgName);
        objBody.put("graphicalGraph", mdmGgGraphicalGraph);
        MongoCollections.getGlobalGraphCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    /**
     * This method is to transform the Integrated Global Graph into MDM Global Graph i.e. Concepts, features etc...
     */
    private void constructGlobalGraph() {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);

        //Create MDM Global Graph i.e. create a named graph in the TDB
        if (ds.containsNamedModel(mdmGgIri)) {
            System.out.println("TRUE, already existed. Removing...");
            ds.removeNamedModel(mdmGgIri);
        }

        Model mdmGlobalGraph = ds.getNamedModel(mdmGgIri);
        System.out.println("Size: " + mdmGlobalGraph.size());

        /*TODO Query to get Classes from BDI Global Graph and convert to Concepts of MDM's Global Graph*/
        classesToConcepts(mdmGlobalGraph);
        /*TODO Query to get Properties from BDI Global Graph and convert to Features of MDM's Global Graph*/
        propertiesToFeatures(mdmGlobalGraph);
        /*TODO Query to get Object Properties from BDI Global Graph and convert to ????  of MDM's Global Graph AND Create hasRelation edge*/
        objectPropertiesToRelations(mdmGlobalGraph);
        /*TODO Query to get Classes and their properties from BDI Global Graph to create hasFeature edges between Concepts and Features of MDM Global Graph*/
        connectConceptsAndFeatures(mdmGlobalGraph);
        mdmGlobalGraph.commit();
        mdmGlobalGraph.close();
        ds.commit();
        ds.end();
        ds.close();
    }

    private void classesToConcepts(Model mdmGlobalGraph) {
        String getClasses = "SELECT * WHERE { GRAPH <" + bdiGgIri + "> { ?s ?p ?o. ?s rdf:type rdfs:Class. FILTER NOT EXISTS {?o rdf:type ?x.} }}";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClasses, bdiGgIri).forEachRemaining(triple -> {
            System.out.print(triple.getResource("s") + "\t");
            System.out.print(triple.get("p") + "\t");
            System.out.print(triple.get("o") + "\n");
            mdmGlobalGraph.add(triple.getResource("s"), new PropertyImpl(RDF.TYPE), new ResourceImpl(GlobalGraph.CONCEPT.val()));
        });
    }

    private void propertiesToFeatures(Model mdmGlobalGraph) {
        String getProperties = " SELECT * WHERE { GRAPH <" + bdiGgIri + "> { ?property rdfs:domain ?domain; rdfs:range ?range . FILTER NOT EXISTS {?range rdf:type rdfs:Class.}} }";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getProperties, bdiGgIri).forEachRemaining(triple -> {
            System.out.print(triple.get("property") + "\t");
            System.out.print(triple.get("domain") + "\t");
            System.out.print(triple.get("range") + "\n");
            mdmGlobalGraph.add(triple.getResource("property"), new PropertyImpl(RDF.TYPE), new ResourceImpl(GlobalGraph.FEATURE.val()));
        });
    }

    private void objectPropertiesToRelations(Model mdmGlobalGraph) {
        String getObjectProperties = " SELECT * WHERE { GRAPH <" + bdiGgIri + "> { ?property rdfs:domain ?domain; rdfs:range ?range . ?range rdf:type rdfs:Class.} }";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getObjectProperties, bdiGgIri).forEachRemaining(triple -> {
            System.out.print(triple.get("property") + "\t");
            System.out.print(triple.get("domain") + "\t");
            System.out.print(triple.get("range") + "\n");
            mdmGlobalGraph.add(triple.getResource("domain"), new PropertyImpl(triple.get("property").toString()), triple.getResource("range"));
        });
        //connectConcepts
    }

    private void connectConceptsAndFeatures(Model mdmGlobalGraph) {
        String getClasses = "SELECT * WHERE { GRAPH <" + bdiGgIri + "> { ?s ?p ?o. ?s rdf:type rdfs:Class. }}";

        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClasses, bdiGgIri).forEachRemaining(triple -> {
            System.out.println();
            System.out.println();
            System.out.print(triple.get("s") + "\n");
            Resource classResource = triple.getResource("s");
            String getClassProperties = " SELECT * WHERE { GRAPH <" + bdiGgIri + "> { ?property rdfs:domain <" + triple.get("s") + ">; rdfs:range ?range. FILTER NOT EXISTS {?range rdf:type rdfs:Class.}}} ";
            RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClassProperties, bdiGgIri).forEachRemaining(featureTriples -> {
                System.out.print(featureTriples.get("property") + "\t");

                mdmGlobalGraph.add(classResource, new PropertyImpl(GlobalGraph.HAS_FEATURE.val()), featureTriples.getResource("property"));
            });
        });
    }
}
