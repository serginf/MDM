package eu.supersede.mdm.storage.db.mongo.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

import java.util.List;

public class DataSourceModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String iri;
    private String dataSourceID;
    private String bootstrappingType;
    private String sourceFileAddress;
    private String csv_path;
    private String json_path;
    private String avro_path;
    private String mongodb_connectionString;
    private String mongodb_database;
    private String parquet_path;
    private String restapi_url;
    private String restapi_format;
    private String sql_jdbc;
    private String parsedFileAddress;
    private String name;
    private String schema_iri;
    private String type;
    private String graphicalGraph;
    private List<String> wrappers;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getDataSourceID() {
        return dataSourceID;
    }

    public void setDataSourceID(String dataSourceID) {
        this.dataSourceID = dataSourceID;
    }

    public String getBootstrappingType() {
        return bootstrappingType;
    }

    public void setBootstrappingType(String bootstrappingType) {
        this.bootstrappingType = bootstrappingType;
    }

    public String getSourceFileAddress() {
        return sourceFileAddress;
    }

    public void setSourceFileAddress(String sourceFileAddress) {
        this.sourceFileAddress = sourceFileAddress;
    }

    public String getCsv_path() {
        return csv_path;
    }

    public void setCsv_path(String csv_path) {
        this.csv_path = csv_path;
    }

    public String getParsedFileAddress() {
        return parsedFileAddress;
    }

    public void setParsedFileAddress(String parsedFileAddress) {
        this.parsedFileAddress = parsedFileAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema_iri() {
        return schema_iri;
    }

    public void setSchema_iri(String schema_iri) {
        this.schema_iri = schema_iri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGraphicalGraph() {
        return graphicalGraph;
    }

    public void setGraphicalGraph(String graphicalGraph) {
        this.graphicalGraph = graphicalGraph;
    }

    public List<String> getWrappers() {
        return wrappers;
    }

    public void setWrappers(List<String> wrappers) {
        this.wrappers = wrappers;
    }

    public String getJson_path() {
        return json_path;
    }

    public void setJson_path(String json_path) {
        this.json_path = json_path;
    }

    public String getAvro_path() {
        return avro_path;
    }

    public void setAvro_path(String avro_path) {
        this.avro_path = avro_path;
    }

    public String getMongodb_connectionString() {
        return mongodb_connectionString;
    }

    public void setMongodb_connectionString(String mongodb_connectionString) {
        this.mongodb_connectionString = mongodb_connectionString;
    }

    public String getMongodb_database() {
        return mongodb_database;
    }

    public void setMongodb_database(String mongodb_database) {
        this.mongodb_database = mongodb_database;
    }

    public String getParquet_path() {
        return parquet_path;
    }

    public void setParquet_path(String parquet_path) {
        this.parquet_path = parquet_path;
    }

    public String getRestapi_url() {
        return restapi_url;
    }

    public void setRestapi_url(String restapi_url) {
        this.restapi_url = restapi_url;
    }

    public String getRestapi_format() {
        return restapi_format;
    }

    public void setRestapi_format(String restapi_format) {
        this.restapi_format = restapi_format;
    }

    public String getSql_jdbc() {
        return sql_jdbc;
    }

    public void setSql_jdbc(String sql_jdbc) {
        this.sql_jdbc = sql_jdbc;
    }
}
