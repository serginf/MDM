package eu.supersede.mdm.storage.model.omq.relational_operators;

import com.google.common.collect.Maps;
import eu.supersede.mdm.storage.model.Namespaces;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

public class AggregatedAttribute {

    private AggregationFunctions function;
    private String attribute;

    public AggregatedAttribute() {}

    public AggregatedAttribute(AggregationFunctions function, String attribute) {
        this.function = function;
        this.attribute = attribute;
    }

    public AggregationFunctions getFunction() {
        return function;
    }

    public void setFunction(AggregationFunctions function) {
        this.function = function;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  AggregatedAttribute) {
            final AggregatedAttribute other = (AggregatedAttribute)o;
            return Objects.equals(function,other.function) && Objects.equals(attribute,other.attribute);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, attribute);
    }

    @Override
    public String toString() {

        Map<String,String> namespaces = Maps.newHashMap();
        EnumSet.allOf(Namespaces.class).forEach(e -> namespaces.put(e.val(),e.name()));
        String uri = namespaces.keySet().stream().filter(n -> attribute.contains(n)).findFirst().get();
        return function+"("+attribute.replace(uri,namespaces.get(uri)+":")+")";
    }
}
