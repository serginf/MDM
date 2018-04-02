/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.deleteAll = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "admin/deleteAll/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json((body));
        } else {
            res.status(500).send("Error");
        }
    });
};

exports.demoPrepare = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "admin/demoPrepare/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json((body));
        } else {
            res.status(500).send("Error");
        }
    });
};
