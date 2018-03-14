/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getWrapper = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "wrapper/"+req.params.wrapperID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving wrapper");
        }
    });
};

exports.getAllWrappers = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "wrapper/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of wrappers");
        }
    });
};

exports.postWrapper = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null ||
        !(req.body.hasOwnProperty('dataSourceID')) || req.body.dataSourceID==null ||
        !(req.body.hasOwnProperty('attributes')) || req.body.attributes==null||
        !(req.body.hasOwnProperty('query')) || req.body.query==null){
        res.status(400).json({msg: "(Bad Request) data format: {name, dataSourceID, attributes, query}"});
    } else {
        var objWrapper = req.body;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "wrapper/",
            body: JSON.stringify(objWrapper)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error storing wrapper");
            }
        });
    }
};

exports.previewWrapper = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "wrapper/preview/"+encodeURIComponent(req.params.dataSourceID)
            +"/"+encodeURIComponent(req.params.query), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error previewing query");
        }
    });
};
