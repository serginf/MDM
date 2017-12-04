/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getBDIOntology = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/"+req.params.bdi_ontologyID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving BDI Ontology");
        }
    });
};

exports.getBDIOntologyFromGraph = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/graph/"+encodeURIComponent(req.params.graph), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving BDI Ontology");
        }
    });
};

exports.getAllBDIOntologies = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of BDI Ontologies");
        }
    });
};

exports.postBDIOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null
       || !(req.body.hasOwnProperty('releases')) || req.body.releases==null){
        res.status(400).json({msg: "(Bad Request) data format: {name, releases}"});
    } else {
        var GLOBAL = config.DEFAULT_NAMESPACE+"GLOBAL/"+randomstring.generate();
        var MAPPINGS = config.DEFAULT_NAMESPACE+"MAPPINGS/"+randomstring.generate();
        var BDI_O = config.DEFAULT_NAMESPACE+"BDI_ONTOLOGY/"+randomstring.generate();
        var RULES = config.DEFAULT_NAMESPACE+"RULES/"+randomstring.generate();

        var objBody = req.body;
        objBody.globalLevel = GLOBAL;
        objBody.mappings = MAPPINGS;
        objBody.O = BDI_O;
        objBody.rules = RULES;

        var globalLevel = new Object();
        globalLevel.name = objBody.name;
        globalLevel.type = "GLOBAL";
        globalLevel.graph = objBody.globalLevel;

        var mappings = new Object();
        mappings.name = objBody.name;
        mappings.type = "MAPPINGS";
        mappings.graph = objBody.mappings;

        var O = new Object();
        O.name = objBody.name;
        O.type = "BDI_ONTOLOGY";
        O.graph = objBody.O;

        var R = new Object();
        R.name = objBody.name;
        R.type = "RULES";
        R.graph = objBody.rules;

        async.parallel([
                function(callback){
                    // Register the BDI Ontology
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "bdi_ontology/",
                        body: JSON.stringify(objBody)
                    }, function done(err, results) {
                        callback();
                    });
                },
                function(callback) {
                    // Register an empty global level
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/",
                        body: JSON.stringify(globalLevel)
                    }, function done(err, results) {
                        callback();
                    });
                },
                function(callback) {
                    // Register empty mappings
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/",
                        body: JSON.stringify(mappings)
                    }, function done(err, results) {
                        callback();
                    });
                }
            ],
            function(err, results){
                // Apply the generation strategy for G and M
                request.post({
                    url: config.METADATA_DATA_LAYER_URL + "bdi_ontology/generationStrategy",
                    body: JSON.stringify(objBody)
                }, function done(err, results) {
                    // Store artifact G
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(globalLevel.graph),
                        body: JSON.parse(results.body).G
                    }, function done(err2, results2) {
                        request.post({
                            url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(mappings.graph),
                            body: JSON.parse(results.body).M
                        }, function done(err3, results3) {
                            request.post({
                                url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(O.graph),
                                body: JSON.parse(results.body).O
                            }, function done(err4, results4) {
                                request.post({
                                    url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(R.graph),
                                    body: JSON.parse(results.body).R
                                }, function done(err5, results5) {
                                    res.status(200).json("success");
                                });
                            });
                        });
                    });
                });
            });
    }
};

exports.getGenerationStrategies = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology_generation_strategies", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of BDI Ontology generation strategies types");
        }
    });
};