package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import javax.servlet.ServletContext;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(ConfigManager.getProperty("system_metadata_db_server"));
    }

    public static Dataset getTDBDataset() {
        System.out.println("obtaining tdb dataset "+ConfigManager.getProperty("metadata_db_file")/*"BolsterMetadataStorage"*/ +
                ConfigManager.getProperty("metadata_db_name")/*"BolsterMetadataStorage"*/);
        try {
            return TDBFactory.createDataset(ConfigManager.getProperty("metadata_db_file")/*"BolsterMetadataStorage"*/ +
                    ConfigManager.getProperty("metadata_db_name")/*"BolsterMetadataStorage"*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("An error has occurred obtaining TDB dataset");
        return null;
    }
}
