var express = require('express');
var router = express.Router();

/* GET atos listing. */
/*router.post('/', function(req, res, next) {
  console.log(req.body);
  res.send('hi im atos');
});
*/

module.exports = function(app, io){
  app.get('/atos', function(req,res) {
    res.render('atos');
  });

  app.post('/atos', function(req, res){
    io.of('/atos').emit('/distinct_users',{message:req.body});
    res.json(true);
  });
};

//module.exports = router;
