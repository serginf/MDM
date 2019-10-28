package eu.supersede.mdm.storage.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import eu.supersede.mdm.storage.util.ConfigManager;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//connection manager
public class MongoConnection {

    private static MongoConnection instance = new MongoConnection();
    private static final Logger LOGGER = Logger.getLogger(MongoConnection.class.getName());


    private MongoClient mongo = null;
    private MongoDatabase database = null;

    private MongoConnection() {}

    public MongoClient getMongo() throws RuntimeException {
        if (mongo == null) {
            LOGGER.info("Starting Mongo Connection");

            // create codec registry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

//            .conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION))

            MongoClientOptions options = MongoClientOptions.builder()
                    .codecRegistry(pojoCodecRegistry)
                    .writeConcern(WriteConcern.ACKNOWLEDGED) //check!! // To be able to wait for confirmation after writing on the DB
                    .connectionsPerHost(4)
                    .maxConnectionIdleTime((60 * 1_000))
                    .maxConnectionLifeTime((120 * 1_000))
                    .build();

            LOGGER.info("About to connect to MongoDB @ " + ConfigManager.getProperty("system_metadata_db_server"));
            try {
                mongo = new MongoClient(ConfigManager.getProperty("system_metadata_db_server"),options);
            } catch (MongoException ex) {
                LOGGER.severe("An error occurred when connecting to MongoDB\n"+ ex);
            } catch (Exception ex) {
                LOGGER.severe("An error occurred when connecting to MongoDB\n"+ex);
            }
        }

        return mongo;
    }

    public MongoDatabase getDatabase() {
        if (database == null) {
            LOGGER.info("Retrieving databaset: "+ConfigManager.getProperty("system_metadata_db_name")); //should be log kind of debug
            database = mongo.getDatabase(ConfigManager.getProperty("system_metadata_db_name"));
        }
        return database;
    }

    public void init() {
        getMongo();
        getDatabase();
    }

    public void close() {
        LOGGER.info("Closing MongoDB connection");
        if (mongo != null) {
            try {
                mongo.close();
                mongo = null;
                database = null;
            } catch (Exception e) {
                LOGGER.severe("An error occurred when closing the MongoDB connection\n" +e.getMessage());
            }
        } else {
            LOGGER.info("mongo object was null, wouldn't close connection");
        }
    }

    public static MongoConnection getInstance() {
        return instance;
    }

}
