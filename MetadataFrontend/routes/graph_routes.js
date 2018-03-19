/**
 * Created by snadal on 7/06/16.
 */
var config = require(__dirname+'/../config'),
    request = require('request');

exports.postGraph = function (req, res, next) {
    var graph = new Object();
    graph.iri = req.body.iri;
    graph.rdf = req.body.rdf;
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "graph/",
        data: JSON.stringify(graph)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};

exports.getGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "graph/"+encodeURIComponent(req.params.iri), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving graph content");
        }
    });
};

exports.getGraphGraphical = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "graph/SOURCE/"+encodeURIComponent(req.params.iri)+"/graphical", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving graphical representation of graph");
        }
    });
};

exports.postTriple = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "graph/"+encodeURIComponent(req.params.iri)+"/triple/"+
            encodeURIComponent(req.body.s) + "/" + encodeURIComponent(req.body.p) + "/" + encodeURIComponent(req.body.o)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};
/*
exports.postGraphicalGraph = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(req.params.artifactID)+"/graphicalGraph",
        body: JSON.stringify(req.body.graphicalGraph)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};
*/