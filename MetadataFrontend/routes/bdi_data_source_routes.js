/**
 * Created by Kashif Rabbani
 */
var fs = require('fs'),
    config = require(__dirname + '/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getDataSource = function (req, res, next) {
    request.get(config.BDI_DATA_LAYER_URL + "bdiDataSource/" + req.params.dataSourceID, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving data source");
        }
    });
};

exports.getIntegratedDataSource = function (req, res, next) {
    request.get(config.BDI_DATA_LAYER_URL + "bdiIntegratedDataSources/" + req.params.integratedDataSourceID, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving data source");
        }
    });
};

exports.getAllDataSources = function (req, res, next) {
    request.get(config.BDI_DATA_LAYER_URL + "bdiDataSource/", function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of data sources");
        }
    });
};

exports.getAllIntegratedDataSources = function (req, res, next) {
    request.get(config.BDI_DATA_LAYER_URL + "bdiIntegratedDataSources/", function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of data sources");
        }
    });
};

exports.deleteDataSource = function (req, res) {
    request.get(config.BDI_DATA_LAYER_URL + "bdiDeleteDataSource/" + req.params.ds_id, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error deleting data source");
        }
    });
};


exports.postDataSource = function (req, res, next) {
    console.log(req.body);
    if (!(req.body.hasOwnProperty('name')) || req.body.name == null ||
        !(req.body.hasOwnProperty('type')) || req.body.type == null) {
        res.status(400).json({msg: "(Bad Request) data format: {name, type}"});
    } else {
        var objDataSource = req.body;
        objDataSource.wrappers = new Array();

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "bdiDataSource/",
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log(body);
                request.post({
                    url: config.METADATA_DATA_LAYER_URL + "graph/" + encodeURIComponent(JSON.parse(body).iri),
                    body: JSON.parse(body).rdf
                }, function done(err, results) {
                    res.status(200).json("ok");
                });
            } else {
                res.status(500).send("Error storing data source");
            }
        });
    }
};
