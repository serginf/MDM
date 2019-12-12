package eu.supersede.mdm.storage.experiments;

import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.experiments.datalog.DatalogConverter;
import eu.supersede.mdm.storage.experiments.datalog.DatalogExperimentsRunner;
import eu.supersede.mdm.storage.experiments.datalog.DatalogQuery;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_EdgeBased;
import eu.supersede.mdm.storage.tests.TestUtils;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class OneQueryTests {

    private static int CLIQUE_SIZE = 6;

    private static int MAX_EDGES_IN_QUERY = 2; //The number of edges in G computed as a subgraph of the clique

    private static int MAX_WRAPPERS = 3;
    private static int MAX_FEATURES = 1;

    //private static float COVERED_FEATURES_QUERY = .1f; //Probability that a query includes a feature
    private static float COVERED_FEATURES_WRAPPER = 1f; //Probability that a wrapper includes a feature

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/Cyclic_AB-BC-CA/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/metamodel.txt", prefixes);

        Random random = new Random(System.currentTimeMillis());
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";

        //Generate a clique of concepts
        IntegrationGraph clique = ExperimentsGenerator.generateCliqueGraphOfConcepts(CLIQUE_SIZE);
        IntegrationGraph clique_withFeatures = ExperimentsGenerator.addFeatures(clique,MAX_FEATURES,1f);
        //System.out.println("The clique is");clique.printAsWebGraphViz();System.out.println("");
        //Here Q=G
        IntegrationGraph Q = ExperimentsGenerator.getConnectedRandomSubgraph(clique,MAX_EDGES_IN_QUERY,true);
        /*for (int i = 1; i < MAX_EDGES_IN_QUERY; ++i) {
            ExperimentsGenerator.expandWithOneEdge(Q,clique);
        }*/
        //System.out.println("Query only with concepts is");Q.printAsWebGraphViz();System.out.println("");

        IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,MAX_FEATURES,1f);

        Set<DatalogQuery> datalogQueries = Sets.newHashSet();
        DatalogQuery dlQ = DatalogConverter.convert("q",Q_withFeatures,clique_withFeatures);
        datalogQueries.add(dlQ);


        System.out.println("Q with features is");Q_withFeatures.printAsWebGraphViz();System.out.println("");
        Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        for (int j = 1; j <= MAX_WRAPPERS; ++j) {
            IntegrationGraph W = ExperimentsGenerator.getConnectedRandomSubgraphFromDAG(Q,1);
            IntegrationGraph W_withFeatures = ExperimentsGenerator.addFeatures(W,MAX_FEATURES,COVERED_FEATURES_WRAPPER);
            DatalogQuery dlW = DatalogConverter.convert("w"+j,W_withFeatures,clique_withFeatures);
            datalogQueries.add(dlW);
            String wrapperName = ExperimentsGenerator.registerWrapper(W_withFeatures,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
            System.out.println(wrapperName+" with features is");W_withFeatures.printAsWebGraphViz();System.out.println("");
        }
        Set<String> rewritings = DatalogExperimentsRunner.runMiniCon(DatalogConverter.minimizeDatalog(datalogQueries));
        System.out.println(rewritings);
        //System.exit(0);


        Dataset T = Utils.getTDBDataset();
        T.begin(ReadWrite.READ);
        long a = System.currentTimeMillis();
        Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting_EdgeBased.
                rewriteToUnionOfConjunctiveQueries(QueryRewriting_EdgeBased.parseSPARQL(ExperimentsGenerator.convertToSPARQL(Q_withFeatures,prefixes), T), T);
        long b = System.currentTimeMillis();
        //edges in query; number of covering wrappers;
        System.out.println(CQs);
        T.end();
        T.close();

    }
}
