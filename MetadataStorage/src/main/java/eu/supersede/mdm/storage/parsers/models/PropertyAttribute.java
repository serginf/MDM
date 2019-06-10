package eu.supersede.mdm.storage.parsers.models;

import java.util.ArrayList;
import java.util.List;

public class PropertyAttribute {

    String id;
    String iri;
    String baseIri;
    String label;
    List<String> attributes;
    String domain;
    String range;

    public PropertyAttribute(){
        this.attributes = new ArrayList<>();
        this.attributes.add("object");
    }

    public PropertyAttribute(String id, String iri, String baseIri, String label, String domain, String range) {
        this.id = id;
        this.iri = iri;
        this.baseIri = baseIri;
        this.label = label;
        this.domain = domain;
        this.range = range;
        this.attributes = new ArrayList<>();
        this.attributes.add("object");
    }

}
