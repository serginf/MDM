package eu.supersede.mdm.storage.bdi.mdm.constructs;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.LAVMappingResource;
import eu.supersede.mdm.storage.resources.WrapperResource;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.vocabulary.OWL;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDMLavMapping {
    private String mdmGlobalGraphIri;
    private String mdmGgId;
    private JSONArray wrappers;
    private Map<String, List<String>> features = new HashMap<>();
    //private List<Tuple2<String, String>> lavMappings = new ArrayList<>();
    private JSONObject lavMapping;
    private JSONArray featureAndAttributes ;

    public MDMLavMapping(String mdmGlobalGraphIri) {
        this.mdmGlobalGraphIri = mdmGlobalGraphIri;
        run();
    }

    private void run() {
        getFeaturesWithSameAsEdges();
        getWrapperInfoFromGg();
        initLavMapping();
    }

    private void getFeaturesWithSameAsEdges() {
        String SPARQL = "SELECT * WHERE { GRAPH <" + mdmGlobalGraphIri + "> { ?f rdf:type <" + GlobalGraph.FEATURE.val() + "> . OPTIONAL {?f owl:sameAs ?o.} } }";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + SPARQL, mdmGlobalGraphIri).forEachRemaining(triple -> {
            //System.out.print(triple.getResource("f") + "\t");
            //System.out.print(triple.get("o") + "\n");

            //String featureName = triple.get("f").toString().split("/")[triple.get("f").toString().split("/").length - 1];
            String featureName = triple.get("f").toString();
            List<String> sameAsfeatures = new ArrayList<>();

            if (triple.get("o") != null)
                sameAsfeatures.add(triple.get("o").toString());

            if (features.containsKey(featureName)) {
                //System.out.println("Printing value of the key : " + features.get(featureName));
                List<String> tempList = features.get(featureName);
                if (triple.get("o") != null)
                    tempList.add(triple.get("o").toString());
                features.put(featureName, tempList);
            } else {
                features.put(featureName, sameAsfeatures);
            }

        });
        //System.out.println(features);
    }

    private void getWrapperInfoFromGg() {
        MongoClient client = Utils.getMongoDBClient();
        MongoCursor<Document> cursor = MongoCollections.getGlobalGraphCollection(client).find(new Document("namedGraph", mdmGlobalGraphIri)).iterator();

        JSONObject ggInfo = (JSONObject) JSONValue.parse(MongoCollections.getMongoObject(client, cursor));
        wrappers = (JSONArray) ggInfo.get("wrappers");
        mdmGgId = ggInfo.getAsString("globalGraphID");
        client.close();
    }

    private void initLavMapping() {

        wrappers.forEach(wrapperId -> {
            MongoClient client = Utils.getMongoDBClient();
            MongoCursor<Document> wrapperCursor = MongoCollections.getWrappersCollection(client).
                    find(new Document("wrapperID", wrapperId)).iterator();
            createLavMappings(((JSONObject) JSONValue.parse(MongoCollections.getMongoObject(client, wrapperCursor))).getAsString("iri"));
            lavMapping = new JSONObject();
            lavMapping.put("wrapperID",wrapperId.toString());
            lavMapping.put("isModified", "false");

            lavMapping.put("globalGraphID", mdmGgId);
            lavMapping.put("sameAs", featureAndAttributes);
            System.out.println(lavMapping.toJSONString());

            // Call LAV Mapping Resource to save the LAV mapping info accordingly
            LAVMappingResource.createLAVMappingMapsTo(lavMapping.toJSONString());
            client.close();
        });

    }

    private void createLavMappings(String wrapperIri) {
        JSONArray wrapperAttributes = WrapperResource.getWrapperAttributes(wrapperIri);
        //System.out.println(wrapperAttributes.toJSONString());
        featureAndAttributes = new JSONArray();
        wrapperAttributes.forEach(attr -> {
            String attribute = getLastElementOfIRI(attr.toString());
            features.forEach((key, list) -> {
                if (list.isEmpty()) {
                    if (attribute.equals(getLastElementOfIRI(key))) {
                        //lavMappings.add(new Tuple2<>(attr.toString(), key));
                        JSONObject temp = new JSONObject();
                        temp.put("feature", key);
                        temp.put("attribute", attr.toString());
                        featureAndAttributes.add(temp);
                    }
                } else {
                    if (list.contains(attribute)) {
                        //lavMappings.add(new Tuple2<>(attr.toString(), key));
                        JSONObject temp = new JSONObject();
                        temp.put("feature", key);
                        temp.put("attribute", attr.toString());
                        featureAndAttributes.add(temp);
                    }
                }
            });
        });
    }

    private String getLastElementOfIRI(String iri) {
        return iri.split("/")[iri.split("/").length - 1];
    }

}
