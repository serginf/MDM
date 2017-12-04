var config = require(__dirname+'/config');
var port = config.PORT;
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var methodOverride = require('method-override');
var session = require('express-session');
var app = express();
var io = require('socket.io').listen(app.listen(port));
var flash = require('express-flash');
var multer = require('multer');
var upload = multer({ dest: config.FILES_PATH });
var passport = require('passport');
var fs = require('fs');

/*****************************************************************************************/
/*****************************************************************************************/
/*          Routing files                                                                */
/*****************************************************************************************/
/*****************************************************************************************/

var user_routes = require(__dirname+'/routes/user_routes');
var dataset_routes = require(__dirname+'/routes/dataset_routes');
var artifact_routes = require(__dirname+'/routes/artifact_routes');
var global_level_routes = require(__dirname+'/routes/global_level_routes');
var bdi_ontology_routes = require(__dirname+'/routes/bdi_ontology_routes');
var source_level_routes = require(__dirname+'/routes/source_level_routes');
var release_routes = require(__dirname+'/routes/release_routes');
var statistical_analysis_model_routes = require(__dirname+'/routes/statistical_analysis_model_routes');
var dispatcher_strategies_routes = require(__dirname+'/routes/dispatcher_strategies_routes');
var eca_rule_routes = require(__dirname+'/routes/eca_rule_routes');
var admin_routes = require(__dirname+'/routes/admin_routes');
var files_routes = require(__dirname+'/routes/files_routes');
var classification_routes = require(__dirname+'/routes/classification_routes');

/*****************************************************************************************/
/*****************************************************************************************/
/*          Server configuration                                                         */
/*****************************************************************************************/
/*****************************************************************************************/
// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');
app.use(logger('dev'));
app.use(methodOverride());
app.use(session({ secret: 'uwotm8', proxy: true, resave: true, saveUninitialized: true }));
app.use(bodyParser.urlencoded({extended: true}));
app.use(cookieParser());
app.use(bodyParser.json());                          // parse application/json
app.use(bodyParser.urlencoded({ extended: true }));  // parse application/x-www-form-urlencoded
//app.use(multer());                                   // parse multipart/form-data
app.use(express.static(path.join(__dirname, 'public')));
app.use(bodyParser.raw({uploadDir:config.FILES_PATH}));

app.use(flash());
app.use(passport.initialize());
app.use(passport.session());
//app.use(multer({ dest: './uploads/'}));

//Sergi: Avoid cache and 304 errors
app.disable('etag');

require('./auth');

/*****************************************************************************************/
/*****************************************************************************************/
/*          Resources                                                                    */
/*****************************************************************************************/
/*****************************************************************************************/


/********** User resource ****************************************************************/

app.post('/users', user_routes.postUser);

app.post('/login', passport.authenticate('local', {
    successRedirect: '/',
    failureRedirect: '/login',
    failureFlash: true
}));

app.get('/logout', function(req, res){
    req.logout();
    res.redirect('/');
});


/********** Dataset resource *************************************************************/

app.get('/datasets', dataset_routes.getDatasets);
app.get('/datasets/:datasetID', dataset_routes.getDataset);
app.post('/datasets/xml', dataset_routes.postXMLDataset);
app.post('/datasets/json', dataset_routes.postJSONDataset);
//app.post('/datasets/relational', dataset_routes.postRelationalDataset);
app.delete('/datasets/:datasetID', dataset_routes.deleteDataset);

/********** Generic Artifact resource *****************************************************/

app.get('/artifacts/:artifactType', artifact_routes.getArtifacts);
app.get('/artifacts/:artifactType/:artifactID', artifact_routes.getArtifact);
app.get('/artifacts/:artifactType/:artifactID/content', artifact_routes.getArtifactContent);
app.get('/artifacts/:artifactType/:artifactID/graphical', artifact_routes.getArtifactGraphical);
app.delete('/artifacts/:artifactType/:artifactID', artifact_routes.deleteArtifact);
app.post('/artifacts/:artifactType/:artifactID/triple', artifact_routes.postTriple);
app.post('/artifacts/:artifactType/:artifactID/graphicalGraph', artifact_routes.postGraphicalGraph);

/********** BDI Ontology resource ********************************************************/

app.get('/bdi_ontology/:bdi_ontologyID', bdi_ontology_routes.getBDIOntology);
app.get('/bdi_ontology/graph/:graph', bdi_ontology_routes.getBDIOntologyFromGraph);
app.get('/bdi_ontology', bdi_ontology_routes.getAllBDIOntologies);
app.post('/bdi_ontology', bdi_ontology_routes.postBDIOntology);

