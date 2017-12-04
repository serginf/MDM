/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.postDomainOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('domainOntologyName')) || req.body.domainOntologyName==null
       || !(req.files.hasOwnProperty('domainOntologyFile')) || req.files.domainOntologyFile==null){
        res.status(400).json({msg: "(Bad Request) data format: {domainOntologyName, domainOntologyFile}"});
    } else {
        var graphName = randomstring.generate();
        async.parallel([
                function(callback){
                    var domainOntology = new Object();
                    domainOntology.name = req.body.domainOntologyName;
                    domainOntology.user = req.user.username;
                    domainOntology.type = "DOMAIN_ONTOLOGY";
                    domainOntology.graph = config.DEFAULT_NAMESPACE+graphName;
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+req.user.username,
                        body: JSON.stringify(domainOntology)
                    }, function done(err, results) {
                        callback();
                    });
                },
                function(callback){
                    var RDF =  fs.readFileSync(req.files.domainOntologyFile.path,'utf8');
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(config.DEFAULT_NAMESPACE+graphName)+"/"+req.user.username,
                        body: RDF
                    }, function done(err, results) {
                        callback();
                    });
                }
            ],
            function(err, results){
                res.status(200).json("success");
            });
    }
};