/**
 * Created by Kashif Rabbani
 */
var config = require(__dirname + '/../config'),
    request = require('request'),
    location = require('location-href'),
    store = require('store');


exports.triggerDataSourcesIntegration = function (req, res) {
    console.log("TriggerDataSourcesIntegration");
    console.log('Request at time: ' + new Date() + ' BODY: ' + req.body);

    /* var dt = new Date();
     dt.setSeconds( dt.getSeconds() + 10 );
     console.log("New Time: " + dt);*/
    var temp;
    if (store.get('lastRequestTime') != null) {
        temp = new Date(store.get('lastRequestTime').time);
        console.log("Last Request Saved Time: " + temp);

        temp.setSeconds(temp.getSeconds() + 5);
        console.log("Last Request + 5 Seconds: " + temp);

        store.set('lastRequestTime', {time: new Date()});

    } else {
        store.set('lastRequestTime', {time: new Date()});
    }

    console.log("Printing TEMP outside " + temp);

    if (new Date() < temp) {
        console.log("Aborting duplicate Request");
        res.status(200).send("DUPLICATE");
    } else {
        if (!(req.body.hasOwnProperty('id1')) || req.body.id1 == null ||
            !(req.body.hasOwnProperty('id2')) || req.body.id2 == null) {
            res.status(400).json({msg: "(Bad Request) data format: {id1, id2}"});
        } else {
            var objDataSource = req.body;
            var url = config.BDI_DATA_LAYER_URL + 'schemaIntegration';
            console.log(url);
            //console.log(objDataSource);

            /*setTimeout(function () {
                res.status(200).send("DONE");
                console.log("TimeOut Function");
            }, 10000);*/

            request.post({
                url: url,
                body: JSON.stringify(objDataSource)
            }, function done(error, response, body) {
                if (!error && response.statusCode === 200) {
                    console.log(body);

                    //res.redirect('/');
                    res.status(200).send(body);
                } else {
                    res.status(500).send("Error in the backend");
                }
            });
            //res.status(200).send("DONE");
        }
    }

};

exports.getAlignments = function (req, res, next) {
    req.connection.setTimeout( 1000 * 60 * 10 ); // ten minutes
    console.log("Triggered getAlignments at : " + new Date());
    console.log("Params: " + req.params);
    request.get(config.BDI_DATA_LAYER_URL + "getSchemaAlignments/" + req.params.ds1_id + "/" + req.params.ds2_id, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).send(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving Alignments");
        }
    });
};


exports.acceptAlignment = function (req, res, next) {
    console.log("TriggeredAcceptAlignment");
    console.log(req.body);
    if (!(req.body.hasOwnProperty('p')) || req.body.p == null ||
        !(req.body.hasOwnProperty('s')) || req.body.s == null ||
        !(req.body.hasOwnProperty('confidence')) || req.body.confidence == null ||
     //   !(req.body.hasOwnProperty('lexical_confidence')) || req.body.lexical_confidence == null ||
     //   !(req.body.hasOwnProperty('mapping_direction')) || req.body.mapping_direction == null ||
     //   !(req.body.hasOwnProperty('mapping_type')) || req.body.mapping_type == null ||
      //  !(req.body.hasOwnProperty('structural_confidence')) || req.body.structural_confidence == null ||
        !(req.body.hasOwnProperty('integrated_iri')) || req.body.integrated_iri == null ||
        !(req.body.hasOwnProperty('actionType')) || req.body.actionType == null
    ) {
        res.status(400).json({msg: "(Bad Request) data format: {P, S, O, ID}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL + 'acceptAlignment';
        /*setTimeout(function(){
            res.status(200).send("DONE");
        }, 5000);*/

        request.post({
            url: url,
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode === 200) {
                console.log(body);
                res.status(200).send(body);
            } else {
                res.status(500).send("Error in the backend");
            }
        });
        //res.status(200).send("DONE");
    }
};

exports.finishIntegration = function (req, res, next) {
    console.log("TriggeredFinishIntegration");
    console.log(req.body);
    if (req.body === null) {
        res.status(400).json({msg: "(Bad Request) empty post data while Triggering FinishIntegration"});
    } else {
        var objDataSource = req.body;
        console.log(req.body);
        var url = config.BDI_DATA_LAYER_URL + 'finishIntegration';

        request.post({
            url: url,
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode === 200) {
                console.log(body);
                res.status(200).send(body);
            } else {
                res.status(500).send("Error in the backend");
            }
        });
        //res.status(200).send("DONE");
    }
};
