package eu.supersede.mdm.storage.db.mongo.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import eu.supersede.mdm.storage.db.mongo.MongoConnection;
import eu.supersede.mdm.storage.db.mongo.models.DataSourceModel;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class DataSourceRepository {

    private final String FIELD_DataSourceID = "dataSourceID";
    private final String FIELD_Wrappers = "wrappers";
    private MongoCollection<DataSourceModel> dataSourceCollection;

    @PostConstruct
    public void init() {
        dataSourceCollection = MongoConnection.getInstance().getInstance().getDatabase().getCollection("dataSources", DataSourceModel.class);
    }

    public List<DataSourceModel> findAll(){
        List<DataSourceModel> wrappers = new ArrayList();
        MongoCursor cur = dataSourceCollection.find().iterator();
        while(cur.hasNext()) {
            wrappers.add((DataSourceModel)cur.next());
        }
        return wrappers;
    }

    public DataSourceModel findByDataSourceID(String dataSourceID){
        return dataSourceCollection.find(eq(FIELD_DataSourceID,dataSourceID)).first();
    }

    public void create(String dataSourceStr){
        try {
            DataSourceModel dataSource = UtilsMongo.mapper.readValue(dataSourceStr, DataSourceModel.class);
            dataSourceCollection.insertOne(dataSource);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (com.mongodb.MongoWriteException ex){
            //TODO: (Javier) Handle error when not able to write in db and check other exception throw by insertOne
            //if not possible to create should throw error and not write in jena
        }
    }

    public void addWrapper(String dataSourceID,String wrapperID ){
        dataSourceCollection.updateOne(eq(FIELD_DataSourceID,dataSourceID), Updates.addToSet(FIELD_Wrappers,wrapperID) );
    }

}
