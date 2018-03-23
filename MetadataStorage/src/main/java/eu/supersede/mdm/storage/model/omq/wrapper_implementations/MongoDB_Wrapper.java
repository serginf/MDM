package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDB_Wrapper extends Wrapper {

    private String connectionString;
    private String database;
    private String mongodbQuery;

    public MongoDB_Wrapper(String name) {
        super(name);
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getMongodbQuery() {
        return mongodbQuery;
    }

    public void setMongodbQuery(String mongodbQuery) {
        this.mongodbQuery = mongodbQuery;
    }

    @Override
    public String preview() throws Exception {
        MongoClient mongoClient = new MongoClient(/*this.connectionString*/"localhost");
        MongoDatabase db = mongoClient.getDatabase(this.database);

        Bson command = new Document("eval",this.mongodbQuery);

        Document out = db.runCommand(command);


        JSONArray data = new JSONArray();

        JSONObject res = new JSONObject(); res.put("data",data);
        return res.toJSONString();
    }

}
