package eu.supersede.mdm.storage.tests.SIGMOD;

import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.tests.TestUtils;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class OneQueryTests {

    private static int CLIQUE_SIZE = 50;

    private static int MAX_EDGES_IN_QUERY = 3; //The number of edges in G computed as a subgraph of the clique

    private static int MAX_WRAPPERS = 30;
    private static float COVERED_FEATURES_QUERY = .1f; //Probability that a query includes a feature
    private static float COVERED_FEATURES_WRAPPER = .75f; //Probability that a wrapper includes a feature

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/SIGMOD_CQ/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/metamodel.txt", prefixes);

        Random random = new Random(System.currentTimeMillis());
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";

        //Generate a clique of concepts
        IntegrationGraph clique = ExperimentsGenerator.generateCliqueGraphOfConcepts(CLIQUE_SIZE);
        //Here Q=G
        IntegrationGraph Q = ExperimentsGenerator.getConnectedRandomSubgraph(clique,1,false);
        for (int i = 2; i <= 2; ++i) {
            ExperimentsGenerator.expandWithOneEdge(Q,clique);
        }
        Q.printAsWebGraphViz();System.out.println("");
        IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,1,1f);
        System.out.println("Q with features is");Q_withFeatures.printAsWebGraphViz();System.out.println("");
        Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        for (int j = 1; j <= MAX_WRAPPERS; ++j) {
            IntegrationGraph W = ExperimentsGenerator.getConnectedRandomSubgraphFromDAG(Q,1);
            IntegrationGraph W_withFeatures = ExperimentsGenerator.addFeatures(W,1,1f);
            ExperimentsGenerator.registerWrapper(W_withFeatures,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
            System.out.println("W with features is");W_withFeatures.printAsWebGraphViz();System.out.println("");
        }
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
