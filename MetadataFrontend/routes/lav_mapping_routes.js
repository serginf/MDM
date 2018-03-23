/**
 * Created by snadal on 18/05/16.
 */
var config = require(__dirname+'/../config'),
    request = require('request');

exports.getLAVMapping = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "LAVMapping/"+req.params.LAVMappingID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving LAV mapping");
        }
    });
};

exports.getAllLAVMappings = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "LAVMapping/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of LAV mappings");
        }
    });
};

exports.postLAVMappingSameAs = function (req, res, next) {
    if (!(req.body.hasOwnProperty('wrapperID')) || req.body.wrapperID==null ||
        !(req.body.hasOwnProperty('globalGraphID')) || req.body.globalGraphID==null ||
        !(req.body.hasOwnProperty('sameAs')) || req.body.sameAs==null){
        res.status(400).json({msg: "(Bad Request) data format: {wrapperID,globalGraphID,sameAs}"});
    } else {
        var objLAVMappingMapsTo = req.body;
        request.post({
            url: config.METADATA_DATA_LAYER_URL + "LAVMapping/sameAs",
            body: JSON.stringify(objLAVMappingMapsTo)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error storing LAV mapping");
            }
        });
    }
};

exports.postLAVMappingSubgraph = function (req, res, next) {
    if (!(req.body.hasOwnProperty('selection')) || req.body.selection==null ||
        !(req.body.hasOwnProperty('LAVMappingID')) || req.body.LAVMappingID==null){
        res.status(400).json({msg: "(Bad Request) data format: {selection,LAVMappingID}"});
    } else {
        var objLAVMappingMapsTo = req.body;
        request.post({
            url: config.METADATA_DATA_LAYER_URL + "LAVMapping/subgraph",
            body: JSON.stringify(objLAVMappingMapsTo)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error storing LAV mapping");
            }
        });
    }
};