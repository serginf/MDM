package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class DHIS_Wrapper extends Wrapper {

    private String path;

    public DHIS_Wrapper(String name) {
        super(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
/*
    @Override
    public String preview(List<String> attributes) throws Exception {
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read().parquet(path);
        ds.createOrReplaceTempView("temp");
        JSONArray data = new JSONArray();
        spark.sql(sparksqlQuery).takeAsList(10).forEach(e -> {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < e.size(); ++i) {
                JSONObject datum = new JSONObject();
                datum.put("attribute",attributes.get(i));
                datum.put("value",e.get(i).toString());
                arr.add(datum);
            }
            data.add(arr);
        });
        JSONObject res = new JSONObject(); res.put("data",data);
        return res.toJSONString();
    }
    */
/*
    @Override
    public void populate(String table, List<String> attributes) throws Exception {
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read().parquet(path);
        ds.createOrReplaceTempView(tableName);
        JSONArray data = new JSONArray();
        spark.sql(sparksqlQuery).collectAsList().forEach(e -> {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < e.size(); ++i) {
                JSONObject datum = new JSONObject();
                datum.put("attribute",attributes.get(i));
                datum.put("value",e.get(i).toString());
                arr.add(datum);
            }
            data.add(arr);
        });
        SQLiteUtils.insertData(table,data);
    }
*/
}
