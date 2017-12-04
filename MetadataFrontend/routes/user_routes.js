/**
 * Created by snadal on 17/05/16.
 */

var request = require('request'),
    config = require(__dirname+'/../config'),
    bcrypt = require('bcryptjs');

exports.postUser = function (req, res, next) {
    if (!(req.body.hasOwnProperty('username')) || req.body.username==null
        || !(req.body.hasOwnProperty('password')) || req.body.password==null){
        res.status(400).json({msg: "(Bad Request) data format: {username, password}"});
        if (next) next(res);
    } else {
        var user_object = req.body;
        user_object.created_at = Date.now();
        user_object.last_modified = Date.now();
        console.log(JSON.stringify(user_object));
        bcrypt.genSalt(10, function(err, salt) {
            bcrypt.hash(user_object.password, salt, function(err, hash) {
                user_object.password = hash;
                request.post({
                    url: config.METADATA_DATA_LAYER_URL + "users",
                    body: JSON.stringify(req.body)
                }, function (error, response, body) {
                    console.log(error + " - " + response + " - " + body);
                    res.status(200).json(body);
                });
            });
        });
    }
};
