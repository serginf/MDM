package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Sets;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Set;

public class SchemaInferenceTest {

    private static void extractAttributes(Set<String> attributes, String parent, JSONObject jsonSchema) {
        if (jsonSchema.get("type") instanceof String && jsonSchema.get("type").equals("struct") ||
            jsonSchema.get("type") instanceof JSONObject && ((JSONObject) jsonSchema.get("type")).get("type").equals("struct")) {
            if (jsonSchema.containsKey("fields")) {
                ((JSONArray)jsonSchema.get("fields")).forEach(f -> {
                    extractAttributes(attributes,jsonSchema.containsKey("name") ?
                            (!parent.isEmpty() ? parent + "." + jsonSchema.getAsString("name") : jsonSchema.getAsString("name"))
                            : parent,((JSONObject)f));
                });
            }
            else if (jsonSchema.containsKey("type")) {
                ((JSONArray)((JSONObject)jsonSchema.get("type")).get("fields")).forEach(f -> {
                    extractAttributes(attributes,jsonSchema.containsKey("name") ?
                            (!parent.isEmpty() ? parent + "." + jsonSchema.getAsString("name") : jsonSchema.getAsString("name"))
                            : parent,((JSONObject)f));
                });
            }
        }
        else if (!jsonSchema.get("type").equals("array")) {
            attributes.add(parent + "." + jsonSchema.getAsString("name"));
        }

    }

    private static String aggregatedYearDrugDist = "/home/snadal/UPC/Projects/WHO/MDM_Demo/datasets/Data-WIDP/aggregated-per-year-drugDistribution/ALL-Export_2000-03-01_2019-03-22_Global.json";
    private static String diagnosis = "/home/snadal/UPC/Projects/WHO/MDM_Demo/datasets/Data-WIDP/individual-per-year-diagnosis/events-diagnosis-all.json";
    private static String testFile = "/home/snadal/UPC/Projects/WHO/MDM_Demo/datasets/Data-WIDP/test.json";


    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read().json(aggregatedYearDrugDist);
        ds.createOrReplaceTempView("testTable");

        //works for aggregatedYearDrugDist
        //spark.sql("select * from (select explode(dataValues) as dataValues from testTable)").foreach(r -> System.out.println(r.toString()));
        Set<String> attributes = Sets.newHashSet();
        extractAttributes(attributes, "", (JSONObject) JSONValue.parse(spark.sql("select * from (select explode(dataValues) as dataValues from testTable)").schema().json()));
        //extractAttributes(attributes, "", (JSONObject) JSONValue.parse(spark.sql("select * from testTable").schema().json()));
        System.out.println(attributes);
//        System.out.println("###################################");
//        System.out.println(spark.sql("select * from (select explode(dataValues) as dataValues from testTable)").schema().catalogString());


        //works for diagnosis (double array)
        //spark.sql("select * from (select explode(events.dataValues) from (select explode(events) as events from testTable))").foreach(r -> System.out.println(r.toString()));
        //spark.sql("select events.coordinate.latitude from (select explode(events) as events from testTable)").foreach(r -> System.out.println(r.toString()));

        //System.out.println(spark.sql("select * from (select explode(events.dataValues) from (select explode(events) as events from testTable))").schema().prettyJson());
        //System.out.println(spark.sql("select events.coordinate.latitude from (select explode(events) as events from testTable)").schema().prettyJson());
    }
}
