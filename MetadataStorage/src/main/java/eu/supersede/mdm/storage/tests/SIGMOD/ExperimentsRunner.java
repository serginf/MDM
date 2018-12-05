package eu.supersede.mdm.storage.tests.SIGMOD;

import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SIGMOD_Optimized;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
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

    private static int MAX_EDGES_IN_QUERY = 25; //The number of edges in G computed as a subgraph of the clique

    private static int MAX_WRAPPERS = 50;
    private static float COVERED_FEATURES_QUERY = .1f; //Probability that a query includes a feature
    private static float COVERED_FEATURES_WRAPPER = .25f; //Probability that a wrapper includes a feature

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    public static void main(String[] args) throws Exception {
        if (args.length>0) {
            if (args.length!=6) throw new Exception("usage: CLIQUE_SIZE (def. 50), UPPER_BOUND_FEATURES_IN_G (def 15), " +
                    "MAX_EDGES_IN_QUERY (def 25), MAX_WRAPPERS (def 50), COVERED_FEATURES_QUERY (def .25), COVERED_FEATURES_WRAPPER (def .75)");
            CLIQUE_SIZE=Integer.parseInt(args[0]);
            UPPER_BOUND_FEATURES_IN_G=Integer.parseInt(args[1]);
            MAX_EDGES_IN_QUERY=Integer.parseInt(args[2]);
            MAX_WRAPPERS=Integer.parseInt(args[3]);
            COVERED_FEATURES_QUERY=Float.parseFloat(args[4]);
            COVERED_FEATURES_WRAPPER=Float.parseFloat(args[5]);
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
        for (int i = 2; i <= MAX_EDGES_IN_QUERY; ++i) {
            //IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_QUERY);
            //Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
            /*for (int j = 1; j <= MAX_WRAPPERS; ++j) {
                IntegrationGraph W = ExperimentsGenerator.getConnectedRandomSubgraphFromDAG(Q,random.nextInt(i)+1);
                IntegrationGraph W_withFeatures = ExperimentsGenerator.addFeatures(W,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_WRAPPER);
                ExperimentsGenerator.registerWrapper(W_withFeatures,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology");

                Dataset T = Utils.getTDBDataset();
                T.begin(ReadWrite.READ);
                long a = System.currentTimeMillis();
                Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting_SIGMOD.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(ExperimentsGenerator.convertToSPARQL(Q_withFeatures,prefixes), T), T);
                long b = System.currentTimeMillis();
                //edges in query; number of covering wrappers;
                System.out.println(i+";"+j+";"+CQs._1+";"+CQs._2.size()+";"+(b-a));
                T.end();
                T.close();
            }*/
            //TestUtils.deleteTDB();
            ExperimentsGenerator.expandWithOneEdge(Q,clique);
        }
        IntegrationGraph Q_withFeatures = ExperimentsGenerator.addFeatures(Q,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_QUERY);
        //System.out.println("Your query is");Q_withFeatures.printAsWebGraphViz();System.out.println("");

        Q_withFeatures.registerRDFDataset("http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        for (int j = 1; j <= MAX_WRAPPERS; ++j) {
            IntegrationGraph W = ExperimentsGenerator.getConnectedRandomSubgraphFromDAG(Q,random.nextInt(MAX_EDGES_IN_QUERY)+1);
            IntegrationGraph W_withFeatures = ExperimentsGenerator.addFeatures(W,UPPER_BOUND_FEATURES_IN_G,COVERED_FEATURES_WRAPPER);
            //System.out.println("Your wrapper is");W_withFeatures.printAsWebGraphViz();System.out.println("");
            ExperimentsGenerator.registerWrapper(W_withFeatures,"http://www.essi.upc.edu/~snadal/SIGMOD_ontology");
        }
        Dataset T = Utils.getTDBDataset();
        T.begin(ReadWrite.READ);
        long a = System.currentTimeMillis();
        Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting_SIGMOD_Optimized.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(ExperimentsGenerator.convertToSPARQL(Q_withFeatures,prefixes), T), T);
        long b = System.currentTimeMillis();
        //edges in query; number of covering wrappers;
        System.out.println(UPPER_BOUND_FEATURES_IN_G+";"+MAX_EDGES_IN_QUERY+";"+MAX_WRAPPERS+";"+COVERED_FEATURES_QUERY+
                ";"+COVERED_FEATURES_WRAPPER+";"+CQs._1+";"+CQs._2.size()+";"+(b-a));

        //System.out.println(CQs);

        T.end();
        T.close();

    }
}
