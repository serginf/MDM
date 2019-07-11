package eu.supersede.mdm.storage.model.omq.relational_operators;

import com.clearspring.analytics.util.Lists;
import com.google.gson.Gson;
import eu.supersede.mdm.storage.model.omq.wrapper_implementations.*;
import eu.supersede.mdm.storage.util.RDFUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Wrapper extends RelationalOperator {

    private String wrapper;

    public Wrapper(String w) {
        this.wrapper = w;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Wrapper) {
            final Wrapper other = (Wrapper)o;
            return Objects.equals(wrapper,other.wrapper);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapper);
    }

    @Override
    public String toString() {
        return "("+RDFUtil.nn(wrapper)+")";
    }

    public String inferSchema() throws Exception {
        throw new Exception("Can't infer the schema of a generic wrapper, need to call an implementation subclass");
    }

    public String preview(List<String> attributes) throws Exception {
        throw new Exception("Can't preview a generic wrapper, need to call an implementation subclass");
    }

    public void populate(String table, List<String> attributes) throws Exception {
        throw new Exception("Can't populate a generic wrapper, need to call an implementation subclass");
    }

    public static Wrapper specializeWrapper(Document ds, String queryParameters) {
        Wrapper w = null;
        switch (ds.getString("type")) {
            case "avro":
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("avro_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(((JSONObject) JSONValue.parse(queryParameters)).getAsString("query"));
                break;
            case "csv":
                w = new CSV_Wrapper("preview");
                ((CSV_Wrapper)w).setPath(ds.getString("csv_path"));
                ((CSV_Wrapper)w).setColumnDelimiter(((JSONObject) JSONValue.parse(queryParameters)).getAsString("csvColumnDelimiter"));
                ((CSV_Wrapper)w).setRowDelimiter(((JSONObject) JSONValue.parse(queryParameters)).getAsString("csvRowDelimiter"));
                ((CSV_Wrapper)w).setHeaderInFirstRow(
                        Boolean.parseBoolean(((JSONObject) JSONValue.parse(queryParameters)).getAsString("headersInFirstRow")));
                break;
            case "mongodb":
                w = new MongoDB_Wrapper("preview");
                ((MongoDB_Wrapper)w).setConnectionString(ds.getString("mongodb_connectionString"));
                ((MongoDB_Wrapper)w).setDatabase(ds.getString("mongodb_database"));

                ((MongoDB_Wrapper)w).setMongodbQuery(((JSONObject) JSONValue.parse(queryParameters)).getAsString("query"));
                break;
            case "neo4j":
                w = new Neo4j_Wrapper("preview");

                break;
            case "plaintext":
                w = new PlainText_Wrapper("preview");
                ((PlainText_Wrapper)w).setPath(ds.getString("plaintext_path"));

                break;
            case "parquet":
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("parquet_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(((JSONObject) JSONValue.parse(queryParameters)).getAsString("query"));
                break;
            case "restapi":
                w = new REST_API_Wrapper("preview");
                ((REST_API_Wrapper)w).setUrl(ds.getString("restapi_url"));
                break;
            case "sql":
                w = new SQL_Wrapper("preview");
                ((SQL_Wrapper)w).setURL_JDBC(ds.getString("sql_jdbc"));
                JSONObject jsonObject =  (JSONObject) JSONValue.parse(queryParameters);
                ((SQL_Wrapper)w).setQuery(jsonObject.getAsString("query"));
                break;
            case "json":
                w = new JSON_Wrapper("preview");
                ((JSON_Wrapper)w).setPath(ds.getString("json_path"));
                //((JSON_Wrapper)w).setExplodeLevels(
                //    ((JSONArray)((JSONObject) JSONValue.parse(queryParameters)).get("explodeLevels")).stream().map(a -> (String)a).collect(Collectors.toList())
                //);
                //((JSON_Wrapper)w).setArrayOfValues(ds.getString("array"));
                //((JSON_Wrapper)w).setAttributeForSchema(ds.getString("key"));
                //((JSON_Wrapper)w).setValueForAttribute(ds.getString("values"));
                //((JSON_Wrapper)w).setCopyToParent(ds.getString("copyToParent"));
        }
        return w;
    }

}
