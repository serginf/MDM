package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

public class MongoCollections {

    public static MongoCollection<Document> getGlobalGraphCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("globalGraphs");
    }

    public static MongoCollection<Document> getWrappersCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("wrappers");
    }

    public static MongoCollection<Document> getDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("dataSources");
    }

    public static MongoCollection<Document> getLAVMappingCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("LAVMappings");
    }

    public static MongoCollection<Document> getIntegratedDataSourcesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("integratedDataSources");
    }

    public static String getMongoObject(MongoClient client, MongoCursor<Document> cursor) {
        boolean itIs = true;
        String out = "";
        if (!cursor.hasNext()) itIs = false;
        else out = cursor.next().toJson();
        client.close();

        if (itIs) {
            System.out.println(out);
        } else {
            System.out.println("Not Found");
        }
        return out;
    }
}
