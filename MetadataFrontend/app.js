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
var artifact_routes = require(__dirname+'/routes/artifact_routes');
var global_graph_routes = require(__dirname+'/routes/global_graph_routes');
var data_source_routes = require(__dirname+'/routes/data_source_routes');
var wrapper_routes = require(__dirname+'/routes/wrapper_routes');

var global_level_routes = require(__dirname+'/routes/global_level_routes');
var bdi_ontology_routes = require(__dirname+'/routes/bdi_ontology_routes');

var source_level_routes = require(__dirname+'/routes/source_level_routes');
var release_routes = require(__dirname+'/routes/release_routes');
var admin_routes = require(__dirname+'/routes/admin_routes');

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

/********** Generic Artifact resource *****************************************************/

app.get('/artifacts/:artifactType', artifact_routes.getArtifacts);
app.get('/artifacts/:artifactType/:artifactID', artifact_routes.getArtifact);
app.get('/artifacts/:artifactType/:artifactID/content', artifact_routes.getArtifactContent);
app.get('/artifacts/:artifactType/:artifactID/graphical', artifact_routes.getArtifactGraphical);
app.delete('/artifacts/:artifactType/:artifactID', artifact_routes.deleteArtifact);
//app.post('/artifacts/:artifactType/:artifactID/triple', artifact_routes.postTriple);
//app.post('/artifacts/:artifactType/:artifactID/graphicalGraph', artifact_routes.postGraphicalGraph);

/********** Global Graph resource ********************************************************/

app.get('/globalGraph/', global_graph_routes.getAllGlobalGraphs);
app.get('/globalGraph/:globalGraphID', global_graph_routes.getGlobalGraph);
app.get('/globalGraph/namedGraph/:namedGraph', global_graph_routes.getGlobalGraphFromNamedGraph);
app.post('/globalGraph', global_graph_routes.postGlobalGraph);
app.post('/globalGraph/:namedGraph/triple', global_graph_routes.postTriple);
app.post('/globalGraph/:globalGraphID:/graphicalGraph', global_graph_routes.postGraphicalGraph);

/********** Data Source resource ********************************************************/

app.get('/dataSource/', data_source_routes.getAllDataSources);
app.get('/dataSource/:dataSourceID', data_source_routes.getDataSource);
app.post('/dataSource', data_source_routes.postDataSource);

/********** Wrapper resource ********************************************************/

app.get('/wrapper/', wrapper_routes.getAllWrappers);
app.get('/wrapper/:wrapperID', wrapper_routes.getWrapper);
app.post('/wrapper', wrapper_routes.postWrapper);
app.get('/wrapper/preview/:dataSourceID/:query', wrapper_routes.previewWrapper);


//app.post('/bdi_ontology/sparQLQuery', bdi_ontology_routes.postSparQLQuery);


/********** BDI Ontology resource ********************************************************/

app.get('/bdi_ontology/:bdi_ontologyID', bdi_ontology_routes.getBDIOntology);
app.get('/bdi_ontology/graph/:graph', bdi_ontology_routes.getBDIOntologyFromGraph);
app.get('/bdi_ontology', bdi_ontology_routes.getAllBDIOntologies);
app.post('/bdi_ontology', bdi_ontology_routes.postBDIOntology);

app.get('/bdi_ontology_generation_strategies', bdi_ontology_routes.getGenerationStrategies);

app.post('/bdi_ontology/sparQLQuery', bdi_ontology_routes.postSparQLQuery);

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

/********** Admin resource *************************************************************/

app.get('/admin/deleteAll', admin_routes.deleteAll);

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

/********** Global graph section ***************************************************************/

app.get('/new_global_graph', checkAuthenticated, function(req,res) {
    res.render('new_global_graph', {user:req.session.passport.user});
});

app.get('/manage_global_graphs', checkAuthenticated, function(req,res) {
    res.render('manage_global_graphs', {user:req.session.passport.user});
});

app.get('/view_global_graph', checkAuthenticated, function(req,res) {
    res.render('view_global_graph', {user:req.session.passport.user});
});

app.get('/edit_global_graph', checkAuthenticated, function(req,res) {
    res.render('edit_global_graph', {user:req.session.passport.user});
});

/********** Data Source section ***************************************************************/

app.get('/new_data_source', checkAuthenticated, function(req,res) {
    res.render('new_data_source', {user:req.session.passport.user});
});

app.get('/manage_data_sources', checkAuthenticated, function(req,res) {
    res.render('manage_data_sources', {user:req.session.passport.user});
});

app.get('/view_data_source', checkAuthenticated, function(req,res) {
    res.render('view_data_source', {user:req.session.passport.user});
});

/********** Wrapper section ***************************************************************/

app.get('/new_wrapper', checkAuthenticated, function(req,res) {
    res.render('new_wrapper', {user:req.session.passport.user});
});

app.get('/manage_wrappers', checkAuthenticated, function(req,res) {
    res.render('manage_wrappers', {user:req.session.passport.user});
});

app.get('/view_wrapper', checkAuthenticated, function(req,res) {
    res.render('view_wrapper', {user:req.session.passport.user});
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


/******* Queries section **************************************************************/

app.get('/view_queries', checkAuthenticated, function (req, res) {
   res.render('view_queries', {user:req.session.passport.user});
});

/**********************************   END   ********************************************/

function checkAuthenticated(req, res, next) {
    if (req.isAuthenticated()) { return next(); }
    res.redirect('/login');
}

module.exports = app;
