package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.mongodb.MongoClient;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import javax.servlet.ServletContext;
import java.io.File;

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
                return TDBFactory.createDataset(ConfigManager.getProperty("metadata_db_file")/*"BolsterMetadataStorage"*/ +
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
}
