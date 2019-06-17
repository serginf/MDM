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
var graph_routes = require(__dirname+'/routes/graph_routes');
var global_graph_routes = require(__dirname+'/routes/global_graph_routes');
var data_source_routes = require(__dirname+'/routes/data_source_routes');
var wrapper_routes = require(__dirname+'/routes/wrapper_routes');
var lav_mapping_routes = require(__dirname+'/routes/lav_mapping_routes');
var omq_routes = require(__dirname+'/routes/omq_routes');



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

/********** Generic RDF graph resource *****************************************************/

app.get('/graph/:iri', graph_routes.getGraph);
app.get('/graph/:type/:iri/graphical', graph_routes.getGraphGraphical);
app.post('/graph', graph_routes.postGraph);
app.post('/graph/:iri/triple', graph_routes.postTriple);
//app.post('/artifacts/:artifactType/:artifactID/triple', artifact_routes.postTriple);
//app.post('/artifacts/:artifactType/:artifactID/graphicalGraph', artifact_routes.postGraphicalGraph);

/********** Global Graph resource ********************************************************/

app.get('/globalGraph/', global_graph_routes.getAllGlobalGraphs);
app.get('/globalGraph/:globalGraphID', global_graph_routes.getGlobalGraph);
app.get('/globalGraph/namedGraph/:namedGraph', global_graph_routes.getGlobalGraphFromNamedGraph);
app.get('/globalGraph/:namedGraph/features', global_graph_routes.getFeaturesForGraph);
app.post('/globalGraph', global_graph_routes.postGlobalGraph);
//app.post('/globalGraph/:namedGraph/triple', global_graph_routes.postTriple);
app.post('/globalGraph/:globalGraphID/graphicalGraph', global_graph_routes.postGraphicalGraph);
app.post('/globalGraph/:namedGraph/TTL', global_graph_routes.postTTL);

app.delete('/globalGraph/:namedGraph/node', global_graph_routes.deleteNode);
app.delete('/globalGraph/:namedGraph/property', global_graph_routes.deleteProperty);
/********** Data Source resource ********************************************************/

app.get('/dataSource/', data_source_routes.getAllDataSources);
app.get('/dataSource/:dataSourceID', data_source_routes.getDataSource);
app.post('/dataSource', data_source_routes.postDataSource);

/********** Wrapper resource ********************************************************/

app.get('/wrapper/', wrapper_routes.getAllWrappers);
app.get('/wrapper/:wrapperID', wrapper_routes.getWrapper);
app.post('/wrapper', wrapper_routes.postWrapper);
app.post('/wrapper/inferSchema/', wrapper_routes.inferSchema);
app.post('/wrapper/preview/', wrapper_routes.previewWrapper);
app.get('/wrapper/:namedGraph/attributes', wrapper_routes.getAttributesForGraph);
app.delete('/wrapper/:wrapperID', wrapper_routes.deleteWrapper);

/********** LAV Mapping resource ********************************************************/

app.get('/LAVMapping/', lav_mapping_routes.getAllLAVMappings);
app.get('/LAVMapping/:LAVMappingID', lav_mapping_routes.getLAVMapping);
app.post('/LAVMapping/sameAs', lav_mapping_routes.postLAVMappingSameAs);
app.post('/LAVMapping/subgraph', lav_mapping_routes.postLAVMappingSubgraph);
app.delete('/LAVMapping/:LAVMappingID', lav_mapping_routes.deleteLAVMapping);
/********** OMQ resource ********************************************************/

app.post('/OMQ/fromGraphicalToSPARQL', omq_routes.postFromGraphicalToSPARQL);
app.post('/OMQ/fromSPARQLtoRA', omq_routes.postFromSPARQLToGraphical);
app.post('/OMQ/fromSQLtoDATA', omq_routes.postFromSQLToData);


/********** Admin resource *************************************************************/

app.get('/admin/deleteAll', admin_routes.deleteAll);
app.get('/admin/demoPrepare', admin_routes.demoPrepare);


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

app.get('/view_source_graph', checkAuthenticated, function(req,res) {
    res.render('view_source_graph', {user:req.session.passport.user});
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

/********** LAV mapping section ************************************************************/

app.get('/new_lav_mapping', checkAuthenticated, function(req,res) {
    res.render('new_lav_mapping', {user:req.session.passport.user});
});

app.get('/manage_lav_mappings', checkAuthenticated, function(req,res) {
    res.render('manage_lav_mappings', {user:req.session.passport.user});
});

app.get('/view_lav_mapping_sameAs', checkAuthenticated, function(req,res) {
    res.render('view_lav_mapping_sameAs', {user:req.session.passport.user});
});

app.get('/view_lav_mapping_subgraph', checkAuthenticated, function(req,res) {
    res.render('view_lav_mapping_subgraph', {user:req.session.passport.user});
});

/********** OMQ section ************************************************************/

app.get('/pose_omq', checkAuthenticated, function(req,res) {
    res.render('pose_omq', {user:req.session.passport.user});
});


/**********************************   END   ********************************************/

function checkAuthenticated(req, res, next) {
    if (req.isAuthenticated()) { return next(); }
    res.redirect('/login');
}

module.exports = app;
