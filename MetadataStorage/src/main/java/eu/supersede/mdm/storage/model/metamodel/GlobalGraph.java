package eu.supersede.mdm.storage.model.metamodel;

import eu.supersede.mdm.storage.model.Namespaces;

import javax.naming.Name;

/**
 * Created by snadal on 6/06/17.
 */
public enum GlobalGraph {

    CONCEPT(Namespaces.G.val()+"Concept"),
    FEATURE(Namespaces.G.val()+"Feature"),
    HAS_FEATURE(Namespaces.G.val()+"hasFeature"),
    INTEGRITY_CONSTRAINT(Namespaces.G.val()+"IntegrityConstraint"),
    HAS_INTEGRITY_CONSTRAINT(Namespaces.G.val()+"hasConstraint"),
    DATATYPE(Namespaces.rdfs.val()+"Datatype"),
    HAS_DATATYPE(Namespaces.G.val()+"hasDatatype"),
    HAS_RELATION(Namespaces.G.val()+"hasRelation"),
    SAME_AS(Namespaces.G.val()+"sameAs"),

    PART_OF(Namespaces.G.val()+"partOf"),
    AGGREGATION_FUNCTION(Namespaces.G.val()+"aggregationFunction"),
    HAS_AGGREGATION_FUNCTION(Namespaces.G.val()+"hasAggregationFunction");



    private String element;

    GlobalGraph(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
