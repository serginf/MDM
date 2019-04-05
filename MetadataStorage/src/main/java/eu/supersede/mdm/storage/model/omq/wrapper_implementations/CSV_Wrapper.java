package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
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

public class CSV_Wrapper extends Wrapper {

    private String path;
    private String columnDelimiter;
    private String rowDelimiter;
    private boolean headerInFirstRow;

    public CSV_Wrapper(String name) {
        super(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public String getRowDelimiter() {
        return rowDelimiter;
    }

    public void setRowDelimiter(String rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }

    public boolean isHeaderInFirstRow() {
        return headerInFirstRow;
    }

    public void setHeaderInFirstRow(boolean headerInFirstRow) {
        this.headerInFirstRow = headerInFirstRow;
    }

    @Override
    public String inferSchema() throws Exception {
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read()
                .option("header",String.valueOf(this.headerInFirstRow))
                .option("delimiter",this.columnDelimiter)
                .csv(this.path);
        JSONObject res = new JSONObject(); res.put("schema",new Gson().toJson(ds.schema().fieldNames()));
        return res.toJSONString();
    }

    @Override
    public String preview(List<String> attributes) throws Exception {
        JSONArray data = new JSONArray();
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read()
                .option("header",String.valueOf(this.headerInFirstRow))
                .option("delimiter",this.columnDelimiter)
                .csv(this.path);
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
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read()
                .option("header",String.valueOf(this.headerInFirstRow))
                .option("delimiter",this.columnDelimiter)
                .csv(this.path);
        String tableName = UUID.randomUUID().toString().replace("-","");
        ds.createTempView(tableName);

        spark.sql("select "+String.join(",",
                attributes.stream().filter(a->!a.isEmpty() && !a.equals("\'\'")).map(a ->  "`"+a.replace("'","")+"`")
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
