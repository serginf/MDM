package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.apache.spark.sql.SparkSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(ConfigManager.getProperty("system_metadata_db_server"));
    }

    public static SparkSession getSparkSession(){
        return SparkSession.builder()
                .appName("parquetPreview")
                .master("local[*]")
                .config("spark.driver.bindAddress","localhost").getOrCreate();
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
     * @return JSONObject of containing one element i.e. vowlJson
     */
    public static JSONObject oWl2vowl(String rdfsFilePath) {
        JSONObject vowlResponse = oWl2vowl(rdfsFilePath, ConfigManager.getProperty("owl2vowl_service_output_path"));
        return vowlResponse;
    }

    public static JSONObject oWl2vowl(String rdfsFilePath, String vowl_output_path) {
        JSONObject vowlData = new JSONObject();
        String var3 = "";

        try {
            File temp = new File(rdfsFilePath);
            String vowlFileName = temp.getName().replaceAll(".ttl", "-vowl.json");
            Owl2Vowl owl2Vowl = new Owl2Vowl(new FileInputStream(rdfsFilePath));
            vowlData.put("vowlJson", owl2Vowl.getJsonAsString());
        } catch (FileNotFoundException var7) {
            var7.printStackTrace();
        }

        return vowlData;
    }
}
