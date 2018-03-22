package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
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



}
