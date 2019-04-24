module.exports = function () {
    var options = {},
        Namespaces={
            S: "http://www.essi.upc.edu/~snadal/BDIOntology/Source/",
            G: "http://www.essi.upc.edu/~snadal/BDIOntology/Global/",
            owl: "http://www.w3.org/2002/07/owl#",
            rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            rdfs:"http://www.w3.org/2000/01/rdf-schema#",
            dct: "http://purl.org/dc/terms/",
            dcat: "http://www.w3.org/ns/dcat#",
            sc: "http://schema.org/"
        },
        Global = {
            CONCEPT: {
                iri: Namespaces.G+"Concept",
                name: "Concept",
                color: "#33CCCC",
                isID: false
            },
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
            }
        };

    options.Namespaces=function () {
        return Namespaces;
    };
    options.Global=function () {
        return Global;
    };

    return options;
};
