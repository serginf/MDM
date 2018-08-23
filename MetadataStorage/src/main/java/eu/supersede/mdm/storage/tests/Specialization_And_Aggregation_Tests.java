package eu.supersede.mdm.storage.tests;

import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.util.List;
import java.util.Map;

public class Specialization_And_Aggregation_Tests {
    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = basePath+"MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String,String> prefixes = TestUtils.populatePrefixes(basePath+"datasets/scenarios/ER2018/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology",basePath+"datasets/scenarios/ER2018/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology",basePath+"datasets/scenarios/ER2018/global_graph.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology",basePath+"datasets/scenarios/ER2018/source_graph.txt", prefixes);
        TestUtils.populateMappings(basePath+"datasets/scenarios/ER2018/mappings.txt",
                basePath+"datasets/scenarios/ER2018/global_graph.txt", prefixes);
        List<Tuple2<String,String>> queries = TestUtils.getQueries(basePath+"datasets/scenarios/ER2018/queries.txt",prefixes);

        Dataset T = Utils.getTDBDataset(); T.begin(ReadWrite.WRITE);

        queries.forEach(query -> {
            System.out.println(query._1);
            QueryRewriting_SimpleGraph.rewriteToUnionOfConjunctiveAggregateQueries(QueryRewriting_SimpleGraph.parseSPARQL(query._2(),T),T);

        });
        TestUtils.deleteTDB();
    }

}
