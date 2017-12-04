/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request');

exports.getDatasets = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "datasets/"+req.user.username, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of datasets");
        }
    });
};

exports.getDataset = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "datasets/"+(req.params.datasetID)+"/"+req.user.username, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of datasets");
        }
    });
};

exports.postXMLDataset = function (req, res, next) {
    if (!(req.body.hasOwnProperty('datasetName')) || req.body.datasetName==null
       || !(req.files.hasOwnProperty('xmlSchemaFile')) || req.files.xmlSchemaFile==null
       || !(req.files.hasOwnProperty('xmlInstanceFile')) || req.files.xmlInstanceFile==null){
        res.status(400).json({msg: "(Bad Request) data format: {xmlSchemaFile, xmlInstanceFile}"});
    } else {
        var dataset = new Object();

        dataset.name = req.body.datasetName;
        dataset.xmlSchema = fs.readFileSync(req.files.xmlSchemaFile.path,'utf8');
        dataset.xmlInstances = fs.readFileSync(req.files.xmlInstanceFile.path,'utf8');
        dataset.user = req.user.username;
        dataset.type = 'XML';

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "datasets/"+req.user.username,
            body: JSON.stringify(dataset)
        }, function done(err, results) {
            res.status(200).json("success");
        });

    }
};

exports.postJSONDataset = function (req, res, next) {
    if (!(req.body.hasOwnProperty('datasetName')) || req.body.datasetName==null
        || !(req.files.hasOwnProperty('jsonInstanceFile')) || req.files.jsonInstanceFile==null){
        res.status(400).json({msg: "(Bad Request) data format: {datasetName, jsonInstanceFile}"});
    } else {
        var dataset = new Object();

        dataset.name = req.body.datasetName;
        dataset.jsonInstances = fs.readFileSync(req.files.jsonInstanceFile.path,'utf8');
        dataset.user = req.user.username;
        dataset.type = 'JSON';

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "datasets/"+req.user.username,
            body: JSON.stringify(dataset)
        }, function done(err, results) {
            res.status(200).json("success");
        });

    }
};

exports.deleteDataset = function (req, res, next) {
    request.delete(config.METADATA_DATA_LAYER_URL + "datasets/"+(req.params.datasetID)+"/"+req.user.username, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json("ok");
        } else {
            res.status(500).send("Error deleting dataset");
        }
    });
};