app.get('/bdi_ontology_generation_strategies', bdi_ontology_routes.getGenerationStrategies);

/********** Global Level resource ********************************************************/

app.get('/global_level/:artifactID/features', global_level_routes.getAllFeatures);

//app.post('/globalLevel', global_level_routes.postGlobalLevel);

/********** Source Level resource ********************************************************/

app.post('/sourceLevel', source_level_routes.postSourceLevel);

/********** Release resource *************************************************************/

app.get('/release/:releaseID/attributes', release_routes.getReleaseAttributes);
app.get('/release/:releaseID', release_routes.getRelease);
app.get('/release', release_routes.getAllReleases);
app.post('/release', release_routes.postRelease);

/********** Statistical Analysis Model resource ******************************************/

app.get('/statistical_analysis_model/:statistical_analysis_modelID', statistical_analysis_model_routes.getStatisticalAnalysisModel);
app.get('/statistical_analysis_model', statistical_analysis_model_routes.getAllStatisticalAnalysisModels);
app.post('/statistical_analysis_model/', statistical_analysis_model_routes.postStatisticalAnalysisModel);

app.get('/statistical_analysis_model_types', statistical_analysis_model_routes.getStatisticalAnalysisModelTypes);

/********** Dispatcher Strategies resource ***********************************************/

app.get('/dispatcher_strategies_types', dispatcher_strategies_routes.getDispatcherStrategiesTypes);

/********** ECA Rule resource *************************************************************/

app.get('/eca_rule/:eca_ruleID', eca_rule_routes.getEcaRule);
app.get('/eca_rule', eca_rule_routes.getAllEcaRules);
app.post('/eca_rule', eca_rule_routes.postEcaRule);
app.get('/eca_rule_operator_types', eca_rule_routes.getEcaRuleOperatorTypes);
app.get('/eca_rule_predicate_types', eca_rule_routes.getEcaRulePredicateTypes);
app.get('/eca_rule_action_types', eca_rule_routes.getEcaRuleActionTypes);
app.get('/eca_rule/:ruleName/generate_config_file', eca_rule_routes.generateConfigFile);

/**********************************   Files   ********************************************/

//app.post('/files', files_routes.postFile);
app.post('/files', upload.single('file'), function (req, res, next) {
    if (req.file==null){
        res.status(400).json({msg: "(Bad Request) data format: {path, file}"});
    } else {
        var tmp_path = req.file.path;
        var target_path = config.FILES_PATH + req.file.originalname;
        fs.rename(tmp_path, target_path, function(err) {
            if (err) throw err;
            fs.unlink(tmp_path, function() {
                if (err) throw err;
                res.send('File uploaded to: ' + target_path);
            });
        });
    }
})


/****************************   Feedback classification  *********************************/

app.post('/classify/feedback', classification_routes.classifyFeedback);


/********** Admin resource *************************************************************/
app.get('/admin/deleteAll', admin_routes.deleteAll);


/********** Websocket messages ***********************************************************/
// Raw data coming from the Kafka topics
app.post('/raw_data', function(req, res){
    io.of('/raw_data').emit('/raw_data',{message:JSON.stringify(req.body)});
    res.json(true);
});

// General statistics per release
app.post('/events_in_last_5_min', function(req, res){
    io.of('/events_in_last_5_min').emit('/events_in_last_5_min',{message:JSON.stringify(req.body)});
    res.json(true);
});

// Data source statistics
app.post('/socket_data_source_statistics', function(req, res){
    io.of('/socket_data_source_statistics').emit('/socket_data_source_statistics',{message:(JSON.stringify(req.body))});
    res.json(true);
});

/*****************************************************************************************/
/*****************************************************************************************/
/*          Frontend Pages                                                               */
/*****************************************************************************************/
/*****************************************************************************************/

app.get('/', checkAuthenticated, function(req,res) {
    res.render('index', {user:req.session.passport.user});
});

app.get('/login', function (req, res) {
    res.setHeader('Last-Modified', (new Date()).toUTCString());
    res.render('login', {message: req.flash('error')});
});

app.get('/registration', function(req, res) {
    res.setHeader('Last-Modified', (new Date()).toUTCString());
    res.render('register_user');
});

/********** Releases section ***************************************************************/

app.get('/new_release', checkAuthenticated, function(req,res) {
    res.render('new_release', {user:req.session.passport.user});
});

app.get('/manage_releases', checkAuthenticated, function(req,res) {
    res.render('manage_releases', {user:req.session.passport.user});
});

app.get('/view_release', checkAuthenticated, function(req,res) {
    res.render('view_release', {user:req.session.passport.user});
});

/********** BDI Ontology section ***********************************************************/

