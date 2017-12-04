package eu.supersede.mdm.storage.resources;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.DispatcherStrategiesTypes;
import eu.supersede.mdm.storage.model.StatisticalAnalysisModelTypes;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bson.Document;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class DispatcherStrategiesResource {

    @Context
    ServletContext context;

    @GET
    @Path("dispatcher_strategies_types/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_dispatcherStrategies_Types() {
        System.out.println("[GET /dispatcher_strategies_types/]");

        JSONArray out = new JSONArray();
        for (DispatcherStrategiesTypes t : DispatcherStrategiesTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }

}
