package eu.supersede.mdm.storage.model.omq.relational_operators;

import eu.supersede.mdm.storage.model.omq.wrapper_implementations.*;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.bson.Document;

import java.util.List;
import java.util.Objects;

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

    public String preview(List<String> attributes) throws Exception {
        throw new Exception("Can't preview a generic wrapper, need to call an implementation subclass");
    }

    public void populate(String table, List<String> attributes) throws Exception {
        throw new Exception("Can't populate a generic wrapper, need to call an implementation subclass");
    }

    public static Wrapper specializeWrapper(Document ds, String query) {
        Wrapper w = null;
        switch (ds.getString("type")) {
            case "avro":
                w = new SparkSQL_Wrapper("preview");
                ((SparkSQL_Wrapper)w).setPath(ds.getString("avro_path"));
                ((SparkSQL_Wrapper)w).setTableName(ds.getString("name"));
                ((SparkSQL_Wrapper)w).setSparksqlQuery(query);
                break;
            case "mongodb":
                w = new MongoDB_Wrapper("preview");
                ((MongoDB_Wrapper)w).setConnectionString(ds.getString("mongodb_connectionString"));
                ((MongoDB_Wrapper)w).setDatabase(ds.getString("mongodb_database"));

                ((MongoDB_Wrapper)w).setMongodbQuery(query);
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
                ((SparkSQL_Wrapper)w).setSparksqlQuery(query);
                break;
            case "restapi":
                w = new REST_API_Wrapper("preview");
                ((REST_API_Wrapper)w).setUrl(ds.getString("restapi_url"));
                break;
            case "sql":
                w = new SQL_Wrapper("preview");
                break;
        }
        return w;
    }

}
