package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import scala.Tuple3;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class AdminResource {

    /** System Metadata **/
    @GET @Path("admin/deleteAll")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_admin_delete_all() {
        System.out.println("[GET /admin/deleteAll/");
        MongoClient client = Utils.getMongoDBClient();
        client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).drop();

        try {
            FileUtils.deleteDirectory(new File(ConfigManager.getProperty("metadata_db_file")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok("OK").build();
    }

    @GET @Path("admin/demoPrepare")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_admin_demoprepare() {
        System.out.println("[GET /admin/demoPrepare/");
        MongoClient client = Utils.getMongoDBClient();
        Bson command = new Document("eval","db.copyDatabase(\"MDM_MetadataStorage_WISCENTD\",\"MDM_MetadataStorage\",\"localhost\")");
        client.getDatabase("MDM_MetadataStorage_WISCENTD").runCommand(command);

        try {
            FileUtils.copyDirectory(new File("/home/snadal/UPC/Projects/MDM/WISCENTD_bkp/MDM_TDB"),
                    new File("/home/snadal/UPC/Projects/MDM/MetadataStorage/MDM_TDB"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok("OK").build();
    }

}