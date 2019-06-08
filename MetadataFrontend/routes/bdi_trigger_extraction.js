/**
 * Created by Kashif-Rabbani
 */
var config = require(__dirname + '/../config'),
    request = require('request');

exports.triggerExtraction = function (req, res) {
    req.connection.setTimeout( 1000 * 60 * 10 ); // ten minutes
    console.log(req.body);
    req.body = req.body[0];

    if (!(req.body.hasOwnProperty('givenName')) || req.body.givenName == null ||
        !(req.body.hasOwnProperty('type')) || req.body.type == null ||
        !(req.body.hasOwnProperty('filePath')) || req.body.filePath == null) {
        res.status(400).json({msg: "(Bad Request) data format: {givenName, type, filePath}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL;

        if (req.body.type === 'SQL') {
            url = url + "sql/";
            //sendPostRequest(url, objDataSource, res);
        }

        if (req.body.type === 'xml') {
            url = url + "xml/";
            sendPostRequest(url, objDataSource, res);
        }

        if (req.body.type === 'json') {
            url = url + "jsonSchema/";
            sendPostRequest(url, objDataSource, res);
        }
    }
};


function sendPostRequest(url, objDataSource, res) {
    request.post({
        url: url,
        body: JSON.stringify(objDataSource),
        cache: false
    }, function done(error, response, body) {
        if (!error && response.statusCode === 200) {
            console.log(body);
            res.sendStatus(200);
            //res.end();
        } else {
            res.status(500).send("Error Triggering Parsing.");
        }
    });

}

