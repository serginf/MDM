package eu.supersede.mdm.storage.db.mongo.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.db.mongo.MongoConnection;
import eu.supersede.mdm.storage.db.mongo.models.WrapperModel;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class WrapperRepository {

    private final String FIELD_WrapperID = "wrapperID";
    private MongoCollection<WrapperModel> wrapperCollection;


    @PostConstruct
    public void init() {
        wrapperCollection = MongoConnection.getInstance().getInstance().getDatabase().getCollection("wrappers", WrapperModel.class);
    }

    public List<WrapperModel> findAll(){
        List<WrapperModel> wrappers = new ArrayList();
        MongoCursor cur = wrapperCollection.find().iterator();
        while(cur.hasNext()) {
            wrappers.add((WrapperModel)cur.next());
        }
        return wrappers;
    }

    public WrapperModel findByWrapperID(String wrapperID){
        return wrapperCollection.find(eq(FIELD_WrapperID,wrapperID)).first();
    }

    public void create(String wrapperStr){
        try {
            WrapperModel wrapper = UtilsMongo.mapper.readValue(wrapperStr, WrapperModel.class);
            wrapperCollection.insertOne(wrapper);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (com.mongodb.MongoWriteException ex){
            //TODO: (Javier) Handle error when not able to write in db and check other exception throw by insertOne
        }
    }


}
