package eu.supersede.mdm.storage.parsers;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.SourceLevel;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.jena.ontology.OntModel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Created by snadal on 10/11/16.
 */
public class JSON_to_SourceLevel {

    private static String getProperClass(JSONObject jsonDataset, String key) {
        if (jsonDataset.get(key) == null) return SourceLevel.ATTRIBUTE.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONObject.class.getName())) return SourceLevel.EMBEDDED_OBJECT.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONArray.class.getName())) return SourceLevel.ARRAY.val();
        return SourceLevel.ATTRIBUTE.val();
    }

    private static String getProperLink(JSONObject jsonDataset, String key) {
        if (jsonDataset.get(key) == null) return SourceLevel.HAS_ATTRIBUTE.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONObject.class.getName())) return SourceLevel.HAS_EMBEDDED_OBJECT.val();
        if (jsonDataset.get(key).getClass().getName().equals(JSONArray.class.getName())) return SourceLevel.HAS_ARRAY.val();
        return SourceLevel.HAS_ATTRIBUTE.val();
    }

    public static void extractRec(OntModel theModel, JSONObject jsonDataset, String parentElement) {
        jsonDataset.forEach((k,v) -> {
            RDFUtil.addTriple(theModel,parentElement+"/"+k, Namespaces.rdf.val()+"type",getProperClass(jsonDataset,k));
            RDFUtil.addTriple(theModel,parentElement,getProperLink(jsonDataset,k),parentElement+"/"+k);
            if (jsonDataset.get(k) != null && jsonDataset.get(k).getClass().getName().equals(JSONObject.class.getName()))
                extractRec(theModel,(JSONObject) jsonDataset.get(k),parentElement+"/"+k);
            else if (jsonDataset.get(k) != null && jsonDataset.get(k).getClass().getName().equals(JSONArray.class.getName()))
                // TODO: Fix the case when is an array of Strings
                extractRec(theModel,(JSONObject) ((JSONArray) jsonDataset.get(k)).get(0),parentElement+"/"+k);
        });
    }

}
