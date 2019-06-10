package eu.supersede.mdm.storage.parsers.models;

import java.util.List;

public class ClassAttribute {

    String id;
    String iri;
    String baseIri;
    String label;
//    List<Integer> pos; //for now null

    public ClassAttribute(){}

    public ClassAttribute(String id, String iri, String baseIri, String label) {
        this.id = id;
        this.iri = iri;
        this.baseIri = baseIri;
        this.label = label;
    }
}
