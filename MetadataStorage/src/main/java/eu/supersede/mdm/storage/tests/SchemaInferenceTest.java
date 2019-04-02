package eu.supersede.mdm.storage.tests;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.AttributeReference;
import org.apache.spark.sql.types.StructField;

import java.util.Iterator;

public class SchemaInferenceTest {

    private static String aggregatedYearDrugDist = "/home/snadal/UPC/Projects/WHO/MDM_Demo/datasets/Data-WIDP/aggregated-per-year-drugDistribution/ALL-Export_2000-03-01_2019-03-22_Global.json";
    private static String diagnosis = "/home/snadal/UPC/Projects/WHO/MDM_Demo/datasets/Data-WIDP/individual-per-year-diagnosis/events-diagnosis-all.json";

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder().master("local").appName("parquetPreview").getOrCreate();
        Dataset<Row> ds = spark.read().json(diagnosis);
        ds.createOrReplaceTempView("testTable");

        //works for aggregatedYearDrugDist
        //spark.sql("select * from (select explode(dataValues) as dataValues from testTable)").foreach(r -> System.out.println(r.toString()));
        //spark.sql("select * from (select explode(dataValues) as dataValues from testTable)").schema().catalogString();


        //works for diagnosis (double array)
        spark.sql("select * from (select explode(events.dataValues) from (select explode(events) as events from testTable))").foreach(r -> System.out.println(r.toString()));
    }
}
