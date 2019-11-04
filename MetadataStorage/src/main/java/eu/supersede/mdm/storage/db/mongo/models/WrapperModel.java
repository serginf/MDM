package eu.supersede.mdm.storage.db.mongo.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

import java.util.List;

public class WrapperModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String iri;
    private String dataSourceID;
    private String wrapperID;
    private String query;
    private String name;
    private List<WrapperAtributes> attributes;

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

    public String getWrapperID() {
        return wrapperID;
    }

    public void setWrapperID(String wrapperID) {
        this.wrapperID = wrapperID;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WrapperAtributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<WrapperAtributes> attributes) {
        this.attributes = attributes;
    }
}
