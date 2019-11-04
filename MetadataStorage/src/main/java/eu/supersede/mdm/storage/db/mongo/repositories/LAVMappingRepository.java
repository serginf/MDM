package eu.supersede.mdm.storage.db.mongo.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.db.mongo.MongoConnection;
import eu.supersede.mdm.storage.db.mongo.models.LAVMappingModel;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class LAVMappingRepository {

    private final String FIELD_LAVMappingID = "LAVMappingID";
    private MongoCollection<LAVMappingModel> LAVCollection;

    @PostConstruct
    public void init() {
        LAVCollection = MongoConnection.getInstance().getInstance().getDatabase().getCollection("LAVMappings", LAVMappingModel.class);
    }

    public List<LAVMappingModel> findAll(){
        List<LAVMappingModel> LAVMappings = new ArrayList();
        MongoCursor cur = LAVCollection.find().iterator();
        while(cur.hasNext()) {
            LAVMappings.add((LAVMappingModel)cur.next());
        }
        return LAVMappings;
    }

    public LAVMappingModel findByLAVMappingID(String LAVMappingIDStr){
        return LAVCollection.find(eq(FIELD_LAVMappingID,LAVMappingIDStr)).first();
    }


}
