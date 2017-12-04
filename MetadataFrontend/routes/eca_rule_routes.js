/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getEcaRule = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule/"+req.params.eca_ruleID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving ECA Rule");
        }
    });
};

exports.getAllEcaRules = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of ECA rules");
        }
    });
};

exports.postEcaRule = function (req, res, next) {
   if (!(req.body.hasOwnProperty('ruleName')) || req.body.ruleName == null
        || !(req.body.hasOwnProperty('pattern')) || req.body.pattern == null
        || !(req.body.hasOwnProperty('condition')) || req.body.condition == null
        || !(req.body.hasOwnProperty('filters')) || req.body.filters == null
        || !(req.body.hasOwnProperty('action')) || req.body.action == null
        || !(req.body.hasOwnProperty('windowTime')) || req.body.windowTime == null
        || !(req.body.hasOwnProperty('windowSize')) || req.body.windowSize == null) {
        res.status(400).json({msg: "(Bad Request) data format: ruleName, pattern, filters, action, windowTime, windowSize}"});
   }
   else {
        var rule = new Object();
        rule.ruleName = req.body.ruleName;
        rule.pattern = req.body.pattern;
        rule.condition = req.body.condition;
        rule.filters = req.body.filters;
        rule.action = req.body.action;
        rule.windowTime = req.body.windowTime;
        rule.windowSize = req.body.windowSize;
        rule.type = "RULES";
        rule.graph = config.DEFAULT_NAMESPACE+"RULE/"+randomstring.generate();

        console.log("Posting "+JSON.stringify(rule));

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "eca_rule/",
            body: JSON.stringify(rule)
        }, function done(error, response, body) {
            console.log("Got response "+error+" - "+response+" - "+body);
            if (!error && response.statusCode == 200) {
                res.status(200).json("ok");
            } else {
                res.status(500).send("Error posting rule");
            }
        });
    }
};

exports.getEcaRuleOperatorTypes = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule_operator_types/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of ECA Rule operator types");
        }
    });
};

exports.getEcaRulePredicateTypes = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule_predicate_types/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of ECA Rule predicate types");
        }
    });
};

exports.getEcaRuleActionTypes = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule_action_types/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of ECA Rule action types");
        }
    });
};

exports.generateConfigFile = function (req, res) {
    request.get(config.METADATA_DATA_LAYER_URL + "eca_rule/" + req.params.ruleName + "/generate_config_file/", function(error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving ECA Rule configuration file");
        }
    })
}