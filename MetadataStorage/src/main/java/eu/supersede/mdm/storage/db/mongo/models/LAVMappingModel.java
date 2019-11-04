package eu.supersede.mdm.storage.db.mongo.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

public class LAVMappingModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String wrapperID;
    private String isModified;
    private String globalGraphID;
    private String LAVMappingID;
    private List<LAVsameAs> sameAs;
    private List<String> graphicalSubGraph;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getWrapperID() {
        return wrapperID;
    }

    public void setWrapperID(String wrapperID) {
        this.wrapperID = wrapperID;
    }

    public String getIsModified() {
        return isModified;
    }

    public void setIsModified(String isModified) {
        this.isModified = isModified;
    }

    public String getGlobalGraphID() {
        return globalGraphID;
    }

    public void setGlobalGraphID(String globalGraphID) {
        this.globalGraphID = globalGraphID;
    }

    @BsonProperty("LAVMappingID")
    public String getLAVMappingID() {
        return LAVMappingID;
    }

    @BsonProperty("LAVMappingID")
    public void setLAVMappingID(String LAVMappingID) {
        this.LAVMappingID = LAVMappingID;
    }

    public List<LAVsameAs> getSameAs() {
        return sameAs;
    }

    public void setSameAs(List<LAVsameAs> sameAs) {
        this.sameAs = sameAs;
    }

    public List<String> getGraphicalSubGraph() {
        return graphicalSubGraph;
    }

    public void setGraphicalSubGraph(List<String> graphicalSubGraph) {
        this.graphicalSubGraph = graphicalSubGraph;
    }
}
