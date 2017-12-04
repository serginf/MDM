/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getStatisticalAnalysisModel = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "statistical_analysis_model/"+req.params.statistical_analysis_modelID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving Statistical Analysis Model");
        }
    });
};

exports.getAllStatisticalAnalysisModels = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "statistical_analysis_model/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of Statistical Analysis Models");
        }
    });
};

exports.postStatisticalAnalysisModel = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null
       || !(req.body.hasOwnProperty('releases')) || req.body.releases==null){
        res.status(400).json({msg: "(Bad Request) data format: {name, releases}"});
    } else {
        var graphName = config.DEFAULT_NAMESPACE+"GLOBAL/"+randomstring.generate();
        var objBody = req.body;
        objBody.globalLevel = graphName;

        async.parallel([
                function(callback){
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "statistical_analysis_model/",
                        body: JSON.stringify(objBody)
                    }, function done(err, results) {
                        callback();
                    });
                }/*,
                function(callback) {
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(graphName),
                        body: ""
                    }, function done(err, results) {
                        callback();
                    });
                }*/
            ],
            function(err, results){
                res.status(200).json("success");
            });
    }
};

exports.getStatisticalAnalysisModelTypes = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "statistical_analysis_model_types/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of Statistical Analysis Model types");
        }
    });
};