package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;

public class MDMGlobalGraph {
    private String bdiGlobalGraphIRI = "";
    private String mdmGlobalGraphIRI = "";

    MDMGlobalGraph(String iri, String id) {
        SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
        this.bdiGlobalGraphIRI = iri;
        mdmGlobalGraphIRI = Namespaces.G.val() + id;

        constructGlobalGraph();
        schemaIntegrationHelper.writeToFile("mdmGlobalGraph", mdmGlobalGraphIRI);
    }

    /**
     * This method is to transform the Integrated Global Graph into MDM Global Graph i.e. Concepts, features etc...
     */
    private void constructGlobalGraph() {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);

        //Create MDM Global Graph i.e. create a named graph in the TDB
        ds.removeNamedModel(mdmGlobalGraphIRI);

        Model mdmGlobalGraph = ds.getNamedModel(mdmGlobalGraphIRI);
        System.out.println(mdmGlobalGraph.size());

        /*TODO Query to get Classes from BDI Global Graph and convert to Concepts of MDM's Global Graph*/
        //classesToConcepts(mdmGlobalGraph);
        /*TODO Query to get Properties from BDI Global Graph and convert to Features of MDM's Global Graph*/
        //propertiesToFeatures(mdmGlobalGraph);
        /*TODO Query to get Object Properties from BDI Global Graph and convert to ????  of MDM's Global Graph AND Create hasRelation edge*/
        objectPropertiesToRelations(mdmGlobalGraph);
        /*TODO Query to get Classes and their properties from BDI Global Graph to create hasFeature edges between Concepts and Features of MDM Global Graph*/

        mdmGlobalGraph.commit();
        mdmGlobalGraph.close();
        ds.commit();
        ds.end();
        ds.close();
    }

    private void classesToConcepts(Model mdmGlobalGraph) {
        String getClasses = "SELECT * WHERE { GRAPH <" + bdiGlobalGraphIRI + "> { ?s ?p ?o. ?s rdf:type rdfs:Class. FILTER NOT EXISTS {?o rdf:type ?x.} }}";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClasses, bdiGlobalGraphIRI).forEachRemaining(triple -> {
            System.out.print(triple.getResource("s") + "\t");
            System.out.print(triple.get("p") + "\t");
            System.out.print(triple.get("o") + "\n");
            mdmGlobalGraph.add(triple.getResource("s"), new PropertyImpl(RDF.TYPE), new ResourceImpl(GlobalGraph.CONCEPT.val()));
        });
    }

    private void propertiesToFeatures(Model mdmGlobalGraph) {
        String getProperties = " SELECT * WHERE { GRAPH <" + bdiGlobalGraphIRI + "> { ?property rdfs:domain ?domain; rdfs:range ?range . FILTER NOT EXISTS {?range rdf:type rdfs:Class.}} }";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getProperties, bdiGlobalGraphIRI).forEachRemaining(triple -> {
            System.out.print(triple.get("property") + "\t");
            System.out.print(triple.get("domain") + "\t");
            System.out.print(triple.get("range") + "\n");
            //mdmGlobalGraph.add(triple.getResource("property"), new PropertyImpl(RDF.TYPE), new ResourceImpl(GlobalGraph.FEATURE.val()));
        });
    }

    private void objectPropertiesToRelations(Model mdmGlobalGraph) {
        String getObjectProperties = " SELECT * WHERE { GRAPH <" + bdiGlobalGraphIRI + "> { ?property rdfs:domain ?domain; rdfs:range ?range . ?range rdf:type rdfs:Class.} }";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getObjectProperties, bdiGlobalGraphIRI).forEachRemaining(triple -> {
            System.out.print(triple.get("property") + "\t");
            System.out.print(triple.get("domain") + "\t");
            System.out.print(triple.get("range") + "\n");
            mdmGlobalGraph.add(triple.getResource("domain"), new PropertyImpl(triple.get("property").toString()), triple.getResource("range"));
        });
        //connectConcepts
    }

    private void connectConceptsAndFeatures() {
        String getClasses = "SELECT * WHERE { GRAPH <" + bdiGlobalGraphIRI + "> { ?s ?p ?o. ?s rdf:type rdfs:Class. }}";

        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClasses, bdiGlobalGraphIRI).forEachRemaining(triple -> {
            System.out.println();
            System.out.println();
            System.out.print(triple.get("s") + "\n");

            String getClassProperties = " SELECT * WHERE { GRAPH <" + bdiGlobalGraphIRI + "> { ?property rdfs:domain <" + triple.get("s") + ">; rdfs:range ?range. FILTER NOT EXISTS {?range rdf:type rdfs:Class.}}} ";
            RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClassProperties, bdiGlobalGraphIRI).forEachRemaining(tripleNested -> {
                System.out.print(tripleNested.get("property") + "\t");
            });
        });
    }
}
