package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import java.io.File;
import java.io.InputStream;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(ConfigManager.getProperty("system_metadata_db_server"));
    }

    public static Dataset copyOfTheDataset = null;
    public static Dataset getTDBDataset() {
        if (copyOfTheDataset == null) {
            try {
                return TDBFactory.createDataset(ConfigManager.getProperty("metadata_db_path") + "/"/*"BolsterMetadataStorage"*/ +
                        ConfigManager.getProperty("metadata_db_name")/*"BolsterMetadataStorage"*/);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("An error has occurred obtaining TDB dataset");

            }
        }
        return copyOfTheDataset;
//        return null;
    }

    public static SQLiteConnection getSQLiteConnection() {
        SQLiteConnection conn = new SQLiteConnection(new File(ConfigManager.getProperty("sqlite_db")));
        try {
            conn.open(true);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static InputStream getResourceAsStream(String filename) {
        InputStream in = Utils.class.getClassLoader().getResourceAsStream(filename);
        return in;
    }

    /**
     * You can access OWL2VOWL Service at owl2vowl_service_url
     *  Request type: POST
     *  Parameters of post request: 'rdfsFilePath' and 'vowlJsonFileOutputPath' as JSONObject elements.
     * @param rdfsFilePath
     * @return JSONObject of containing two elements 'vowlJsonFileName' and 'vowlJsonFilePath'
     */
    public static JSONObject oWl2vowl(String rdfsFilePath) {
        JSONObject vowlResponse = new JSONObject();
        Client client = Client.create();
        WebResource webResource = client
                .resource(ConfigManager.getProperty("owl2vowl_service_url"));

        JSONObject postData = new JSONObject();

        postData.put("rdfsFilePath", rdfsFilePath);
        postData.put("vowlJsonFileOutputPath", ConfigManager.getProperty("owl2vowl_service_output_path"));

        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, postData.toJSONString());

        if (response.getStatus() == 200) {
            String output = response.getEntity(String.class);
            JSONParser parser = new JSONParser();
            try {
                vowlResponse = (JSONObject) parser.parse(output);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        return vowlResponse;
    }
}
