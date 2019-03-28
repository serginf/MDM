/**
 * Created by serginadalfrancesch on 24/2/15.
 */

var passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy,
    bcrypt = require('bcryptjs'),
    request = require('request'),
    config = require(__dirname+'/config');


passport.use(new LocalStrategy(
    function(username, password, done) {
        request.get(config.METADATA_DATA_LAYER_URL+"users/findOne/"+username, function (error, response, body) {
            if (error || response.statusCode == 404) { return done(null, false, {message: 'Invalid username or password'}); }
            var user = JSON.parse(body);
            if (!user) { return done(null, false, {message: 'Invalid username or password'}); }
            bcrypt.compare(password, user.password, function (err, res) {
                if (!res) return done(null, false, {message: 'Invalid username or password'});
                return done(null, user);
            })
        });
    }
));

passport.serializeUser(function(user, done) {
    done(null, user);
});

passport.deserializeUser(function(user, done) {
    done(null, user);
});