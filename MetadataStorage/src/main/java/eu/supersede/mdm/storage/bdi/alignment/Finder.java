package eu.supersede.mdm.storage.bdi.alignment;

import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple2;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class Finder {
    private String iriA = "";
    private String iriB = "";
    private List<Resource> propertiesA = new ArrayList<>();
    private List<Resource> propertiesB = new ArrayList<>();
    private JSONArray alignmentsArray = new JSONArray();

    public Finder(String iriA, String iriB) {
        System.out.println(iriA + " " + iriB);

        System.out.println(" A ");
        propertiesA = getProperties(iriA);
        System.out.println(propertiesA);
        System.out.println(" B ");
        propertiesB = getProperties(iriB);
        System.out.println(propertiesB);
        comparator();
        System.out.println(alignmentsArray.toJSONString());
    }

    private void comparator() {
        String address = ConfigManager.getProperty("resources_path") + "english_stopwords.txt";
        List<String> stopwords = null;
        try {
            stopwords = Files.readAllLines(Paths.get(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String stopwordsRegex = stopwords.stream().collect(Collectors.joining("|", "\\b(", ")\\b\\s?"));

        HashMap<String, String> map = new HashMap<String, String>();
        for (Resource rA : propertiesA) {
            for (Resource rB : propertiesB) {
                //TODO Handle all cases here
                if (!rA.getLocalName().trim().equals(rB.getLocalName().trim())) {
                    //Split based on words

                    String rAwithoutStopWords = rA.getLocalName().replaceAll("_", " ").toLowerCase().replaceAll(stopwordsRegex, "");
                    String rBwithoutStopWords = rB.getLocalName().replaceAll("_", " ").toLowerCase().replaceAll(stopwordsRegex, "");

                    int countWordsA = rAwithoutStopWords.split(" ").length;
                    int countWordsB = rBwithoutStopWords.split(" ").length;

                    String[] wordsInA = rAwithoutStopWords.split(" ");
                    String[] wordsInB = rBwithoutStopWords.split(" ");

                    double total = countWordsA + countWordsB;

                    HashSet<String> set = new HashSet<String>();

                    for (int i = 0; i < wordsInA.length; i++) {
                        for (int j = 0; j < wordsInB.length; j++) {
                            if (wordsInA[i].equals(wordsInB[j])) {
                                set.add(wordsInA[i]);
                            }
                        }
                    }

                    if (set.size() > 0) {
                        double setSize = set.size();
                        double c = setSize / (total - setSize);
                        double confidence = c * 100;
                        System.out.println(set + " --> " + rA.getLocalName() + " --- And ---- " + rB.getLocalName() + " -- " + c);
                        if (confidence > 20.0) {
                            if (!map.containsKey(rA.getURI() + rB.getURI())) {
                                map.put(rA.getURI() + rB.getURI(), Double.toString(c));

                                JSONObject alignments = new JSONObject();
                                alignments.put("s", rA.getURI());
                                alignments.put("p", rB.getURI());
                                alignments.put("confidence", Double.toString(c));
                                alignments.put("mapping_type", "DATA-PROPERTY");
                                alignments.put("lexical_confidence", Double.toString(c));
                                alignments.put("structural_confidence", Double.toString(c));
                                alignments.put("mapping_direction", Double.toString(c));
                                alignmentsArray.add(alignments);

                            }
                        }
                    }

                }
            }
        }
    }

    public JSONArray getAlignmentsArray() {
        return alignmentsArray;
    }

    private List<Resource> getProperties(String iri) {
        String getProperties = " SELECT * WHERE { GRAPH <" + iri + "> { ?property rdfs:domain ?domain; rdfs:range ?range . FILTER NOT EXISTS {?range rdf:type rdfs:Class.}} }";
        List<Resource> properties = new ArrayList<>();

        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getProperties, iri).forEachRemaining(triple -> {
            //System.out.print(triple.get("property") + "\t");
            //System.out.print(triple.get("domain") + "\t");
            //System.out.print(triple.get("range") + "\n");
            //System.out.println(triple.get("property"));
            properties.add(triple.getResource("property"));
        });
        return properties;
    }
}
