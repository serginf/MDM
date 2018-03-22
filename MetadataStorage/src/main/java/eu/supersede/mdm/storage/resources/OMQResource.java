package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class OMQResource {

    @POST @Path("omq/fromGraphicalToSPARQL")
    @Consumes("text/plain")
    public Response POST_omq_fromGraphicalToSPARQL(String body) {
        System.out.println("[POST /omq/fromGraphicalToSPARQL/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        String select = "SELECT ";
        String values = "VALUES (";
        String constants = "{";

        JSONArray projectedFeatures =(JSONArray)objBody.get("projectedFeatures");
        for (int i = 0; i < projectedFeatures.size(); ++i) {
            select += "?v"+(i+1)+" ";
            values += "?v"+(i+1)+" ";
            constants += projectedFeatures.get(i)+" ";
        }

        values = values.substring(0,values.length()-1)+")";
        constants = constants.substring(0,constants.length()-1)+"}";

        String pattern = "";
        for (Object selectionElement : ((JSONArray)objBody.get("selection"))) {
            JSONObject selectedElement = (JSONObject)selectionElement;
            if (selectedElement.containsKey("source")) {
                JSONObject source = (JSONObject)selectedElement.get("source");
                JSONObject target = (JSONObject)selectedElement.get("target");
                pattern += source.getAsString("iri") + " " + selectedElement.getAsString("name") + " " +
                        target.getAsString("iri") + " .\n";
            }
        }
        pattern = pattern.substring(0,pattern.length()-2)+"\n";

        JSONObject out = new JSONObject();
        out.put("sparql",select+"\nWHERE {\n"+values+" "+constants+"\n"+pattern+"}");
        return Response.ok(out.toJSONString()).build();
    }


    @POST @Path("omq/fromSPARQLToRA")
    @Consumes("text/plain")
    public Response POST_omq_fromSPARQLToRA(String body) {
        System.out.println("[POST /omq/fromSPARQLToRA/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        JSONObject out = new JSONObject();
        out.put("ra","");
        return Response.ok(out.toJSONString()).build();
    }
}
