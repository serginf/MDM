package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.model.omq.QueryRewriting_DAG;
import eu.supersede.mdm.storage.model.omq.Walk;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.ProjectionSet_OLD;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;
import scala.Tuple3;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        String constants = "{(";

        JSONArray projectedFeatures =(JSONArray)objBody.get("projectedFeatures");
        for (int i = 0; i < projectedFeatures.size(); ++i) {
            select += "?v"+(i+1)+" ";
            values += "?v"+(i+1)+" ";
            constants += "<"+projectedFeatures.get(i)+"> ";
        }

        values = values.substring(0,values.length()-1)+")";
        constants = constants.substring(0,constants.length()-1)+")}";

        String pattern = "";
        for (Object selectionElement : ((JSONArray)objBody.get("selection"))) {
            JSONObject selectedElement = (JSONObject)selectionElement;
            if (selectedElement.containsKey("source")) {
                JSONObject source = (JSONObject)selectedElement.get("source");
                JSONObject target = (JSONObject)selectedElement.get("target");
                pattern += "<"+source.getAsString("iri") + "> <" + selectedElement.getAsString("name") + "> <" +
                        target.getAsString("iri") + "> .\n";
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

        String SPARQL = objBody.getAsString("sparql");
        //String namedGraph = objBody.getAsString("namedGraph");
        QueryRewriting_DAG qr = new QueryRewriting_DAG(SPARQL.replace("\n"," "));
        Set<Walk> walks = qr.rewrite();
        //System.out.println(walks);

        JSONObject out = new JSONObject();
        out.put("ra",RDFUtil.nn(walks.stream().map(w -> w.toString()).collect(Collectors.joining("\nU\n"))));

        HashMap<String,String> wrapperIriToID = Maps.newHashMap(); //used to map wrapper IRIs to IDs
        //Populate data here
        JSONArray wrappers = new JSONArray();
        walks.forEach(w -> {
            w.getOperators().forEach(op -> {
                if (op instanceof Wrapper) {
                    String wrapperID = MongoCollections.getWrappersCollection(Utils.getMongoDBClient()).find(
                            new Document("iri",((Wrapper)op).getWrapper())
                    ).first().getString("wrapperID");

                    wrapperIriToID.putIfAbsent(((Wrapper)op).getWrapper(),wrapperID);
                    wrappers.add(wrapperID);
                }
            });
        });

        //Convert to SQL
        StringBuilder SQL = new StringBuilder();
        walks.forEach(w -> {
            StringBuilder select = new StringBuilder("SELECT ");
            StringBuilder from = new StringBuilder(" FROM ");
            StringBuilder where = new StringBuilder(" WHERE ");
            w.getOperators().forEach(op -> {
                if (op instanceof ProjectionSet_OLD) {
                    ProjectionSet_OLD projection = (ProjectionSet_OLD)op;
                    projection.getProjectedAttributes().forEach(p -> select.append(RDFUtil.nn(p).split("/")[RDFUtil.nn(p).split("/").length-1]+","));
                }
                else if (op instanceof Wrapper) {
                    Wrapper wrapper = (Wrapper)op;
                    from.append(wrapperIriToID.get(wrapper.getWrapper())+",");
                }
                else if (op instanceof EquiJoin) {
                    EquiJoin equiJoin = (EquiJoin)op;
                    where.append(
                            RDFUtil.nn(equiJoin.getLeft_attribute()).split("/")[RDFUtil.nn(equiJoin.getLeft_attribute()).split("/").length-1]+
                            " = "+
                            RDFUtil.nn(equiJoin.getRight_attribute()).split("/")[RDFUtil.nn(equiJoin.getRight_attribute()).split("/").length-1]+
                            " AND ");
                }
            });
            SQL.append(select.substring(0,select.length()-1));
            SQL.append(from.substring(0,from.length()-1));
            if (!where.toString().equals(" WHERE ")) {
                SQL.append(where.substring(0, where.length() - " AND ".length()));
            }
            SQL.append(" UNION ");
        });
        String SQLstr = SQL.substring(0,SQL.length()-" UNION ".length())+";";

        out.put("wrappers",wrappers);
        out.put("sql",SQLstr);
        return Response.ok(out.toJSONString()).build();
    }

    @POST @Path("omq/fromSQLToData")
    @Consumes("text/plain")
    public Response POST_omq_fromSQLToData(String body) {
        System.out.println("[POST /omq/fromSQLToData/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        String SQL = objBody.getAsString("sql");
        List<String> features = ((JSONArray)objBody.get("features")).stream().map(f -> f.toString()).collect(Collectors.toList());

        MongoClient client = Utils.getMongoDBClient();
        // Structure with wrapper obj, wrapper ID and list of attributes
        List<Tuple3<Wrapper,String,List<String>>> wrappers = Lists.newArrayList();
        ((JSONArray)objBody.get("wrappers")).forEach(wID -> {
            String strWrapperID = (String)wID;
            Document wrapper = MongoCollections.getWrappersCollection(client).find(new Document("wrapperID",strWrapperID)).first();
            Document ds = MongoCollections.getDataSourcesCollection(client).find(new Document("dataSourceID", wrapper.getString("dataSourceID"))).first();
            List<String> attributes = Lists.newArrayList();
            ((List<Document>)wrapper.get("attributes")).forEach(a -> {
                attributes.add(a.getString("name"));
            });
            wrappers.add(new Tuple3<>(Wrapper.specializeWrapper(ds,wrapper.getString("query")),strWrapperID,attributes));
        });

        JSONArray data = new JSONArray();

        wrappers.forEach(w -> {
            SQLiteUtils.createTable(w._2(),w._3());
            try {
                w._1().populate(w._2(),w._3());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        SQLiteUtils.executeSelect(SQL,features).forEach(d -> data.add(d));

        client.close();
        JSONObject out = new JSONObject();
        out.put("data",data);
        return Response.ok(out.toJSONString()).build();
    }

}
