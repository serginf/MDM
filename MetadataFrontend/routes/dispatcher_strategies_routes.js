/**
 * Created by snadal on 18/05/16.
 */
var config = require(__dirname+'/../config'),
    request = require('request');

exports.getDispatcherStrategiesTypes = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "dispatcher_strategies_types/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of Dispatcher Strategy types");
        }
    });
};