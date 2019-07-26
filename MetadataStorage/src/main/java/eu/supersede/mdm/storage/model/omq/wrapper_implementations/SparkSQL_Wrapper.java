package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class SparkSQL_Wrapper extends Wrapper {

    private String path;
    private String tableName;
    private String sparksqlQuery;

    public SparkSQL_Wrapper(String name) {
        super(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSparksqlQuery() {
        return sparksqlQuery;
    }

    public void setSparksqlQuery(String sparksqlQuery) {
        this.sparksqlQuery = sparksqlQuery;
    }

    @Override
    public String preview(List<String> attributes) throws Exception {
        SparkSession spark = Utils.getSparkSession();
        Dataset<Row> ds = spark.read().parquet(path);
        ds.createOrReplaceTempView(tableName);
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
        spark.close();
        return res.toJSONString();
    }

    @Override
    public void populate(String table, List<String> attributes) throws Exception {
        SparkSession spark = Utils.getSparkSession();
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
        spark.close();
    }

}
