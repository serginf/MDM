package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.util.List;
import java.util.Map;

public class ConjunctiveQuery_Tests {
    private static String basePath = "/home/snadal/UPC/Projects/MDM/";
    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath+"MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String,String> prefixes = TestUtils.populatePrefixes(basePath+"datasets/scenarios/InfSyst_SUPERSEDE/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology",basePath+"datasets/scenarios/InfSyst_SUPERSEDE/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology",basePath+"datasets/scenarios/InfSyst_SUPERSEDE/global_graph.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology",basePath+"datasets/scenarios/InfSyst_SUPERSEDE/source_graph.txt", prefixes);
        TestUtils.populateMappings(basePath+"datasets/scenarios/InfSyst_SUPERSEDE/mappings.txt",
                basePath+"datasets/scenarios/InfSyst_SUPERSEDE/global_graph.txt", prefixes);
        List<Tuple2<String,String>> queries = TestUtils.getQueries(basePath+"datasets/scenarios/InfSyst_SUPERSEDE/queries.txt",prefixes);

        Dataset T = Utils.getTDBDataset(); T.begin(ReadWrite.READ);

        queries.forEach(query -> {
            System.out.println(query._1);
            List<ConjunctiveQuery> CQs = Lists.newArrayList(QueryRewriting_SimpleGraph.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(query._2,T),T));
            for (int i = 0; i < CQs.size(); ++i) {
                System.out.println("    [" + (i + 1) + "/" + (CQs.size()) + "]: " + CQs.get(i));
            }
        });
        TestUtils.deleteTDB();
    }
}