app.get('/new_bdi_ontology', checkAuthenticated, function(req,res) {
    res.render('new_bdi_ontology', {user:req.session.passport.user});
});

app.get('/manage_bdi_ontologies', checkAuthenticated, function(req,res) {
    res.render('manage_bdi_ontologies', {user:req.session.passport.user});
});

app.get('/view_bdi_ontology', checkAuthenticated, function(req,res) {
    res.render('view_bdi_ontology', {user:req.session.passport.user});
});


/********** Domain Ontology section ********************************************************/

/*app.get('/upload_domain_ontology', checkAuthenticated, function(req,res) {
    res.render('upload_domain_ontology', {user:req.session.passport.user});
});

app.get('/manage_domain_ontologies', checkAuthenticated, function(req,res) {
    res.render('manage_domain_ontologies', {user:req.session.passport.user});
});

app.get('/view_domain_ontology', checkAuthenticated, function(req,res) {
    res.render('view_domain_ontology', {user:req.session.passport.user});
});*/

/********** Global Level section *******************************************************/
/*
app.get('/new_global_level', checkAuthenticated, function(req,res) {
    res.render('new_global_level', {user:req.session.passport.user});
});

app.get('/manage_global_levels', checkAuthenticated, function(req,res) {
    res.render('manage_global_levels', {user:req.session.passport.user});
});
*/
app.get('/view_global_level', checkAuthenticated, function(req,res) {
    res.render('view_global_level', {user:req.session.passport.user});
});

/********** Source Level section ******************************************************/
/*
app.get('/new_source_level', checkAuthenticated, function(req,res) {
    res.render('new_source_level', {user:req.session.passport.user});
});

app.get('/manage_source_levels', checkAuthenticated, function(req,res) {
    res.render('manage_source_levels', {user:req.session.passport.user});
});*/

app.get('/view_source_level', checkAuthenticated, function(req,res) {
    res.render('view_source_level', {user:req.session.passport.user});
});

/********** Reference Dataset section ********************************************************/

app.get('/new_dataset', checkAuthenticated, function(req,res) {
    res.render('new_dataset', {user:req.session.passport.user});
});

app.get('/manage_datasets', checkAuthenticated, function(req,res) {
    res.render('manage_datasets', {user:req.session.passport.user});
});

app.get('/view_dataset', checkAuthenticated, function(req,res) {
    res.render('view_dataset', {user:req.session.passport.user});
});


/********** Data Feed section ************************************************************/

app.get('/live_data_feeds', checkAuthenticated, function(req,res) {
    res.render('live_data_feeds', {user:req.session.passport.user});
});

app.get('/live_data_feed', checkAuthenticated, function(req,res) {
    res.render('live_data_feed', {user:req.session.passport.user});
});

/********** Statistics section ***********************************************************/

app.get('/general_statistics', checkAuthenticated, function(req,res) {
    res.render('general_statistics', {user:req.session.passport.user});
});

app.get('/data_source_statistics_wrapper', checkAuthenticated, function(req,res) {
    res.render('data_source_statistics_wrapper', {user:req.session.passport.user});
});

app.get('/data_source_statistics', checkAuthenticated, function(req,res) {
    res.render('data_source_statistics', {user:req.session.passport.user});
});


/******* Statistical Analysis Model section **********************************************/

app.get('/new_statistical_analysis_model', checkAuthenticated, function(req,res) {
    res.render('new_statistical_analysis_model', {user:req.session.passport.user});
});

app.get('/manage_statistical_analysis_models', checkAuthenticated, function(req,res) {
    res.render('manage_statistical_analysis_models', {user:req.session.passport.user});
});

app.get('/view_statistical_analysis_model', checkAuthenticated, function(req,res) {
    res.render('view_statistical_analysis_model', {user:req.session.passport.user});
});

/******* ECA Rule section **************************************************************/

app.get('/new_eca_rule', checkAuthenticated, function(req,res) {
    res.render('new_eca_rule', {user:req.session.passport.user});
});

app.get('/manage_eca_rules', checkAuthenticated, function(req,res) {
    res.render('manage_eca_rules', {user:req.session.passport.user});
});

app.get('/view_eca_rules', checkAuthenticated, function(req,res) {
    res.render('view_eca_rules', {user:req.session.passport.user});
});

app.get('/view_eca_rule', checkAuthenticated, function(req,res) {
    res.render('view_eca_rule', {user:req.session.passport.user});
});

/**********************************   END   ********************************************/

function checkAuthenticated(req, res, next) {
    if (req.isAuthenticated()) { return next(); }
    res.redirect('/login');
}

module.exports = app;
