package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.manager.Manager;
import eu.supersede.mdm.storage.cep.sm4cep.Sm4cepParser;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.ActionTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.OperatorTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.BDIOntologyGenerationStrategies;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.Strategy_CopyFromSources;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.Rules;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.sys.Names;
import org.apache.jena.util.FileManager;
import org.bson.Document;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class ECARuleResource {

    private MongoCollection<Document> getEcaRulesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("eca_rules");
    }

    @GET
    @Path("eca_rule/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_all_ECA_rules() {
        System.out.println("[GET /eca_rule/]");

        MongoClient client = Utils.getMongoDBClient();
        JSONArray arr = new JSONArray();
        getEcaRulesCollection(client).find().iterator().forEachRemaining(document -> arr.add(document));
        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
    }

    @GET
    @Path("eca_rule/{eca_ruleID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_eca_rule(@PathParam("eca_ruleID") String eca_ruleID) {
        System.out.println("[GET /eca_rule/"+eca_ruleID+"]");

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("eca_ruleID",eca_ruleID);
        Document res = getEcaRulesCollection(client).find(query).first();
        client.close();

        return Response.ok((res.toJson())).build();
    }

    /**
     * POST an ECA RULE
     */
    @POST @Path("eca_rule/")
    @Consumes("text/plain")
    public Response POST_ECA_rule(String body) throws FileNotFoundException {
        System.out.println("[POST /eca_rule/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        // Store in MongoDB
        objBody.put("eca_ruleID", UUID.randomUUID().toString());
        getEcaRulesCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        // Store in RDF
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(objBody.getAsString("graph"));

        // RULE
        System.out.println("# Rule definition");
        String RULE_IRI = Namespaces.ex.val()+objBody.getAsString("ruleName");
        RDFUtil.addTriple(model, RULE_IRI, Namespaces.rdf.val()+"type", Rules.RULE.val());
        System.out.println();

        // WINDOW
        System.out.println("# Window definition");
        String WINDOW_SIZE_IRI = RULE_IRI+"/"+objBody.getAsString("windowSize");
        RDFUtil.addTriple(model, WINDOW_SIZE_IRI, Namespaces.rdf.val()+"type", Rules.WINDOW.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_WINDOW.val(), WINDOW_SIZE_IRI);
        System.out.println();

        // EVENT SCHEMA
        System.out.println("# Event schema definition");
        JSONArray arrayEvents = (JSONArray) objBody.get("filters");
        String EVENT_IRI = RULE_IRI+"/Event/";
        for (Object e : arrayEvents) {
            JSONObject obj = (JSONObject) e;
            String name = EVENT_IRI + obj.getAsString("event") + "Schema";
            RDFUtil.addTriple(model, name, Namespaces.rdf.val() + "type", Rules.EVENT_SCHEMA.val());
            RDFUtil.addTriple(model, name, Rules.HAS_EVENT_ATTRIBUTE.val(), obj.getAsString("leftOperand"));
            System.out.println();
        }

        // EVENT ATTRIBUTES
        System.out.println("# Event Attribute definitions");
        for (Object e : arrayEvents) {
            JSONObject obj = (JSONObject) e;
            RDFUtil.addTriple(model, obj.getAsString("leftOperand"), Namespaces.rdf.val() + "type", Rules.EVENT_ATTRIBUTE.val());
        }
        System.out.println();

        // EVENTS
        System.out.println("# Event definitions");
        for (Object ev : arrayEvents) {
            JSONObject obj = (JSONObject) ev;
            RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.getAsString("event"), Namespaces.rdf.val() + "type", Rules.EVENT.val());
            RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.getAsString("event"), Rules.HAS_EVENT_SCHEMA.val(), EVENT_IRI+"/"+obj.getAsString("event")+"Schema");
            System.out.println();
        }


        // PATTERN RELEASES
        System.out.println("# Pattern definition");
        String PATTERN_IRI = RULE_IRI+"/Pattern";
        RDFUtil.addTriple(model, PATTERN_IRI, Namespaces.rdf.val()+"type", Rules.PATTERN.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CEP_ELEMENT.val(), PATTERN_IRI);
        System.out.println();
        JSONArray arrayPattern = (JSONArray) objBody.get("pattern");
        int patternElementOrder = 1;
        for (Object obj : arrayPattern) {
          //  RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Namespaces.rdf.val()+"type", Rules.INCLUDED_ELEMENT.val());
           // RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.toString(), Namespaces.rdf.val()+"type", Rules.EVENT.val());
            //   RDFUtil.addTriple(model, Integer.toString(patternElementOrder), Namespaces.rdf.val()+"type", Rules.POSITIVE_INTEGER.val());
            RDFUtil.addTriple(model, PATTERN_IRI, Rules.CONTAINS_ELEMENT.val(), PATTERN_IRI+"/"+obj.toString());
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Rules.HAS_ELEMENT_ORDER.val(), Integer.toString(patternElementOrder));
            JSONArray ev = (JSONArray) objBody.get("filters");
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Rules.REPRESENTS_ELEMENT.val(),EVENT_IRI+"/"+obj.toString());
            System.out.println();
            ++patternElementOrder;
        }

        // CONDITIONS
        System.out.println("# Condition definitions");
        String CONDITION_IRI = RULE_IRI+"/"+objBody.getAsString("condition");
        RDFUtil.addTriple(model, CONDITION_IRI, Namespaces.rdf.val()+"type", Rules.CONDITION.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CONDITION.val(), CONDITION_IRI);
        System.out.println();

        // ACTION
        System.out.println("# Action definition");
        JSONObject action = (JSONObject) objBody.get("action");
        String ACTION_IRI = RULE_IRI+"/"+action.getAsString("name");
        RDFUtil.addTriple(model, ACTION_IRI, Namespaces.rdf.val()+"type", Rules.ACTION.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_ACTION.val(), ACTION_IRI);
        System.out.println();
        JSONArray parameters = (JSONArray) action.get("parameters");
        int parametersOrder = 1;
        for (Object p : parameters) {
            // Instantiate
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Namespaces.rdf.val()+"type", Rules.ACTION_PARAMETER.val());
            // Link
            RDFUtil.addTriple(model, ACTION_IRI, Rules.HAS_ACTION_PARAMETER.val(), ACTION_IRI+"/"+p.toString());
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Rules.HAS_ACTION_PARAMETER_ORDER.val(), Integer.toString(parametersOrder));

            ++parametersOrder;
        }
        System.out.println();

        // FILTERS
        System.out.println("# Filters definitions");
        JSONArray arrayFilters = (JSONArray) objBody.get("filters");
        for (Object f : arrayFilters) {
            JSONObject obj = (JSONObject) f;
            String FILTER_IRI = RULE_IRI+"/Filter/"+obj.getAsString("name");
            System.out.println("# "+obj.getAsString("name"));
            //  RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), "Filter"+filterNum);
            RDFUtil.addTriple(model, FILTER_IRI+"/", Namespaces.rdf.val()+"type", Rules.SIMPLE_CLAUSE.val());
            RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), FILTER_IRI);
            //RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Namespaces.rdf.val()+"type", Rules.USED_ATTRIBUTE.val());
            //  RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("event"), Namespaces.rdf.val()+"type", Rules.EVENT.val());

           // RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Rules.FOR_EVENT.val(), EVENT_IRI+"/"+obj.getAsString("event"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_LEFT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("leftOperand"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_COMPARISON_OPERATOR.val(), FILTER_IRI+"/"+obj.getAsString("comparator"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_RIGHT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("rightOperand"));
        }

        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @GET
    @Path("eca_rule_operator_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_operator_types() {
        System.out.println("[GET /eca_rule_operator_types/]");
        JSONArray out = new JSONArray();
        for (OperatorTypes t : OperatorTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }


    @GET
    @Path("eca_rule_predicate_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_predicate_types() {
        System.out.println("[GET /eca_rule_predicate_types/]");
        JSONArray out = new JSONArray();
        for (PredicatesTypes t : PredicatesTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }

    @GET
    @Path("eca_rule_action_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_action_types() {
        System.out.println("[GET /eca_rule_action_types/]");
        JSONArray out = new JSONArray();
        for (ActionTypes t : ActionTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }

    @GET
    @Path("eca_rule/{ruleName}/generate_config_file")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_config_file(@PathParam("ruleName") String ruleName) {
        System.out.println("[GET /eca_rule/{ruleName}/generate_config_file]");
        try {
            Sm4cepParser sm4cepparser = new Sm4cepParser();
            sm4cepparser.getAllEventSchemata();
            Rule r = sm4cepparser.getRule(ruleName);

            Manager m = new Manager();
            String s = m.CreateConfiguration("SergiAgent",Lists.newArrayList(sm4cepparser.getEventSchemata().values()), Lists.newArrayList(r),"localhost:9092","stream_type",false,"");

            File f = File.createTempFile(UUID.randomUUID().toString(), ".config"/*, new File("/home/alba/SUPERSEDE/tmpFiles/")*/);
            BufferedWriter bw = new BufferedWriter(new FileWriter(s));
            bw.write(ruleName);
            bw.close();
            System.out.println(f.getAbsolutePath());
            //return Response.ok(new Gson().toJson(f)).build();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Response.ok(new Gson().toJson(ruleName)).build();
    }

    /** Load metamodel sm4cep **/
    @GET @Path("eca_rule/load_sm4cep")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_load_sm4cep() {
        System.out.println("[GET /eca_rule/load_sm4cep/");
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel();
        model.add(FileManager.get().readModel(ontModel, "sm4cep_metamodel.ttl"));
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        return Response.ok("OK").build();
    }

}
