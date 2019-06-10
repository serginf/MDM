package eu.supersede.mdm.storage.parsers.models;

import java.util.ArrayList;
import java.util.List;

public class JsonWebVowl {
    Header header;
    List<String> namespace = new ArrayList<>(); //it's empty
    List<Nodes> classes = new ArrayList<>(); // TODO_replace when toString json
    List<ClassAttribute> classAttribute;
    List<Property> property;
    List<PropertyAttribute> propertyAttribute;

    public JsonWebVowl(){}

    public JsonWebVowl(Header header, List<String> namespace, List<Nodes> classes, List<ClassAttribute> classAttribute, List<Property> property, List<PropertyAttribute> propertyAttribute) {
        this.header = header;
        this.namespace = namespace;
        this.classes = classes;
        this.classAttribute = classAttribute;
        this.property = property;
        this.propertyAttribute = propertyAttribute;
    }
}
