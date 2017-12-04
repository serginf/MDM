package eu.supersede.mdm.storage.cep.flume.sinks;

import org.apache.avro.Schema;

import java.util.Map;
import java.util.Set;

/**
 * Created by osboxes on 08/05/17.
 */
public class SinkEvent {

    private Map<String, String> attributes;
    private Set<String> attributeNames;
    private Schema schema;

    public SinkEvent() {
    }

    public SinkEvent(Map<String, String> attributes, Set<String> attributeNames, Schema schema) {
        this.attributes = attributes;
        this.attributeNames = attributeNames;
        this.schema = schema;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Set<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(Set<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
