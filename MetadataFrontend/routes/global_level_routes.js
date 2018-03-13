/**artifacts
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');


exports.getAllFeatures = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "global_level/"+encodeURIComponent(req.params.artifactID)+"/features", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json((body));
        } else {
            res.status(500).send("Error retrieving artifact content");
        }
    });
};