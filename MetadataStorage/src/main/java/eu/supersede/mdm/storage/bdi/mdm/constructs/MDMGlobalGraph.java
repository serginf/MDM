package eu.supersede.mdm.storage.bdi.mdm.constructs;

import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import org.semarglproject.vocab.RDF;

import java.util.UUID;

public class MDMGlobalGraph {
    private String bdiGgIri = "";
    private String mdmGgIri = "";

    MDMGlobalGraph(String name, String iri, String id) {
        SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
        this.bdiGgIri = iri;
        mdmGgIri = Namespaces.G.val() + id;

        constructGlobalGraph();
        insertMdmGgInMongo(name);
        schemaIntegrationHelper.writeToFile(name, mdmGgIri);
    }

    private void insertMdmGgInMongo(String name) {
        JSONObject objBody = new JSONObject();
        MongoClient client = Utils.getMongoDBClient();
        objBody.put("globalGraphID", UUID.randomUUID().toString().replace("-", ""));
        objBody.put("namedGraph", mdmGgIri);
        objBody.put("defaultNamespace", Namespaces.G.val());
        objBody.put("name", name);
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
        ds.removeNamedModel(mdmGgIri);

        Model mdmGlobalGraph = ds.getNamedModel(mdmGgIri);
        System.out.println(mdmGlobalGraph.size());

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
