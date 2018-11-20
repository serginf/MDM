package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SIGMOD;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_SimpleGraph;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConjunctiveQuery_Tests {
    private static String basePath = "/home/snadal/UPC/Projects/MDM/";
    public static void main(String[] args) throws Exception {

        String sourceGraph = "" +
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
                "sup:?_time owl:sameAs sup:hId";
        String eventMapping = "2,3,4,5,6,7,8,9,11,17,19,21,22,23,24,25,26,27,29,35,37,39,40";

        for (int n = 1; n <= 50; ++n) {

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


            System.out.println(tempMappingsFile.toAbsolutePath().toString());
            System.out.println(tempSourceGraphFile.toAbsolutePath().toString());


            for (int i = 1; i <= n; ++i) {
                //String wrapperName = "Wrapper_" + i;//"+RandomStringUtils.random(5,"abcdefghijklmnopqrstuwvxyz");
                String wrapperName = "Wrapper_" + RandomStringUtils.random(3, "abcdefghijklmnopqrstuwvxyz");

                Files.write(tempMappingsFile, ("\nsup:" + wrapperName + "-" + eventMapping).getBytes(), StandardOpenOption.APPEND);
                Files.write(tempSourceGraphFile, (sourceGraph.replace("?", wrapperName)).getBytes(), StandardOpenOption.APPEND);
            }

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
                //System.out.println(query._1);
                long a = System.currentTimeMillis();
                //List<ConjunctiveQuery> CQs = Lists.newArrayList(QueryRewriting_SIGMOD.rewriteToUnionOfConjunctiveQueries(QueryRewriting_SimpleGraph.parseSPARQL(query._2, T), T));
                long b = System.currentTimeMillis();
                //for (int i = 0; i < CQs.size(); ++i) {
                //    System.out.println("    [" + (i + 1) + "/" + (CQs.size()) + "]: " + CQs.get(i));
                //}
                //System.out.println(n+" wrappers - "+CQs.size()+" CQs");
                System.out.println("    processing time = "+(b-a)+" ms");
            };
            T.abort();
            T.close();
            Files.delete(tempMappingsFile.toAbsolutePath());
            Files.delete(tempSourceGraphFile.toAbsolutePath());
            TestUtils.deleteTDB();
        }
    }
}
