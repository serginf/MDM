/**
 * Created by snadal on 18/05/16.
 */
var config = require(__dirname+'/../config'),
    request = require('request');

exports.getLAVMapping = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "lav_mapping/"+req.params.LAVMappingID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving LAV mapping");
        }
    });
};

exports.getAllLAVMappings = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "lav_mapping/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of LAV mappings");
        }
    });
};

exports.postLAVMapping = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null){
        res.status(400).json({msg: "(Bad Request) data format: {name}"});
    } else {
        var objLAVMapping = req.body;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "lav_mapping/",
            body: JSON.stringify(objLAVMapping)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error storing LAV mapping");
            }
        });
    }
};