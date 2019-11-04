package eu.supersede.mdm.storage.db.mongo.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Method to parse all objects in a list to Json and save the result in a list and then parse as json.
     *
     * @param list
     * @return
     */
    public static String serializeListJsonAsString(List list){
        try {
            List<String> serializeJson = new ArrayList<>();
            for (Object element : list) {
                serializeJson.add(mapper.writeValueAsString(element));
            }
            return mapper.writeValueAsString(serializeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
            return "";
    }

}
