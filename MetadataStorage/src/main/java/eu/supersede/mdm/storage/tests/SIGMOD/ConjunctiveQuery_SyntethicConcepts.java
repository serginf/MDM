package eu.supersede.mdm.storage.tests.SIGMOD;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SIGMOD;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.tests.TestUtils;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ConjunctiveQuery_SyntethicConcepts {

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    private static int START_N = 1;
    private static int END_N = 500;

    public static void main(String[] args) throws Exception {
        TestUtils.deleteTDB();

        String sourceGraph = "" +
                "sup:Wrapper_C#_? rdf:type S:Wrapper\n" +
                "sup:Wrapper_C#_?_F#id rdf:type S:Attribute\n" +
                "sup:Wrapper_C#_?_F#1 rdf:type S:Attribute\n" +
                "sup:Wrapper_C#_?_F#2 rdf:type S:Attribute\n" +
                "sup:Wrapper_C#_?_F#fk rdf:type S:Attribute\n" +
                "sup:Wrapper_C#_? S:hasAttribute sup:Wrapper_C#_?_F#id\n" +
                "sup:Wrapper_C#_? S:hasAttribute sup:Wrapper_C#_?_F#1\n" +
                "sup:Wrapper_C#_? S:hasAttribute sup:Wrapper_C#_?_F#2\n" +
                "sup:Wrapper_C#_? S:hasAttribute sup:Wrapper_C#_?_F#fk\n" +
                "sup:Wrapper_C#_?_F#id owl:sameAs sup:F#id\n" +
                "sup:Wrapper_C#_?_F#1 owl:sameAs sup:F#1\n" +
                "sup:Wrapper_C#_?_F#1 owl:sameAs sup:F#2\n" +
                "sup:Wrapper_C#_?_F#fk owl:sameAs sup:F*id\n";

        Map<Integer,String> eventMapping = Maps.newHashMap();
        eventMapping.put(1, "2,8,9,10,11,27,28,29,30,46,47,53");
        eventMapping.put(2, "3,11,12,13,14,30,31,32,33,47,48,54");
        eventMapping.put(3, "4,14,15,16,17,33,34,35,36,48,49,55");
        eventMapping.put(4, "5,17,18,19,20,36,37,38,39,49,50,56");
        eventMapping.put(5, "6,20,21,22,23,39,40,41,42,50,51,57");
        eventMapping.put(6, "7,23,24,25,42,43,44,51");


        Map<Integer,Float> probabilities = Maps.newLinkedHashMap();
        probabilities.put(1,0.1f);
        probabilities.put(2,0.2f);
        probabilities.put(3,0.3f);
        probabilities.put(4,0.4f);
        probabilities.put(5,0.5f);
        probabilities.put(6,1f);


        ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
        //TestUtils.deleteTDB();
        Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/SIGMOD_CQ/prefixes.txt");
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/metamodel.txt", prefixes);
        TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/global_graph.txt", prefixes);

        //Add 6 wrappers for the 6 concepts

            Path tempMappingsFile = Files.createTempFile("sigmod_mappings_", ".txt");
            Path tempSourceGraphFile = Files.createTempFile("sigmod_source_graph_", ".txt");

            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+1).replace("?","A").replace("*",""+2).getBytes(), StandardOpenOption.APPEND);
            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+2).replace("?","B").replace("*",""+3).getBytes(), StandardOpenOption.APPEND);
            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+3).replace("?","C").replace("*",""+4).getBytes(), StandardOpenOption.APPEND);
            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+4).replace("?","D").replace("*",""+5).getBytes(), StandardOpenOption.APPEND);
            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+5).replace("?","E").replace("*",""+6).getBytes(), StandardOpenOption.APPEND);
            Files.write(tempSourceGraphFile, sourceGraph.replace("#",""+6).replace("?","F").replace("*",""+7).getBytes(), StandardOpenOption.APPEND);

            Files.write(tempMappingsFile, ("sup:Wrapper_C1_A-" + eventMapping.get(1)+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(tempMappingsFile, ("sup:Wrapper_C2_B-" + eventMapping.get(2)+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(tempMappingsFile, ("sup:Wrapper_C3_C-" + eventMapping.get(3)+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(tempMappingsFile, ("sup:Wrapper_C4_D-" + eventMapping.get(4)+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(tempMappingsFile, ("sup:Wrapper_C5_E-" + eventMapping.get(5)+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(tempMappingsFile, ("sup:Wrapper_C6_F-" + eventMapping.get(6)+"\n").getBytes(), StandardOpenOption.APPEND);

            TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", tempSourceGraphFile.toAbsolutePath().toString(), prefixes);
            TestUtils.populateMappings(tempMappingsFile.toAbsolutePath().toString(),basePath + "datasets/scenarios/SIGMOD_CQ/global_graph.txt", prefixes);




        for (int n = START_N; n <= END_N; ++n) {
            Path tempMappingsFile2 = Files.createTempFile("sigmod_mappings_", ".txt");
            Path tempSourceGraphFile2 = Files.createTempFile("sigmod_source_graph_", ".txt");


            String wrapperName="";
            Integer key = 0;
            Random random=new Random();
            float f = random.nextFloat();
            List<Map.Entry<Integer,Float>> probs = Lists.newArrayList(probabilities.entrySet());
            boolean found = false;
            for (Map.Entry<Integer,Float> entry : probs) {
                if (f<entry.getValue() && !found) {
                    wrapperName = "Wrapper_C"+entry.getKey()+"_"+n;
                    key=entry.getKey();
                    found=true;
                }
            }
            //System.out.println("adding "+wrapperName);
            try {
                String sourceGraphContent = sourceGraph.replace("#",""+key).replace("?",""+n).replace("*",""+(key+1));
                //System.out.println(sourceGraphContent);
                Files.write(tempMappingsFile2, ("sup:" + wrapperName + "-" + eventMapping.get(key)+"\n").getBytes(), StandardOpenOption.APPEND);
                Files.write(tempSourceGraphFile2, sourceGraphContent.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException exc) {
                exc.printStackTrace();
            }


            //TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology",basePath+"datasets/scenarios/SIGMOD_CQ/source_graph.txt", prefixes);
            TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", tempSourceGraphFile2.toAbsolutePath().toString(), prefixes);
            TestUtils.populateMappings(/*basePath+"datasets/scenarios/SIGMOD_CQ/mappings.txt"*/tempMappingsFile2.toAbsolutePath().toString(),
                    basePath + "datasets/scenarios/SIGMOD_CQ/global_graph.txt", prefixes);
            List<Tuple2<String, String>> queries = TestUtils.getQueries(basePath + "datasets/scenarios/SIGMOD_CQ/queries.txt", prefixes);

            Dataset T = Utils.getTDBDataset();
            T.begin(ReadWrite.READ);

            for (Tuple2<String, String> query : queries) {
                //System.out.println(query._1);
                long a = System.currentTimeMillis();
                Tuple2<Integer, Set<ConjunctiveQuery>> CQs = QueryRewriting_SIGMOD.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(query._2, T), T);
                long b = System.currentTimeMillis();
                /*for (int i = 0; i < CQs.size(); ++i) {
                    System.out.println("    [" + (i + 1) + "/" + (CQs.size()) + "]: " + CQs.get(i));
                }*/

                // n_wrappers;query;size of intermediate results;size of cqs;processing time
                System.out.println(n+";"+query._1+";"+CQs._1+";"+CQs._2.size()+";"+(b-a));

            };
            T.end();
            T.close();

            //Files.delete(tempMappingsFile.toAbsolutePath());
            //Files.delete(tempSourceGraphFile.toAbsolutePath());
            //TestUtils.deleteTDB();
        }
    }
}
