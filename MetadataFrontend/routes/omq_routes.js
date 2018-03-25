/**
 * Created by snadal on 18/05/16.
 */
var config = require(__dirname+'/../config'),
    request = require('request');

exports.postFromGraphicalToSPARQL = function (req, res, next) {
    if (!(req.body.hasOwnProperty('selection')) || req.body.selection==null ||
        !(req.body.hasOwnProperty('projectedFeatures')) || req.body.projectedFeatures==null){
        res.status(400).json({msg: "(Bad Request) data format: {selection,projectedFeatures}"});
    } else {
        var objFromGraphicalToSPARQL = req.body;
        request.post({
            url: config.METADATA_DATA_LAYER_URL + "omq/fromGraphicalToSPARQL",
            body: JSON.stringify(objFromGraphicalToSPARQL)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error generating SPARQL");
            }
        });
    }
};

exports.postFromSPARQLToGraphical = function (req, res, next) {
    if (!(req.body.hasOwnProperty('sparql')) || req.body.sparql==null ||
        !(req.body.hasOwnProperty('namedGraph')) || req.body.namedGraph==null){
        res.status(400).json({msg: "(Bad Request) data format: {sparql,namedGraph}"});
    } else {
        var objFromSPARQLToRA = req.body;
        request.post({
            url: config.METADATA_DATA_LAYER_URL + "omq/fromSPARQLToRA",
            body: JSON.stringify(objFromSPARQLToRA)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error generating RA");
            }
        });
    }
};

exports.postFromSQLToData = function (req, res, next) {
    if (!(req.body.hasOwnProperty('sql')) || req.body.sql==null ||
        !(req.body.hasOwnProperty('wrappers')) || req.body.wrappers==null ||
        !(req.body.hasOwnProperty('features')) || req.body.features==null){
        res.status(400).json({msg: "(Bad Request) data format: {sql,wrappers, features}"});
    } else {
        var objFromSQLToData = req.body;
        request.post({
            url: config.METADATA_DATA_LAYER_URL + "omq/fromSQLToData",
            body: JSON.stringify(objFromSQLToData)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error getting data");
            }
        });
    }
};
