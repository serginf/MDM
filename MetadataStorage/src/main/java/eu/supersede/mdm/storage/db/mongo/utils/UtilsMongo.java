package eu.supersede.mdm.storage.db.mongo.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

public class UtilsMongo {

    public final static ObjectMapper mapper = new ObjectMapper();


    public static String ToJsonString(Object obj){
        try {
            //Mapping List will result in '[{...' and we expect '["{....' so need to do this since frontend expects a non-json array of json strings
            if (obj instanceof List) {
                String json = "[";
                for (Object element : ((List) obj)) {
                    json+=mapper.writeValueAsString(element);
                }
                json += "]";
                return json;
            }
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
