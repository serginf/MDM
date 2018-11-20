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
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConjunctiveQuery_AllWrapper_Tests {

    private static String basePath = "/home/snadal/UPC/Projects/MDM/";

    private static int START_N = 1;
    private static int END_N = 50;

    public static void main(String[] args) throws Exception {

        Map<String,List<String>> wrapperNames = Maps.newHashMap();
        wrapperNames.put("InfoMonitor",Lists.newArrayList("Wrapper_InfoMonitor_1", "Wrapper_InfoMonitor_2", "Wrapper_InfoMonitor_3", "Wrapper_InfoMonitor_4", "Wrapper_InfoMonitor_5", "Wrapper_InfoMonitor_6", "Wrapper_InfoMonitor_7", "Wrapper_InfoMonitor_8", "Wrapper_InfoMonitor_9", "Wrapper_InfoMonitor_10", "Wrapper_InfoMonitor_11", "Wrapper_InfoMonitor_12", "Wrapper_InfoMonitor_13", "Wrapper_InfoMonitor_14", "Wrapper_InfoMonitor_15", "Wrapper_InfoMonitor_16", "Wrapper_InfoMonitor_17", "Wrapper_InfoMonitor_18", "Wrapper_InfoMonitor_19", "Wrapper_InfoMonitor_20", "Wrapper_InfoMonitor_21", "Wrapper_InfoMonitor_22", "Wrapper_InfoMonitor_23", "Wrapper_InfoMonitor_24", "Wrapper_InfoMonitor_25", "Wrapper_InfoMonitor_26", "Wrapper_InfoMonitor_27", "Wrapper_InfoMonitor_28", "Wrapper_InfoMonitor_29", "Wrapper_InfoMonitor_30" ));
        wrapperNames.put("Time",Lists.newArrayList("Wrapper_Time_1", "Wrapper_Time_2", "Wrapper_Time_3", "Wrapper_Time_4", "Wrapper_Time_5", "Wrapper_Time_6", "Wrapper_Time_7", "Wrapper_Time_8", "Wrapper_Time_9", "Wrapper_Time_10", "Wrapper_Time_11", "Wrapper_Time_12", "Wrapper_Time_13", "Wrapper_Time_14", "Wrapper_Time_15", "Wrapper_Time_16", "Wrapper_Time_17", "Wrapper_Time_18", "Wrapper_Time_19", "Wrapper_Time_20", "Wrapper_Time_21", "Wrapper_Time_22", "Wrapper_Time_23", "Wrapper_Time_24", "Wrapper_Time_25", "Wrapper_Time_26", "Wrapper_Time_27", "Wrapper_Time_28", "Wrapper_Time_29", "Wrapper_Time_30" ));
        wrapperNames.put("Monitor",Lists.newArrayList("Wrapper_Monitor_1", "Wrapper_Monitor_2", "Wrapper_Monitor_3", "Wrapper_Monitor_4", "Wrapper_Monitor_5", "Wrapper_Monitor_6", "Wrapper_Monitor_7", "Wrapper_Monitor_8", "Wrapper_Monitor_9", "Wrapper_Monitor_10", "Wrapper_Monitor_11", "Wrapper_Monitor_12", "Wrapper_Monitor_13", "Wrapper_Monitor_14", "Wrapper_Monitor_15", "Wrapper_Monitor_16", "Wrapper_Monitor_17", "Wrapper_Monitor_18", "Wrapper_Monitor_19", "Wrapper_Monitor_20", "Wrapper_Monitor_21", "Wrapper_Monitor_22", "Wrapper_Monitor_23", "Wrapper_Monitor_24", "Wrapper_Monitor_25", "Wrapper_Monitor_26", "Wrapper_Monitor_27", "Wrapper_Monitor_28", "Wrapper_Monitor_29", "Wrapper_Monitor_30" ));
        wrapperNames.put("Apps",Lists.newArrayList("Wrapper_Apps_1", "Wrapper_Apps_2", "Wrapper_Apps_3", "Wrapper_Apps_4", "Wrapper_Apps_5", "Wrapper_Apps_6", "Wrapper_Apps_7", "Wrapper_Apps_8", "Wrapper_Apps_9", "Wrapper_Apps_10", "Wrapper_Apps_11", "Wrapper_Apps_12", "Wrapper_Apps_13", "Wrapper_Apps_14", "Wrapper_Apps_15", "Wrapper_Apps_16", "Wrapper_Apps_17", "Wrapper_Apps_18", "Wrapper_Apps_19", "Wrapper_Apps_20", "Wrapper_Apps_21", "Wrapper_Apps_22", "Wrapper_Apps_23", "Wrapper_Apps_24", "Wrapper_Apps_25", "Wrapper_Apps_26", "Wrapper_Apps_27", "Wrapper_Apps_28", "Wrapper_Apps_29", "Wrapper_Apps_30" ));


        Map<String,String> sourceGraph = Maps.newHashMap();
        sourceGraph.put("InfoMonitor", "" +
                "\nsup:? rdf:type S:Wrapper\n" +
                "sup:?_idMonitor rdf:type S:Attribute\n" +
                "sup:?_br rdf:type S:Attribute\n" +
                "sup:?_lr rdf:type S:Attribute\n" +
                "sup:?_time rdf:type S:Attribute\n" +
                "sup:? S:hasAttribute sup:?_idMonitor\n" +
                "sup:? S:hasAttribute sup:?_br\n" +
                "sup:? S:hasAttribute sup:?_lr\n" +
                "sup:? S:hasAttribute sup:?_time\n" +
                "sup:?_idMonitor owl:sameAs sup:dcId\n" +
                "sup:?_br owl:sameAs sup:bitRate\n" +
                "sup:?_lr owl:sameAs sup:lagRatio\n" +
                "sup:?_time owl:sameAs sup:hId");
        sourceGraph.put("Monitor", "\nsup:? rdf:type S:Wrapper\n" +
                        "sup:?_app rdf:type S:Attribute\n" +
                        "sup:?_nameMon rdf:type S:Attribute\n" +
                        "sup:?_idMon rdf:type S:Attribute\n" +
                        "sup:? S:hasAttribute sup:?_app\n" +
                        "sup:? S:hasAttribute sup:?_nameMon\n" +
                        "sup:? S:hasAttribute sup:?_idMon\n" +
                        "sup:?_app owl:sameAs sup:appId\n" +
                        "sup:?_nameMon owl:sameAs sup:collectorName\n" +
                        "sup:?_idMon owl:sameAs sup:dcId");
        sourceGraph.put("Time", "\nsup:? rdf:type S:Wrapper\n" +
                "sup:?_hourId rdf:type S:Attribute\n" +
                "sup:?_minId rdf:type S:Attribute\n" +
                "sup:?_secId rdf:type S:Attribute\n" +
                "sup:?_hName rdf:type S:Attribute\n" +
                "sup:? S:hasAttribute sup:?_hourId\n" +
                "sup:? S:hasAttribute sup:?_minId\n" +
                "sup:? S:hasAttribute sup:?_secId\n" +
                "sup:? S:hasAttribute sup:hName\n" +
                "sup:?_hourId owl:sameAs sup:hId\n" +
                "sup:?_hName owl:sameAs sup:hourName");
        sourceGraph.put("Apps", "\nsup:? rdf:type S:Wrapper\n" +
                "sup:?_appName rdf:type S:Attribute\n" +
                "sup:?_appVersion rdf:type S:Attribute\n" +
                "sup:?_idApp rdf:type S:Attribute\n" +
                "sup:? S:hasAttribute sup:?_appName\n" +
                "sup:? S:hasAttribute sup:?_appVersion\n" +
                "sup:? S:hasAttribute sup:?_idApp\n" +
                "sup:?_appName owl:sameAs sup:name\n" +
                "sup:?_appVersion owl:sameAs sup:version\n" +
                "sup:?_idApp owl:sameAs sup:appId");


        Map<String,String> eventMapping = Maps.newHashMap();
        eventMapping.put("InfoMonitor", "2,3,4,5,6,7,8,9,11,17,19,21,22,23,24,25,26,27,29,35,37,39,40");
        eventMapping.put("Monitor","7,8,9,10,11,12,26,27,28,29,30,40,41");
        eventMapping.put("Time","5,6,24,39,45,46");
        eventMapping.put("Apps","11,12,13,14,30,31,32,41");

        Map<String,Float> probabilities = Maps.newLinkedHashMap();
        probabilities.put("InfoMonitor",0.25f);
        probabilities.put("Monitor",0.5f);
        probabilities.put("Time",0.75f);
        probabilities.put("Apps",1f);

        for (int n = START_N; n <= END_N; ++n) {

            Path tempMappingsFile = Files.createTempFile("sigmod", "txt");
            File originalMappingsFile = new File(basePath + "datasets/scenarios/SIGMOD_CQ/mappings.txt");
            FileChannel srcMappings = new FileInputStream(originalMappingsFile).getChannel();
            FileChannel destMappings = new FileOutputStream(tempMappingsFile.toFile()).getChannel();
            destMappings.transferFrom(srcMappings, 0, srcMappings.size());

            Path tempSourceGraphFile = Files.createTempFile("sigmod", "txt");
            File originalSourceGraphFile = new File(basePath + "datasets/scenarios/SIGMOD_CQ/source_graph.txt");
            FileChannel srcSourceGraph = new FileInputStream(originalSourceGraphFile).getChannel();
            FileChannel destSourceGraph = new FileOutputStream(tempSourceGraphFile.toFile()).getChannel();
            destSourceGraph.transferFrom(srcSourceGraph, 0, srcSourceGraph.size());

//            for (Map.Entry<String,Float> e : probabilities.entrySet()) {
            String wrapperName="";
            String key = "";
            Random random=new Random();
            float f = random.nextFloat();
            List<Map.Entry<String,Float>> probs = Lists.newArrayList(probabilities.entrySet());
            boolean found = false;
            for (Map.Entry<String,Float> entry : probs) {
                if (f<entry.getValue() && !found) {
                    wrapperName=wrapperNames.get(entry.getKey()).get(n);
                    key=entry.getKey();
                    found=true;
                }
            }
            System.out.println("adding "+wrapperName);
            try {
                Files.write(tempMappingsFile, ("\nsup:" + wrapperName + "-" + eventMapping.get(key)).getBytes(), StandardOpenOption.APPEND);
                Files.write(tempSourceGraphFile, (sourceGraph.get(key).replace("?", wrapperName)).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException exc) {
                exc.printStackTrace();
            }

//            };

            //}

            ApacheMain.configPath = basePath + "MetadataStorage/config.sergi.properties";
            TestUtils.deleteTDB();
            Map<String, String> prefixes = TestUtils.populatePrefixes(basePath + "datasets/scenarios/SIGMOD_CQ/prefixes.txt");
            TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/metamodel.txt", prefixes);
            TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", basePath + "datasets/scenarios/SIGMOD_CQ/global_graph.txt", prefixes);
            //TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology",basePath+"datasets/scenarios/SIGMOD_CQ/source_graph.txt", prefixes);
            TestUtils.populateTriples("http://www.essi.upc.edu/~snadal/SIGMOD_ontology", tempSourceGraphFile.toAbsolutePath().toString(), prefixes);
            TestUtils.populateMappings(/*basePath+"datasets/scenarios/SIGMOD_CQ/mappings.txt"*/tempMappingsFile.toAbsolutePath().toString(),
                    basePath + "datasets/scenarios/SIGMOD_CQ/global_graph.txt", prefixes);
            List<Tuple2<String, String>> queries = TestUtils.getQueries(basePath + "datasets/scenarios/SIGMOD_CQ/queries.txt", prefixes);

            Dataset T = Utils.getTDBDataset();
            T.begin(ReadWrite.READ);

            for (Tuple2<String, String> query : queries) {
                System.out.println(query._1);
                long a = System.currentTimeMillis();
                //List<ConjunctiveQuery> CQs = Lists.newArrayList(QueryRewriting_SIGMOD.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(query._2, T), T));
                long b = System.currentTimeMillis();
                /*for (int i = 0; i < CQs.size(); ++i) {
                    System.out.println("    [" + (i + 1) + "/" + (CQs.size()) + "]: " + CQs.get(i));
                }*/
                //System.out.println(n+" wrappers - "+CQs.size()+" CQs");
                System.out.println("    processing time = "+(b-a)+" ms");
            };
            T.end();
            T.close();

            Files.delete(tempMappingsFile.toAbsolutePath());
            Files.delete(tempSourceGraphFile.toAbsolutePath());
            TestUtils.deleteTDB();
        }
    }
}
