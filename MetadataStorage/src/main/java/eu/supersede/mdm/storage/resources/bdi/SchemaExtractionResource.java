package eu.supersede.mdm.storage.resources.bdi;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.bdi.extraction.CsvSchemaExtractor;
import eu.supersede.mdm.storage.bdi.extraction.JsonSchemaExtractor;
import eu.supersede.mdm.storage.bdi.extraction.XmlSchemaExtractor;
import eu.supersede.mdm.storage.bdi.extraction.rdb.MySqlDB;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.bson.Document;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
//import java.util.logging.Logger;
import org.apache.log4j.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Kashif-Rabbani in June 2019
 */
@Path("metadataStorage")
public class SchemaExtractionResource {
    private static final Logger LOGGER = Logger.getLogger(SchemaExtractionResource.class.getName());

    @POST
    @Path("jsonSchema/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_JsonFileInfo(String body) {
        LOGGER.info("[POST /json] body = " + body);
        try {
            //Parsing body as JSON
            JSONObject objBody = (JSONObject) JSONValue.parse(body);

            LocalDateTime startDatetime = LocalDateTime.now();

            //Creating JsonSchemaExtractor Object
            JsonSchemaExtractor jsonSchemaExtractor = new JsonSchemaExtractor();
            //Initiating Extraction Process
            // This process will extract JSON schema from the file and convert it into RDFS Knowledge Graph.
            JSONObject res = jsonSchemaExtractor.initiateExtraction(
                    objBody.getAsString("filePath"),
                    objBody.getAsString("givenName").replaceAll(" ", "_"));


            LocalDateTime endDateTime = LocalDateTime.now();
            Duration duration = Duration.between(endDateTime, startDatetime);
            long diff = Math.abs(duration.toMillis());

            LOGGER.info("EXPERIMENTATION,JSON," + objBody.getAsString("filename") + "," +
                    SchemaIntegrationHelper.calculateFileSize(objBody.getAsString("filePath")) + "," +  diff);



            //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
            JSONObject vowlObj = Utils.oWl2vowl(JsonSchemaExtractor.getOutputFile());

            // Preparing the response to be sent back
            JSONObject resData = prepareResponse(JsonSchemaExtractor.getOutputFile(), JsonSchemaExtractor.getIRI(), objBody, vowlObj);

            // Adding the RDFS Schema in Jena TDB Triple Store using IRI
            addExtractedSchemaIntoTDBStore(JsonSchemaExtractor.getIRI(), JsonSchemaExtractor.getOutputFile());

            // Adding the response to MongoDB
            addDataSourceInfoAsMongoCollection(resData);

            return Response.ok(new Gson().toJson("JSON")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("csvSchema/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_CsvFileInfo(String body) {
        LOGGER.info("[POST /csv] body = " + body);
        try {
            //Parsing body as JSON
            JSONObject objBody = (JSONObject) JSONValue.parse(body);
            System.out.println(objBody.toJSONString());
            //Creating CsvSchemaExtractor Object
            CsvSchemaExtractor csvSchemaExtractor = new CsvSchemaExtractor();

            LocalDateTime startDatetime = LocalDateTime.now();

            boolean validity = csvSchemaExtractor.initCsvSchemaExtractor(objBody.getAsString("filePath"), objBody.getAsString("givenName").replaceAll(" ", "_"));
            if (validity) {
                LocalDateTime endDateTime = LocalDateTime.now();
                Duration duration = Duration.between(endDateTime, startDatetime);
                long diff = Math.abs(duration.toMillis());

                //EXPERIMENTATION,CSV,fileName.csv,11.8544921875,1314
                LOGGER.info("EXPERIMENTATION,CSV," + objBody.getAsString("filename") + "," +
                        SchemaIntegrationHelper.calculateFileSize(objBody.getAsString("filePath")) + "," +  diff);

                //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
                JSONObject vowlObj = Utils.oWl2vowl(csvSchemaExtractor.getCsvModelOutputFilePath());

                // Preparing the response to be sent back
                JSONObject resData = prepareResponse(csvSchemaExtractor.getCsvModelOutputFilePath(), csvSchemaExtractor.getCsvModelIRI(), objBody, vowlObj);

                // Adding the RDFS Schema in Jena TDB Triple Store using IRI
                addExtractedSchemaIntoTDBStore(csvSchemaExtractor.getCsvModelIRI(), csvSchemaExtractor.getCsvModelOutputFilePath());

                // Adding the response to MongoDB
                addDataSourceInfoAsMongoCollection(resData);

                return Response.ok(new Gson().toJson("CSV")).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("xml/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_XmlFileInfo(String body) {
        try {
            LOGGER.info("[POST /xml] body = " + body);
            //Parsing body as JSON
            JSONObject objBody = (JSONObject) JSONValue.parse(body);

            //Creating XmlSchemaExtractor Object
            XmlSchemaExtractor xmlSchemaExtractor = new XmlSchemaExtractor();
            LocalDateTime startDatetime = LocalDateTime.now();
            //Initiating Extraction Process
            // This process will extract XML schema from the file and convert it into RDFS Knowledge Graph.
            JSONObject res = xmlSchemaExtractor.initiateXmlExtraction(
                    objBody.getAsString("filePath"),
                    objBody.getAsString("givenName").replaceAll(" ", "_"));

            LocalDateTime endDateTime = LocalDateTime.now();
            Duration duration = Duration.between(endDateTime, startDatetime);
            long diff = Math.abs(duration.toMillis());

            LOGGER.info("EXPERIMENTATION,XML," + objBody.getAsString("filename") + "," +
                    SchemaIntegrationHelper.calculateFileSize(objBody.getAsString("filePath")) + "," +  diff);

            //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
            JSONObject vowlObj = Utils.oWl2vowl(xmlSchemaExtractor.getOutputFilePath());

            // Preparing the response to be sent back
            JSONObject resData = prepareResponse(xmlSchemaExtractor.getOutputFilePath(), xmlSchemaExtractor.getIRI(), objBody, vowlObj);

            // Adding the RDFS Schema in Jena TDB Triple Store using IRI
            addExtractedSchemaIntoTDBStore(xmlSchemaExtractor.getIRI(), xmlSchemaExtractor.getOutputFilePath());

            // Adding the response to MongoDB
            addDataSourceInfoAsMongoCollection(resData);
            return Response.ok(new Gson().toJson("XML")).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }


    }


    @POST
    @Path("sql/")
    @Consumes("text/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_SqlConnectionInfo(String body) {
        LOGGER.info("[POST /sql] body = " + body);
        //Parsing body as JSON
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        String path = objBody.getAsString("filePath");
        String databaseType = "jdbc:mysql";   // It can be changed and adjusted/configured if MS-SQL database is plugged in
        //Creating XmlSchemaExtractor Object
        MySqlDB mySqlDB = new MySqlDB(
                path.split(",")[0],
                path.split(",")[1],
                path.split(",")[2],
                path.split(",")[3],
                databaseType);

        //Convert RDFS to VOWL (Visualization Framework) Compatible JSON
        JSONObject vowlObj = Utils.oWl2vowl(mySqlDB.getRelationalOutputFilePath());

        // Preparing the response to be sent back
        JSONObject resData = prepareResponse(mySqlDB.getRelationalOutputFilePath(), mySqlDB.getRelationalIRI(), objBody, vowlObj);

        // Adding the RDFS Schema in Jena TDB Triple Store using IRI
        addExtractedSchemaIntoTDBStore(mySqlDB.getRelationalIRI(), mySqlDB.getRelationalOutputFilePath());

        // Adding the response to MongoDB
        addDataSourceInfoAsMongoCollection(resData);

        return Response.ok(new Gson().toJson("SQL")).build();
    }

    private JSONObject prepareResponse(String fileName, String IRI, JSONObject objBody, JSONObject vowlObj) {
        JSONObject resData = new JSONObject();
        resData.put("name", objBody.getAsString("givenName"));
        resData.put("type", objBody.getAsString("type"));
        resData.put("sourceFileAddress", objBody.getAsString("filePath"));
        resData.put("parsedFileAddress", fileName);
        resData.put("dataSourceID", RandomStringUtils.randomAlphanumeric(8).replace("-", ""));
        resData.put("iri", SourceGraph.DATA_SOURCE.val() + "/" + objBody.getAsString("givenName"));
        //resData.put("iri", IRI);
        resData.put("bootstrappingType", "auto");
        resData.put("schema_iri", IRI);
        resData.put("graphicalGraph", "\" " + StringEscapeUtils.escapeJava(vowlObj.getAsString("vowlJson")) + "\"");
        //resData.put("vowlJsonFileName", vowlObj.getAsString("vowlJsonFileName"));

        if (objBody.getAsString("type").equals("json")) {
            resData.put("json_path", objBody.getAsString("filePath"));
        }

        if (objBody.getAsString("type").equals("xml")) {
            resData.put("xml_path", objBody.getAsString("filePath"));
        }

        if (objBody.getAsString("type").equals("SQL")) {
            resData.put("sql_path", objBody.getAsString("filePath"));
        }

        if (objBody.getAsString("type").equals("csv")) {
            resData.put("csv_path", objBody.getAsString("filePath"));
        }
        return resData;
    }

    private void addDataSourceInfoAsMongoCollection(JSONObject objBody) {
        MongoClient client = Utils.getMongoDBClient();
        MongoCollections.getDataSourcesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        client.close();
    }

    private void addExtractedSchemaIntoTDBStore(String iri, String outputFilePath) {
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(iri);
        //OntModel ontModel = ModelFactory.createOntologyModel();
        model.read(outputFilePath);
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
    }
}