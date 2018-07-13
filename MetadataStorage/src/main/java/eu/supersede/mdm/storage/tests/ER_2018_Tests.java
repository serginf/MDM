package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.omq.QueryRewriting;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.commons.io.FileUtils;
import scala.Tuple2;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ER_2018_Tests {

    public static void deleteTDB() {
        try {
            FileUtils.deleteDirectory(new File("/Users/snadal/UPC/Projects/MDM/MetadataStorage/MDM_TDBMDM_TDB"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,String> populatePrefixes(String filePath) throws IOException {
        Map<String,String> out = Maps.newHashMap();
        Files.readAllLines(new File(filePath).toPath()).forEach(p -> out.put(p.split(" ")[0],p.split(" ")[1]));
        return out;
    }

    public static void populateTriples(String namedGraph, String filePath, Map<String,String> prefixes) throws IOException {
        Files.readAllLines(new File(filePath).toPath()).stream().filter(t -> !t.startsWith("#")).forEach(t -> {
            String[] spo = t.split(" ");
            String s = prefixes.get(spo[0].split(":")[0])+spo[0].split(":")[1];
            String p = prefixes.get(spo[1].split(":")[0])+spo[1].split(":")[1];
            String o = prefixes.get(spo[2].split(":")[0])+spo[2].split(":")[1];

            RDFUtil.addTriple(namedGraph,s,p,o);
        });
    }

    public static void populateMappings(String mappingsPath, String globalGraphPath, Map<String,String> prefixes) throws IOException {
        List<String> globalGraphTriples =  Files.readAllLines(new File(globalGraphPath).toPath());
        Files.readAllLines(new File(mappingsPath).toPath()).stream().filter(t -> !t.startsWith("#")).forEach(t -> {
            String wrapper = t.split("-")[0];
            String[] indexes = t.split("-")[1].split(",");
            Arrays.stream(indexes).map(i -> Integer.parseInt(i)).forEach(i -> {
                String spo[] = globalGraphTriples.get(i-1).split(" ");
                String s = prefixes.get(spo[0].split(":")[0])+spo[0].split(":")[1];
                String p = prefixes.get(spo[1].split(":")[0])+spo[1].split(":")[1];
                String o = prefixes.get(spo[2].split(":")[0])+spo[2].split(":")[1];
                RDFUtil.addTriple(prefixes.get(wrapper.split(":")[0])+wrapper.split(":")[1],s,p,o);
            });
        });
    }

    public static List<Tuple2<String,String>> getQueries(String filePath, Map<String,String> prefixes) throws IOException {
        List<Tuple2<String,String>> queries = Lists.newArrayList();

        List<String> lines = Files.readAllLines(new File(filePath).toPath());
        String queryDesc = "";
        String query = "";
        boolean first = true;
        for (String line : lines) {
            if (line.startsWith("#")) {
                if (first) first = false;
                else queries.add(new Tuple2<String,String>(queryDesc,query));

                query = prefixes.keySet().stream().map(p -> "PREFIX "+p+": <"+prefixes.get(p)+"> ").reduce(String::concat).get();
                queryDesc = line;
            } else {
                query += line+" ";
            }
        }
        return queries;
    }

    public static void main(String[] args) throws Exception {
        ApacheMain.configPath = "/Users/snadal/UPC/Projects/MDM/MetadataStorage/config.sergi.properties";
        deleteTDB();
        Map<String,String> prefixes = populatePrefixes("/Users/snadal/UPC/Projects/MDM/datasets/ER2018/prefixes.txt");
        populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/ER2018/metamodel.txt", prefixes);
        populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/ER2018/global_graph.txt", prefixes);
        populateTriples("http://www.essi.upc.edu/~snadal/ER_ontology","/Users/snadal/UPC/Projects/MDM/datasets/ER2018/source_graph.txt", prefixes);

        populateMappings("/Users/snadal/UPC/Projects/MDM/datasets/ER2018/mappings.txt",
                "/Users/snadal/UPC/Projects/MDM/datasets/ER2018/global_graph.txt", prefixes);

        List<Tuple2<String,String>> queries = getQueries("/Users/snadal/UPC/Projects/MDM/datasets/ER2018/queries.txt",prefixes);
        queries.forEach(query -> {
            System.out.println(query._1);
            QueryRewriting qr = new QueryRewriting(query._2);
            qr.rewriteAggregations().forEach(w -> System.out.println(w));
            //qr.aggregationStep();

        });

        deleteTDB();
    }

}
