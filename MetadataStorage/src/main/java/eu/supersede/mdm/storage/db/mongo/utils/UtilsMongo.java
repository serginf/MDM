package eu.supersede.mdm.storage.db.mongo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class UtilsMongo {

    public final static ObjectMapper mapper = new ObjectMapper();


    public static String ToJsonString(Object obj){
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
