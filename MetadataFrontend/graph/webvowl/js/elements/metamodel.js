module.exports = function () {
    var options = {},
        namespaces={
            S: "http://www.essi.upc.edu/~snadal/BDIOntology/Source/",
            G: "http://www.essi.upc.edu/~snadal/BDIOntology/Global/",
            owl: "http://www.w3.org/2002/07/owl#",
            rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            rdfs:"http://www.w3.org/2000/01/rdf-schema#",
            dct: "http://purl.org/dc/terms/",
            dcat: "http://www.w3.org/ns/dcat#",
            sc: "http://schema.org/"
        },
        colors = {
            concept:  "#33CCCC",
            feature:  "#D7DF01",
            feature_id: "#FF6600"
        };

    options.namespaces=function () {
        return namespaces;
    };
    options.colors=function () {
        return colors;
    };

    return options;
};
