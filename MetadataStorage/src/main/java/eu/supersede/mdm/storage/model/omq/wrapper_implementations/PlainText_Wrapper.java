package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.nio.file.Files;
import java.io.File;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class PlainText_Wrapper extends Wrapper {

    private String path;

    public PlainText_Wrapper(String name) {
        super(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String preview(List<String> attributes) throws Exception {
        JSONArray data = new JSONArray();

        String jsonArray = Files.lines(new File(this.path).toPath()).collect(Collectors.joining());
        JSONArray resp = ((JSONArray) JSONValue.parse(jsonArray));
        for (int i = 0; i < resp.size() && i < 10; ++i) {
            JSONArray arr = new JSONArray();
            for (int j = 0; j < attributes.size(); ++j) {
                JSONObject datum = new JSONObject();
                datum.put("attribute",attributes.get(j));
                datum.put("value",((JSONObject)resp.get(i)).getAsString(attributes.get(j)));
                arr.add(datum);
            }
            data.add(arr);
        }

        JSONObject res = new JSONObject(); res.put("data",data);
        return res.toJSONString();
    }

    @Override
    public void populate(String table, List<String> attributes) throws Exception {
        JSONArray data = new JSONArray();

        String jsonArray = Files.lines(new File(this.path).toPath()).collect(Collectors.joining());
        JSONArray resp = ((JSONArray) JSONValue.parse(jsonArray));
        for (int i = 0; i < resp.size(); ++i) {
            JSONArray arr = new JSONArray();
            for (int j = 0; j < attributes.size(); ++j) {
                JSONObject datum = new JSONObject();
                datum.put("attribute",attributes.get(j));
                datum.put("value",((JSONObject)resp.get(i)).getAsString(attributes.get(j)));
                arr.add(datum);
            }
            data.add(arr);
        }
        SQLiteUtils.insertData(table,data);
    }
}
