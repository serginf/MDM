package eu.supersede.mdm.storage.resources.bdi;

import eu.supersede.mdm.storage.bdi.alignment.AlignmentAlgorithm;
import eu.supersede.mdm.storage.bdi.alignment.Finder;
import eu.supersede.mdm.storage.bdi.alignment.GlobalVsLocal;
import eu.supersede.mdm.storage.bdi.alignment.LogMapMatcher;
import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;
/**
 * Created by Kashif-Rabbani in June 2019
 */
@Path("metadataStorage")
public class SchemaIntegrationResource {
    private static final Logger LOGGER = Logger.getLogger(SchemaIntegrationResource.class.getName());
    private final SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();

    @GET
    @Path("getSchemaAlignments/{dataSource1_id}/{dataSource2_id}")
    @Consumes("text/plain")
    public Response GET_IntegratedSchemaAlignment(@PathParam("dataSource1_id") String ds1, @PathParam("dataSource2_id") String ds2) {
        LOGGER.info("[GET /getSchemaAlignments" + "/" + ds1 + ds2);
        try {
            JSONObject dataSource1Info = new JSONObject();
            JSONObject dataSource2Info = new JSONObject();
            String dataSource1 = null;
            String dataSource2 = null;
            JSONArray alignmentsArray = new JSONArray();

            if (ds1.contains("INTEGRATED-") && ds2.contains("INTEGRATED-")) {
                LOGGER.info("------------------- GLOBAL-vs-GLOBAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds2);

            } else if (ds1.contains("INTEGRATED-")) {
                LOGGER.info("------------------- GLOBAL-vs-LOCAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(ds2);

                if (!dataSource1.isEmpty() && !dataSource2.isEmpty()) {
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
                    alignmentsArray = new GlobalVsLocal(dataSource1Info, dataSource2Info).runGlobalVsLocalIntegration();
                }

            } else {
                LOGGER.info("------------------- LOCAL-vs-LOCAL ------------------- ");
                dataSource1 = schemaIntegrationHelper.getDataSourceInfo(ds1);
                dataSource2 = schemaIntegrationHelper.getDataSourceInfo(ds2);

                if (!dataSource1.isEmpty() && !dataSource2.isEmpty()) {
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                    /* Create an IRI for alignments which will be produced by LogMap for the two sources. Note that this IRI is required to store the alignments in the TripleStore. */

                    String alignmentsIRI = Namespaces.Alignments.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");
                    // Calling LogMapMatcher class to extract, and save the alignments
                    LogMapMatcher logMapMatcher = new LogMapMatcher(
                            dataSource1Info.getAsString("parsedFileAddress"),
                            dataSource2Info.getAsString("parsedFileAddress"),
                            alignmentsIRI
                    );

                    Finder finder = new Finder(dataSource1Info.getAsString("schema_iri"), dataSource2Info.getAsString("schema_iri"));
                    JSONArray dataPropertiesSpecialAlignments = finder.getAlignmentsArray();
                    JSONArray tempAlignmentsArray = alignmentsArray;
                    RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + alignmentsIRI + "> {?s ?p ?o} }", alignmentsIRI).forEachRemaining(triple -> {
                        JSONObject alignments = new JSONObject();
                       schemaIntegrationHelper.populateResponseArray(tempAlignmentsArray, triple, alignments);
                    });
                    alignmentsArray = tempAlignmentsArray;
                    alignmentsArray.addAll(dataPropertiesSpecialAlignments);
                    System.out.println(alignmentsArray.toJSONString());

                }
            } // end else condition here
            String integratedIRI = schemaIntegrationHelper.integrateTDBDatasets(dataSource1Info, dataSource2Info);

            schemaIntegrationHelper.initAlignmentTables();
            return Response.ok((alignmentsArray).toJSONString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }



    @POST
    @Path("acceptAlignment/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_AcceptAlignment(String body) {
        LOGGER.info("[POST /acceptAlignment] body = " + body);
        try {
            // Alignments are stored in a triple store because it is more convenient to deal with them this way
            // p: Contains Alignment of Class/Property A , s: Contains Alignment of Class/Property B, and o: Contains the confidence value between both mappings
            JSONObject objBody = (JSONObject) JSONValue.parse(body);

            String integratedIRI = Namespaces.G.val() + objBody.getAsString("integrated_iri");
            Resource s = ResourceFactory.createResource(objBody.getAsString("s"));
            Resource p = ResourceFactory.createResource(objBody.getAsString("p"));
            //System.out.println(s.getLocalName() + " " + p.getLocalName() + " URI " + s.getURI());

            String query = " SELECT * WHERE { GRAPH <" + integratedIRI + "> { <" + objBody.getAsString("s") + "> rdf:type ?o ." + "<" + objBody.getAsString("p") + "> rdf:type ?oo .  } }";

            final String[] checkIfQueryContainsResult = new String[5];

            if (objBody.getAsString("ds1_id").contains("INTEGRATED-") && objBody.getAsString("ds2_id").contains("INTEGRATED-")) {
                //System.out.println("GLOBAL-vs-GLOBAL");
            } else if (objBody.getAsString("ds1_id").contains("INTEGRATED-")) {
                //System.out.println("GLOBAL-vs-LOCAL");
                schemaIntegrationHelper.processAlignment(objBody, integratedIRI, s, p, query, checkIfQueryContainsResult, "GLOBAL-vs-LOCAL");
            } else {
                schemaIntegrationHelper.processAlignment(objBody, integratedIRI, s, p, query, checkIfQueryContainsResult, "LOCAL-vs-LOCAL");
                //System.out.println("LOCAL-vs-LOCAL");
            }

            //System.out.println(checkIfQueryContainsResult[0] + " " + checkIfQueryContainsResult[1]);
            if (checkIfQueryContainsResult[0] != null && checkIfQueryContainsResult[1] != null) {
                LOGGER.info("Alignment Succeeded");
                return Response.ok(("AlignmentSucceeded")).build();
            } else {
                LOGGER.info("Alignment Failed");
                return Response.ok(("AlignmentFailed")).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("finishIntegration/")
    @Consumes("text/plain")
    public Response GET_finishIntegration(String body) {
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        LOGGER.info("[GET /finishIntegration" + "/" + objBody.getAsString("schema_iri"));
        String integratedIRI = Namespaces.G.val() + objBody.getAsString("schema_iri");

        JSONObject dataSource1Info = new JSONObject();
        JSONObject dataSource2Info = new JSONObject();
        try {
            objBody.put("integratedIRI", integratedIRI);
            //System.out.println(objBody.toJSONString()); {"schema_iri":"wfFEEDGx-FBFLAdRr","integrationType":"LOCAL-vs-LOCAL", "integratedIRI":"http:\/\/www.BDIOntology.com\/global\/wfFEEDGx-FBFLAdRr","ds2_id":"FBFLAdRr","ds1_id":"wfFEEDGx"}
            new AlignmentAlgorithm(objBody);
            String integratedModelFileName = schemaIntegrationHelper.writeToFile(objBody.getAsString("schema_iri"), integratedIRI);
            //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
            JSONObject vowlObj = Utils.oWl2vowl(ConfigManager.getProperty("output_path") + integratedModelFileName);
            if (objBody.getAsString("integrationType").equals("GLOBAL-vs-LOCAL")) {
                String dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(objBody.getAsString("ds1_id"));
                String dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
                //System.out.println("Global-vs-Local - About to UpdateInfo");
                schemaIntegrationHelper.updateInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }
            if (objBody.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                String dataSource1 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds1_id"));
                String dataSource2 = schemaIntegrationHelper.getDataSourceInfo(objBody.getAsString("ds2_id"));

                if (!dataSource1.isEmpty())
                    dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

                if (!dataSource2.isEmpty())
                    dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);

                schemaIntegrationHelper.addInfo(dataSource1Info, dataSource2Info, ConfigManager.getProperty("output_path") + integratedModelFileName, vowlObj);
            }

            return Response.ok(("Okay")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


}
