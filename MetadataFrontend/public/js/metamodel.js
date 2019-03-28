
/**
 * Created by snadal on 21/12/16.
 */

const Namespaces = {
    S: "http://www.essi.upc.edu/~snadal/BDIOntology/Source/",
    G: "http://www.essi.upc.edu/~snadal/BDIOntology/Global/",
    owl: "http://www.w3.org/2002/07/owl#",
    rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    rdfs:"http://www.w3.org/2000/01/rdf-schema#",
    dct: "http://purl.org/dc/terms/",
    dcat: "http://www.w3.org/ns/dcat#",
    sc: "http://schema.org/"
};

const Global = {
    CONCEPT: {
        iri: Namespaces.G+"Concept",
        name: "Concept",
        color: "#33CCCC",
        isID: false
    },
    /*MEMBERCONCEPT: {
        iri: Namespaces.G+"MemberConcept",
        name: "MemberConcept",
        color: "#1dcc72",
        isID: false
    },*/
    HAS_RELATION: {
        iri: Namespaces.G+"hasRelation",
        name: "hasRelation",
        color: "#33CCCC",
        isID: false
    },
    FEATURE: {
        iri: Namespaces.G+"Feature",
        name: "Feature",
        color: "#D7DF01",
        isID: false
    },
    FEATURE_ID: {
        iri: Namespaces.G+"Feature",
        name: "Feature_ID",
        color: "#FF6600",
        isID: true
    },
    HAS_FEATURE: {
        iri: Namespaces.G+"hasFeature",
        name: "hasFeature",
        color: "#D7DF01",
        isID: false
    },
    PART_OF: {
        iri: Namespaces.G+"partOf",
        name: "partOf",
        color: "#D7DF01",
        isID: false
    }/*,
    AGGREGATIONFUNCTION: {
        iri: Namespaces.G+"AggregationFunction",
        name: "AggregationFunction",
        color: "#aa4adf",
        isID: false
    },
    HAS_AGGREGATIONFUNCTION: {
        iri: Namespaces.G+"hasAggregationFunction",
        name: "hasAggregationFunction",
        color: "#aa4adf",
        isID: false
    }*/
    /*
    HAS_DATATYPE: {
        iri: Namespaces.G+"hasDatatype",
        name: "hasDatatype",
        color: "#FF6600"
    }
    */
};

const Source = {
    EVENT: {
        iri: Namespaces.S+"Event",
        name: "Event",
        color: "#FF3300"
    },
    SCHEMA_VERSION: {
        iri: Namespaces.S+"SchemaVersion",
        name: "SchemaVersion",
        color: "#FECB98"
    },
    ATTRIBUTE: {
        iri: Namespaces.rdfs+"Attribute",
        name: "Attribute",
        color: "#00CCFF"
    }
}


function getGlobalEdge(namespaceOrigin, namespaceDest) {
    if (namespaceOrigin == Global.CONCEPT.iri && namespaceDest == Global.FEATURE.iri) return Global.HAS_FEATURE.iri;
    if (namespaceOrigin == Global.FEATURE.iri && namespaceDest == Global.INTEGRITY_CONSTRAINT.iri) return Global.HAS_INTEGRITY_CONSTRAINT.iri;
    if (namespaceOrigin == Global.FEATURE.iri && namespaceDest == Global.DATATYPE.iri) return Global.HAS_DATATYPE.iri;
    if (namespaceOrigin == Global.CONCEPT.iri && namespaceDest == Global.CONCEPT.iri) return Global.HAS_RELATION.iri;
    /*    if (namespaceOrigin == Global.MEMBERCONCEPT.iri && namespaceDest == Global.MEMBERCONCEPT.iri) return Global.PART_OF.iri;
        if (namespaceOrigin == Global.MEMBERCONCEPT.iri && namespaceDest == Global.CONCEPT.iri) return Global.HAS_RELATION.iri;
        if (namespaceOrigin == Global.MEMBERCONCEPT.iri && namespaceDest == Global.FEATURE_ID.iri) return Global.HAS_FEATURE.iri;
        if (namespaceOrigin == Global.MEMBERCONCEPT.iri && namespaceDest == Global.FEATURE.iri) return Global.HAS_FEATURE.iri;*/
    return null;
}