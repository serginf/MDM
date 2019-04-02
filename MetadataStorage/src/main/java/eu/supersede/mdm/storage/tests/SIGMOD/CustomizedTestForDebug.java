package eu.supersede.mdm.storage.tests.SIGMOD;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.graph.RelationshipEdge;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.tests.TestUtils;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Tuple3;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CustomizedTestForDebug {

    private static int a = 1;
    private static void registerWrapper(IntegrationGraph W, String namedGraph, String wrapperName) {
        List<Tuple3<String,String,String>> triples = Lists.newArrayList();
        triples.add(new Tuple3<>(RDFUtil.convertToURI(wrapperName), Namespaces.rdf.val()+"type", SourceGraph.WRAPPER.val()));
        for (String v : W.vertexSet()) {
            if (v.contains("Feature")) {
                String attributeName = "A_"+(a++);
                triples.add(new Tuple3<>(RDFUtil.convertToURI(attributeName), Namespaces.rdf.val()+"type", SourceGraph.ATTRIBUTE.val()));
                triples.add(new Tuple3<>(RDFUtil.convertToURI(wrapperName), SourceGraph.HAS_ATTRIBUTE.val(), RDFUtil.convertToURI(attributeName)));
                triples.add(new Tuple3<>(RDFUtil.convertToURI(attributeName), Namespaces.owl.val() + "sameAs", RDFUtil.convertToURI(v)));
                System.out.println("    "+wrapperName+" - "+attributeName + " -- sameAs --> "+v);
            }
        }
        RDFUtil.addBatchOfTriples(namedGraph,triples);
        //LAV mapping
        W.registerRDFDataset(RDFUtil.convertToURI(wrapperName));
    }

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/SIGMOD_CQ/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/metamodel.txt", prefixes);

        Random random = new Random(System.currentTimeMillis());
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";

        IntegrationGraph Q = new IntegrationGraph();
        Q.addVertex("Concept_1");Q.addVertex("Concept_2");Q.addVertex("Concept_3");Q.addVertex("Concept_4");
        Q.addEdge("Concept_1","Concept_2",new RelationshipEdge("E1"));
        Q.addEdge("Concept_1","Concept_3",new RelationshipEdge("E2"));
        Q.addEdge("Concept_3","Concept_4",new RelationshipEdge("E3"));
        IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,2,1);
        System.out.println("Your query is");Q_withFeatures.printAsWebGraphViz();System.out.println("");

        Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");


        //Wrapper_1
        IntegrationGraph W1 = new IntegrationGraph();
        W1.addVertex("Concept_1");W1.addVertex("Concept_2");W1.addVertex("ID");
        W1.addEdge("Concept_1","Concept_2",new RelationshipEdge("E1"));
        W1.addVertex("Concept_1_Feature_id");W1.addVertex("Concept_1_Feature_1");
        W1.addEdge("Concept_1","Concept_1_Feature_id",new RelationshipEdge("hasFeature"));
        W1.addEdge("Concept_1_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W1.addEdge("Concept_1","Concept_1_Feature_1",new RelationshipEdge("hasFeature"));
        W1.addVertex("Concept_2_Feature_id");W1.addVertex("Concept_2_Feature_1");W1.addVertex("Concept_2_Feature_2");
        W1.addEdge("Concept_2","Concept_2_Feature_id",new RelationshipEdge("hasFeature"));
        W1.addEdge("Concept_2_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W1.addEdge("Concept_2","Concept_2_Feature_1",new RelationshipEdge("hasFeature"));
        W1.addEdge("Concept_2","Concept_2_Feature_2",new RelationshipEdge("hasFeature"));
        System.out.println("Wrapper_1 is");W1.printAsWebGraphViz();System.out.println("");
        registerWrapper(W1,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology","Wrapper_1");

        //Wrapper_2
        IntegrationGraph W2 = new IntegrationGraph();
        W2.addVertex("Concept_1");W2.addVertex("Concept_2");W2.addVertex("Concept_3");W2.addVertex("ID");
        W2.addEdge("Concept_1","Concept_2",new RelationshipEdge("E1"));
        W2.addEdge("Concept_1","Concept_3",new RelationshipEdge("E2"));
        W2.addVertex("Concept_1_Feature_id");W2.addVertex("Concept_1_Feature_1");W2.addVertex("Concept_1_Feature_2");
        W2.addEdge("Concept_1_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W2.addEdge("Concept_1","Concept_1_Feature_id",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_1","Concept_1_Feature_1",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_1","Concept_1_Feature_2",new RelationshipEdge("hasFeature"));
        W2.addVertex("Concept_2_Feature_id");W2.addVertex("Concept_2_Feature_1");W2.addVertex("Concept_2_Feature_2");
        W2.addEdge("Concept_2_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W2.addEdge("Concept_2","Concept_2_Feature_id",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_2","Concept_2_Feature_1",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_2","Concept_2_Feature_2",new RelationshipEdge("hasFeature"));
        W2.addVertex("Concept_3_Feature_id");W2.addVertex("Concept_3_Feature_1");W2.addVertex("Concept_3_Feature_2");
        W2.addEdge("Concept_3","Concept_3_Feature_id",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_3_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W2.addEdge("Concept_3","Concept_3_Feature_1",new RelationshipEdge("hasFeature"));
        W2.addEdge("Concept_3","Concept_3_Feature_2",new RelationshipEdge("hasFeature"));
        System.out.println("Wrapper_2 is");W2.printAsWebGraphViz();System.out.println("");
        registerWrapper(W2,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology","Wrapper_2");

        //Wrapper_3 (combines with Wrapper_1)
        IntegrationGraph W3 = new IntegrationGraph();
        W3.addVertex("Concept_1");W3.addVertex("Concept_3");W3.addVertex("ID");
        W3.addEdge("Concept_1","Concept_3",new RelationshipEdge("E2"));
        W3.addVertex("Concept_1_Feature_id");W3.addVertex("Concept_1_Feature_2");
        W3.addEdge("Concept_1","Concept_1_Feature_id",new RelationshipEdge("hasFeature"));
        W3.addEdge("Concept_1_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W3.addEdge("Concept_1","Concept_1_Feature_2",new RelationshipEdge("hasFeature"));
        W3.addVertex("Concept_3_Feature_id");W3.addVertex("Concept_3_Feature_1");W3.addVertex("Concept_3_Feature_2");
        W3.addEdge("Concept_3","Concept_3_Feature_id",new RelationshipEdge("hasFeature"));
        W3.addEdge("Concept_3_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W3.addEdge("Concept_3","Concept_3_Feature_1",new RelationshipEdge("hasFeature"));
        W3.addEdge("Concept_3","Concept_3_Feature_2",new RelationshipEdge("hasFeature"));
        System.out.println("Wrapper_3 is");W3.printAsWebGraphViz();System.out.println("");
        registerWrapper(W3,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology","Wrapper_3");

        IntegrationGraph W4 = new IntegrationGraph();
        W4.addVertex("Concept_1");W4.addVertex("Concept_3");W4.addVertex("ID");
        W4.addVertex("Concept_1_Feature_id");W4.addVertex("Concept_1_Feature_2");
        W4.addEdge("Concept_1_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W4.addEdge("Concept_1","Concept_1_Feature_id",new RelationshipEdge("hasFeature"));
        W4.addEdge("Concept_1","Concept_1_Feature_2",new RelationshipEdge("hasFeature"));
        W4.addVertex("Concept_3_Feature_id");W4.addVertex("Concept_3_Feature_1");W4.addVertex("Concept_3_Feature_2");
        W4.addEdge("Concept_3","Concept_3_Feature_id",new RelationshipEdge("hasFeature"));
        W4.addEdge("Concept_3_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W4.addEdge("Concept_3","Concept_3_Feature_1",new RelationshipEdge("hasFeature"));
        W4.addEdge("Concept_3","Concept_3_Feature_2",new RelationshipEdge("hasFeature"));
        System.out.println("Wrapper_4 is");W4.printAsWebGraphViz();System.out.println("");
        registerWrapper(W4,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology","Wrapper_4");


        //Wrapper_1
        IntegrationGraph W5 = new IntegrationGraph();
        W5.addVertex("Concept_3");W5.addVertex("Concept_4");W5.addVertex("ID");
        W5.addEdge("Concept_3","Concept_4",new RelationshipEdge("E3"));
        W5.addVertex("Concept_3_Feature_id");
        W5.addEdge("Concept_3","Concept_3_Feature_id",new RelationshipEdge("hasFeature"));
        W5.addEdge("Concept_3_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W5.addVertex("Concept_4_Feature_id");W5.addVertex("Concept_4_Feature_1");W5.addVertex("Concept_4_Feature_2");
        W5.addEdge("Concept_4","Concept_4_Feature_id",new RelationshipEdge("hasFeature"));
        W5.addEdge("Concept_4_Feature_id","ID",new RelationshipEdge("subClassOf"));
        W5.addEdge("Concept_4","Concept_4_Feature_1",new RelationshipEdge("hasFeature"));
        W5.addEdge("Concept_4","Concept_4_Feature_2",new RelationshipEdge("hasFeature"));
        System.out.println("Wrapper_5 is");W5.printAsWebGraphViz();System.out.println("");
        registerWrapper(W5,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology","Wrapper_5");


        Dataset T = Utils.getTDBDataset();
        T.begin(ReadWrite.READ);
        long a = System.currentTimeMillis();
        Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(ExperimentsGenerator.convertToSPARQL(Q_withFeatures,prefixes), T), T);
        long b = System.currentTimeMillis();
        //edges in query; number of covering wrappers;
        System.out.println(CQs);

        T.end();
        T.close();

    }
}
