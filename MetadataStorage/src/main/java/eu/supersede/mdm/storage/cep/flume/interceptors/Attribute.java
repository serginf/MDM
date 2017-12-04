package eu.supersede.mdm.storage.cep.flume.interceptors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osboxes on 25/06/17.
 */
class Attribute {
    private String attName;
    private Map<String, String> operations;
    private String type;

    public Attribute() {
        operations = new HashMap<String, String>();
    }

    public String getType() {
        return type;
    }

    public void setType(String attName) {
        this.type = attName;
    }

    public String getAttName() {
        return attName;
    }

    public void setAttName(String attName) {
        this.attName = attName;
    }

    public Map<String, String> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, String> operations) {
        this.operations = operations;
    }

    public void addOperation(String operation, String value) {
        this.operations.put(operation, value);
    }

}
