package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.model.omq.wrapper_implementations.*;
import eu.supersede.mdm.storage.util.ConfigManager;
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
import java.util.stream.Collectors;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class WrapperResource {

    @GET
    @Path("wrapper/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_wrapper() {
        System.out.println("[GET /wrapper/]");

        MongoClient client = Utils.getMongoDBClient();
        List<String> wrappers = Lists.newArrayList();
        MongoCollections.getWrappersCollection(client).find().iterator().forEachRemaining(document -> wrappers.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(wrappers)).build();
    }

    @GET
    @Path("wrapper/{wrapperID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_wrapperByID(@PathParam("wrapperID") String wrapperID) {
        System.out.println("[GET /wrapper/] wrapperID = " + wrapperID);

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("wrapperID", wrapperID);
        Document res = MongoCollections.getWrappersCollection(client).find(query).first();
        client.close();
        return Response.ok((res.toJson())).build();
    }

    @POST
    @Path("wrapper/")
    @Consumes("text/plain")
    public Response POST_wrapper(String body) {
        System.out.println("[POST /wrapper/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();
        //Metadata for the wrapper
        objBody.put("wrapperID", "w"+UUID.randomUUID().toString().replace("-",""));
        String wrapperName = objBody.getAsString("name")/*.trim().replace(" ","")*/;
        String wIRI = SourceGraph.WRAPPER.val()+"/"+wrapperName;
        objBody.put("iri",wIRI);

        MongoCollections.getWrappersCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        //Update the data source with the new wrapper
        MongoCollections.getDataSourcesCollection(client).findOneAndUpdate(
                new Document().append("dataSourceID",objBody.getAsString("dataSourceID")),
                new Document().append("$push", new Document().append("wrappers",objBody.getAsString("wrapperID")))
        );

        //RDF - we use as named graph THE SAME as the data source
        String dsIRI = MongoCollections.getDataSourcesCollection(client).
                find(new Document().append("dataSourceID",objBody.getAsString("dataSourceID"))).first()
                .getString("iri");

        RDFUtil.addTriple(dsIRI, wIRI, Namespaces.rdf.val() + "type", SourceGraph.WRAPPER.val());
        RDFUtil.addTriple(dsIRI, dsIRI, SourceGraph.HAS_WRAPPER.val(), wIRI);
        ((JSONArray) objBody.get("attributes")).forEach(attribute -> {
            String attName = ((JSONObject) attribute).getAsString("name");
            //String attIRI = dsIRI + "/" + wrapperName + "." + attName/*.trim().replace(" ", "")*/;
            String attIRI = dsIRI + "/" + attName/*.trim().replace(" ", "")*/;
            RDFUtil.addTriple(dsIRI, attIRI, Namespaces.rdf.val() + "type", SourceGraph.ATTRIBUTE.val());
            RDFUtil.addTriple(dsIRI, wIRI, SourceGraph.HAS_ATTRIBUTE.val(), attIRI);
                //if (Boolean.parseBoolean(((JSONObject)attribute).getAsString("isID"))) {
                //    RDFUtil.addTriple(S,attIRI,Namespaces.rdfs.val()+"subClassOf",Namespaces.sc.val()+"identifier");
                //}
        });

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @POST
    @Path("wrapper/inferSchema/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_inferSchema(String body) throws Exception {
        System.out.println("[POST /inferSchema/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        String query = objBody.getAsString("query");
        String dataSourceID = objBody.getAsString("dataSourceID");

        MongoClient client = Utils.getMongoDBClient();
        Document ds = MongoCollections.getDataSourcesCollection(client).find(new Document("dataSourceID", dataSourceID)).first();
        Wrapper w = Wrapper.specializeWrapper(ds,query); //Body sent to get extra parameters
        client.close();
        return Response.ok((w.inferSchema())).build();
    }

    @POST
    @Path("wrapper/preview/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_preview(String body) throws Exception {
        System.out.println("[POST /preview/] body = " + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        String dataSourceID = objBody.getAsString("dataSourceID");
        String query = objBody.getAsString("query");
        List<String> attributes = ((JSONArray)JSONValue.parse(objBody.getAsString("attributes")))
                .stream().map(a -> (String)a).collect(Collectors.toList());

        MongoClient client = Utils.getMongoDBClient();
        Document ds = MongoCollections.getDataSourcesCollection(client).find(new Document("dataSourceID", dataSourceID)).first();
        Wrapper w = Wrapper.specializeWrapper(ds,query);
        client.close();
        return Response.ok((w.preview(attributes))).build();
    }

    @GET
    @Path("wrapper/{iri}/attributes")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_attributesForWrapper(@PathParam("iri") String iri) {
        System.out.println("[GET /wrapper/attributes/] iri = "+iri);
        JSONArray attributes = new JSONArray();
        String SPARQL = "SELECT ?a WHERE { GRAPH ?g { <"+iri+"> <"+SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }";
        RDFUtil.runAQuery(SPARQL,iri).forEachRemaining(t -> {
            attributes.add(t.get("a").asNode().getURI());
        });
        return Response.ok(attributes.toJSONString()).build();
    }

}