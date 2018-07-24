package eu.supersede.mdm.storage.tests;

import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.util.Tuple2;

import java.util.List;
import java.util.Map;

public class ConjunctiveAggregateQuery_Tests {

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = "/Users/snadal/UPC/Projects/MDM/MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String,String> prefixes = TestUtils.populatePrefixes("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/global_graph.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/source_graph.txt", prefixes);
        TestUtils.populateMappings("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/mappings.txt",
                "/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/global_graph.txt", prefixes);
        List<Tuple2<String,String>> queries = TestUtils.getQueries("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/ER2018/queries.txt",prefixes);
        queries.forEach(query -> {
            System.out.println(query._1);
            QueryRewriting_SimpleGraph qr = new QueryRewriting_SimpleGraph(query._2);
            qr.rewriteAggregations().forEach(w -> {
                System.out.println("QUERY REWRITING RESULT:");
                System.out.println(w);
            });
            //qr.aggregationStep();

        });
        TestUtils.deleteTDB();
    }

}
