package eu.supersede.mdm.storage.bdi.mdm.constructs;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.LAVMappingResource;
import eu.supersede.mdm.storage.resources.WrapperResource;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple3;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDMLavMapping {
    private String mdmGlobalGraphIri;
    private String mdmGgId;
    private JSONArray wrappers;
    /*Map<feature, List< Tuple3<localName, sourceName, IRI>, Tuple3<,,>,....*/
    private Map<String, List<Tuple3<String, String, String>>> features = new HashMap<>();
    //private List<Tuple2<String, String>> lavMappings = new ArrayList<>();
    private JSONObject lavMapping;
    private JSONArray featureAndAttributes;

    public MDMLavMapping(String mdmGlobalGraphIri) {
        this.mdmGlobalGraphIri = mdmGlobalGraphIri;
        run();
    }

    private void run() {
        getFeaturesWithSameAsEdges();
        System.out.println("FEATURES:");

        System.out.println(features);
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
            List<Tuple3<String, String, String>> sameAsfeatures = new ArrayList<>();

            if (triple.get("o") != null)
                sameAsfeatures.add(new Tuple3<>(getLastElementOfIRI(triple.get("o").toString()),
                        getSourceFromIRI(triple.get("o").toString()), triple.get("o").toString()));

            if (features.containsKey(featureName)) {
                //System.out.println("Printing value of the key : " + features.get(featureName));
                List<Tuple3<String, String, String>> tempList = features.get(featureName);
                if (triple.get("o") != null)
                    tempList.add(new Tuple3<>(getLastElementOfIRI(triple.get("o").toString()),
                            getSourceFromIRI(triple.get("o").toString()), triple.get("o").toString()));
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
            lavMapping.put("wrapperID", wrapperId.toString());
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
                    /*Check the type of the feature and the attribute*/

                    if (attribute.equals(getLastElementOfIRI(key)) && getSourceFromIRI(key).equals(getWrapperSourceFromIRI(attr.toString()))) {
                        //System.out.println("Key: " + key);
                        //lavMappings.add(new Tuple2<>(attr.toString(), key));
                        JSONObject temp = new JSONObject();
                        temp.put("feature", key);
                        temp.put("attribute", attr.toString());
                        featureAndAttributes.add(temp);
                    }
                } else {
                    /*For Schema IRI, source type can be checked*/
                    list.forEach(tuple -> {
                        if (tuple._1.equals(attribute) && tuple._2.equals(getWrapperSourceFromIRI(attr.toString()))) {
                            //System.out.println("Key: " + key + " LocalName: " + tuple._1  + " Source: " + tuple._2);
                            //lavMappings.add(new Tuple2<>(attr.toString(), key));
                            JSONObject temp = new JSONObject();
                            temp.put("feature", key);
                            temp.put("attribute", attr.toString());
                            featureAndAttributes.add(temp);
                        }
                    });

                }
            });
        });
    }

    private String getLastElementOfIRI(String iri) {
        return iri.split("/")[iri.split("/").length - 1];
    }

    private String getWrapperSourceFromIRI(String iri) {
        return iri.split("/")[iri.split("/").length - 2];
    }

    private String getSourceFromIRI(String iri) {
        // If the global IRI is of a source, e.g. http://www.BDIOntology.com/schema/Bicycles/Bicycle_Manufacturer then
        // the word after http://www.BDIOntology.com/schema/ is the name of the source. However, if the global IRI is from global
        // instances (created while aligning) e.g. http://www.BDIOntology.com/global/ermaElU0-QbtrOURF/Model, then we can not identify the source from this IRI,
        String source = "";
        if (iri.contains(Namespaces.G.val())) {
            source = "global";
        }

        if (iri.contains(Namespaces.Schema.val())) {
            /*Extract the source name from the IRI*/
            /*Source Name is Bicycle in this IRI http://www.BDIOntology.com/schema/Bicycles/Bicycle_Manufacturer */
            source = iri.split(Namespaces.Schema.val())[1].split("/")[0];
        }

        return source;
    }

}
