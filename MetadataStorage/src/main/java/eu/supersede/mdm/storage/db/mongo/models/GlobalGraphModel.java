package eu.supersede.mdm.storage.db.mongo.models;

import org.bson.types.ObjectId;

public class GlobalGraphModel {

    private ObjectId id;
    private String namedGraph;
    private String defaultNamespace;
    private String name;
    private String globalGraphID;
    private String graphicalID;
    private String[] wrappers;

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

    public String getGraphicalID() {
        return graphicalID;
    }

    public void setGraphicalID(String graphicalID) {
        this.graphicalID = graphicalID;
    }

    public String[] getWrappers() {
        return wrappers;
    }

    public void setWrappers(String[] wrappers) {
        this.wrappers = wrappers;
    }
}
