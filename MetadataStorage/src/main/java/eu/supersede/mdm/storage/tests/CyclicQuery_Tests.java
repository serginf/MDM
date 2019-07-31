package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_EdgeBased;
import eu.supersede.mdm.storage.tests.TestUtils;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CyclicQuery_Tests {

    private static String basePath = "/home/snadal/UPC/Projects/MDM_v2/MDM/";

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/VLDB2020/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/VLDB2020", basePath + "datasets/scenarios/VLDB2020/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/VLDB2020", basePath + "datasets/scenarios/VLDB2020/global_graph.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/VLDB2020",basePath+"datasets/scenarios/VLDB2020/source_graph.txt", prefixes);
        TestUtils.populateMappings(basePath+"datasets/scenarios/VLDB2020/mappings.txt",
                basePath + "datasets/scenarios/VLDB2020/global_graph.txt", prefixes);

        List<Tuple2<String, String>> queries = TestUtils.getQueries(basePath + "datasets/scenarios/VLDB2020/queries.txt", prefixes);

        Dataset T = Utils.getTDBDataset();
        T.begin(ReadWrite.READ);

        for (Tuple2<String, String> query : queries) {
            System.out.println(query._1);

            Set<ConjunctiveQuery> CQs = QueryRewriting_EdgeBased.rewriteToUnionOfConjunctiveQueries(QueryRewriting_EdgeBased.parseSPARQL(query._2,T),T)._2;
            System.out.println(CQs);
        }
        T.end();
        T.close();
        TestUtils.deleteTDB();
    }
}
