/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getGlobalGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "globalGraph/"+req.params.globalGraphID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving global graph");
        }
    });
};

exports.getGlobalGraphFromNamedGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "globalGraph/namedGraph/"+encodeURIComponent(req.params.namedGraph), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving global graph");
        }
    });
};

/*
exports.getBDIOntologyFromGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/graph/"+encodeURIComponent(req.params.graph), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving BDI Ontology");
        }
    });
};
*/
exports.getAllGlobalGraphs = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "globalGraph/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of global graphs");
        }
    });
};

exports.getFeaturesForGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "globalGraph/"+encodeURIComponent(req.params.namedGraph)+"/features", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of features for the global graph");
        }
    });
};

exports.postGlobalGraph = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null
       || !(req.body.hasOwnProperty('defaultNamespace')) || req.body.defaultNamespace==null){
        res.status(400).json({msg: "(Bad Request) data format: {name, defaultNamespace}"});
    } else {
        var objGlobalGraph = new Object();
        objGlobalGraph.name = req.body.name;
        objGlobalGraph.defaultNamespace = req.body.defaultNamespace;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "globalGraph/",
            body: JSON.stringify(objGlobalGraph)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error storing global graph");
            }
        });
    }
};

exports.postGraph = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "globalGraph/"+encodeURIComponent(req.params.namedGraph)+"/triple/",
        data: req.body
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};

exports.postTriple = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "globalGraph/"+encodeURIComponent(req.params.namedGraph)+"/triple/",
        data: req.body
        //encodeURIComponent(req.body.s) + "/" + encodeURIComponent(req.body.p) + "/" + encodeURIComponent(req.body.o)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};

exports.postGraphicalGraph = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "globalGraph/"+req.params.globalGraphID+"/graphicalGraph",
        body: JSON.stringify(req.body.graphicalGraph)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};

exports.postSparQLQuery = function (req, res) {
    var query = req.body;
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "bdi_ontology/sparQLQuery/",
        body: JSON.stringify(query)
    }, function done(error, response, body) {
        //console.log("Got response "+error+" - "+response+" - "+body);
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error posting SparQL query");
        }
    });
};