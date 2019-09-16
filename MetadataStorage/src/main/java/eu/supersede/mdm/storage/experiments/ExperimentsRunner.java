package eu.supersede.mdm.storage.experiments;

import eu.supersede.mdm.storage.ApacheMain;
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

public class ExperimentsRunner {

    private static int CLIQUE_SIZE = 50;
    private static int UPPER_BOUND_FEATURES_IN_G = 15; //How many features at most per concept (excluding ID)

    private static int N_EDGES_IN_QUERY = 25; //The number of edges in G computed as a subgraph of the clique
    private static int N_WRAPPERS = 500;
    private static int N_EDGES_COVERED_BY_WRAPPERS = 1;

    private static float COVERED_FEATURES_QUERY = .1f; //Probability that a query includes a feature
    private static float COVERED_FEATURES_WRAPPER = .25f; //Probability that a wrapper includes a feature

    private static String basePath = "/home/snadal/UPC/Projects/MDM_v2/MDM/";

    public static void main(String[] args) throws Exception {
        if (args.length>0) {
            if (args.length!=7) throw new Exception("usage: CLIQUE_SIZE (def. 50), UPPER_BOUND_FEATURES_IN_G (def 15), " +
                    "N_EDGES_IN_QUERY (def 25), N_WRAPPERS (def 50), N_EDGES_COVERED_BY_WRAPPERS (def 1), " +
                    "COVERED_FEATURES_QUERY (def .25), COVERED_FEATURES_WRAPPER (def .75)");
            CLIQUE_SIZE=Integer.parseInt(args[0]);
            UPPER_BOUND_FEATURES_IN_G=Integer.parseInt(args[1]);
            N_EDGES_IN_QUERY=Integer.parseInt(args[2]);
            N_WRAPPERS=Integer.parseInt(args[3]);
            N_EDGES_COVERED_BY_WRAPPERS = Integer.parseInt(args[4]);
            COVERED_FEATURES_QUERY=Float.parseFloat(args[5]);
            COVERED_FEATURES_WRAPPER=Float.parseFloat(args[6]);
        }

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
        for (int i = 2; i <= N_EDGES_IN_QUERY; ++i)
            ExperimentsGenerator.expandWithOneEdge(Q,clique);

        IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_QUERY);
        //System.out.println("Your query is");Q_withFeatures.printAsWebGraphViz();System.out.println("");

        Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        for (int j = 1; j <= N_WRAPPERS; ++j) {
            IntegrationGraph W = ExperimentsGenerator.getConnectedRandomSubgraphFromDAG(Q,N_EDGES_COVERED_BY_WRAPPERS);
            IntegrationGraph W_withFeatures = ExperimentsGenerator.addFeatures(W,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_WRAPPER);
            //System.out.println("Your wrapper is");W_withFeatures.printAsWebGraphViz();System.out.println("");
            ExperimentsGenerator.registerWrapper(W_withFeatures,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        }
        Dataset T = Utils.getTDBDataset();
        T.begin(ReadWrite.READ);
        long a = System.currentTimeMillis();
        Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting_EdgeBased.rewriteToUnionOfConjunctiveQueries
                (QueryRewriting_EdgeBased.parseSPARQL
                        (ExperimentsGenerator.convertToSPARQL(Q_withFeatures,prefixes), T), T);
        long b = System.currentTimeMillis();
        //edges in query; number of covering wrappers;
        System.out.println(UPPER_BOUND_FEATURES_IN_G+";"+N_EDGES_IN_QUERY+";"+N_WRAPPERS+";"+N_EDGES_COVERED_BY_WRAPPERS+";"+COVERED_FEATURES_QUERY+
                ";"+COVERED_FEATURES_WRAPPER+";"+CQs._1+";"+CQs._2.size()+";"+(b-a));

        //System.out.println(CQs);

        T.end();
        T.close();

    }
}
