package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.QueryRewriting;
import eu.supersede.mdm.storage.model.omq.Walk;
import eu.supersede.mdm.storage.util.Tuple2;

import java.util.List;
import java.util.Map;

public class ConjunctiveQuery_Tests {
    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = "/Users/snadal/UPC/Projects/MDM/MetadataStorage/config.sergi.properties";
        TestUtils.deleteTDB();
        Map<String,String> prefixes = TestUtils.populatePrefixes("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/global_graph.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/InfSyst_ontology","/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/source_graph.txt", prefixes);
        TestUtils.populateMappings("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/mappings.txt",
                "/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/global_graph.txt", prefixes);
        List<Tuple2<String,String>> queries = TestUtils.getQueries("/Users/snadal/UPC/Projects/MDM/datasets/scenarios/InfSyst_SUPERSEDE/queries.txt",prefixes);
        queries.forEach(query -> {
            System.out.println(query._1);
            List<Walk> walks = Lists.newArrayList(new QueryRewriting(query._2).rewrite());
            for (int i = 0; i < walks.size(); ++i) {
                System.out.println("    [" + (i + 1) + "/" + (walks.size()) + "]: " + walks.get(i));
            }
        });
        TestUtils.deleteTDB();
    }
}
