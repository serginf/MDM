package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JSON_Wrapper extends Wrapper {

    private String path;
    private List<String> explodeLevels;
    private String arrayOfValues;
    private String attributeForSchema;
    private String valueForAttribute;
    private String copyToParent;

    public JSON_Wrapper(String name) {
        super(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getExplodeLevels() {
        return explodeLevels;
    }

    public void setExplodeLevels(List<String> explodeLevels) {
        this.explodeLevels = explodeLevels;
    }

    public String getArrayOfValues() {
        return arrayOfValues;
    }

    public void setArrayOfValues(String arrayOfValues) {
        this.arrayOfValues = arrayOfValues;
    }

    public String getAttributeForSchema() {
        return attributeForSchema;
    }

    public void setAttributeForSchema(String attributeForSchema) {
        this.attributeForSchema = attributeForSchema;
    }

    public String getValueForAttribute() {
        return valueForAttribute;
    }

    public void setValueForAttribute(String valueForAttribute) {
        this.valueForAttribute = valueForAttribute;
    }

    public String getCopyToParent() {
        return copyToParent;
    }

    public void setCopyToParent(String copyToParent) {
        this.copyToParent = copyToParent;
    }

    public static void extractAttributes(Set<String> attributes, String parent, JSONObject jsonSchema) {
        if (jsonSchema.get("type") instanceof String && jsonSchema.get("type").equals("struct") ||
                jsonSchema.get("type") instanceof JSONObject && ((JSONObject) jsonSchema.get("type")).get("type").equals("struct")) {
            if (jsonSchema.containsKey("fields")) {
                ((JSONArray)jsonSchema.get("fields")).forEach(f -> {
                    extractAttributes(attributes,jsonSchema.containsKey("name") ?
                            (!parent.isEmpty() ? parent + /*"." +*/ jsonSchema.getAsString("name") : jsonSchema.getAsString("name"))
                            : parent,((JSONObject)f));
                });
            }
            else if (jsonSchema.containsKey("type")) {
                ((JSONArray)((JSONObject)jsonSchema.get("type")).get("fields")).forEach(f -> {
                    extractAttributes(attributes,jsonSchema.containsKey("name") ?
                            (!parent.isEmpty() ? parent + /*"." +*/ jsonSchema.getAsString("name") : jsonSchema.getAsString("name"))
                            : parent,((JSONObject)f));
                });
            }
        }
        else if (!jsonSchema.get("type").equals("array")) {
            attributes.add(parent + /*"." +*/ jsonSchema.getAsString("name"));
        }

    }

    private String generateSparkSQLQuery(String tableName) {
        return "select * from ("+ tableName + ")";
    }

    @Override
    public String inferSchema() throws Exception {
        SparkSession spark = Utils.getSparkSession();
        Dataset<Row> ds = spark.read().json(this.path);
        ds.createOrReplaceTempView("inference");
        Set<String> attributes = Sets.newHashSet();
        extractAttributes(attributes,"",(JSONObject) JSONValue.parse(spark.sql(generateSparkSQLQuery("inference")).schema().json()));


        JSONObject res = new JSONObject(); res.put("schema",new Gson().toJson(attributes));
        spark.close();
        return res.toJSONString();
        //return super.inferSchema();
    }

    @Override
    public String preview(List<String> attributes) throws Exception {
        JSONArray data = new JSONArray();
        SparkSession spark = Utils.getSparkSession();
        Dataset<Row> ds = spark.read().json(this.path);
        String tableName = UUID.randomUUID().toString().replace("-","");
        ds.createTempView(tableName);

        spark.sql("select "+String.join(",",
                attributes.stream().filter(a->!a.isEmpty()).map(a -> "`"+a+"`")
                        .collect(Collectors.toList()))+" from "+tableName+" limit 10")
                .toJavaRDD()
                .collect()
                .forEach(r -> {
                    JSONArray arr = new JSONArray();
                    attributes.stream().filter(a->!a.isEmpty()).forEach(a -> {
                        JSONObject datum = new JSONObject();
                        datum.put("attribute",a);
                        datum.put("value",String.valueOf(r.get(r.fieldIndex(a))));
                        arr.add(datum);
                    });
                    data.add(arr);
                });
        spark.close();
        JSONObject res = new JSONObject(); res.put("data",data);
        return res.toJSONString();
    }

    @Override
    public void populate(String table, List<String> attributes) throws Exception {
        JSONArray data = new JSONArray();
        SparkSession spark = Utils.getSparkSession();
        Dataset<Row> ds = spark.read().json(this.path);
        String tableName = UUID.randomUUID().toString().replace("-","");
        ds.createTempView(tableName);

        spark.sql("select "+String.join(",",
                attributes.stream().filter(a->!a.isEmpty() && !a.equals("\'\'"))
                        .map(a ->  "`"+a.replace("'","")+"`")
                        .collect(Collectors.toList()))+" from "+tableName)                .toJavaRDD()
                .collect()
                .forEach(r -> {
                    JSONArray arr = new JSONArray();
                    attributes.stream().filter(a->!a.isEmpty() && !a.equals("\'\'")).forEach(a -> {
                        JSONObject datum = new JSONObject();
                        String att = a.replace("'","");
                        datum.put("attribute",att);
                        datum.put("value",String.valueOf(r.get(r.fieldIndex(att))));
                        arr.add(datum);
                    });
                    data.add(arr);
                });
        spark.close();
        SQLiteUtils.insertData(table,data);
    }
}
