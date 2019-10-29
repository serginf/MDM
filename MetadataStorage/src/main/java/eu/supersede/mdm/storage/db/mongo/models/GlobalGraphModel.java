package eu.supersede.mdm.storage.db.mongo.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

import java.util.List;

public class GlobalGraphModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String namedGraph;
    private String defaultNamespace;
    private String name;
    private String globalGraphID;
    private String graphicalGraph;
    private List<String> wrappers;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNamedGraph() {
        return namedGraph;
    }

    public void setNamedGraph(String namedGraph) {
        this.namedGraph = namedGraph;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGlobalGraphID() {
        return globalGraphID;
    }

    public void setGlobalGraphID(String globalGraphID) {
        this.globalGraphID = globalGraphID;
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
}
