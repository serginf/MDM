/**
 * Created by snadal on 17/05/16.
 */

var request = require('request'),
    config = require(__dirname+'/../config'),
    fs = require('fs');


exports.postFile = function (req, res, next) {
    if (req.files.file==null){
        res.status(400).json({msg: "(Bad Request) data format: {file}"});
    } else {
        var tmp_path = req.files.file.path;
        var target_path = config.FILES_PATH + req.files.file.originalname;
        fs.rename(tmp_path, target_path, function(err) {
            if (err) throw err;
            fs.unlink(tmp_path, function() {
                if (err) throw err;
                res.send('File uploaded to: ' + target_path);
            });
        });
    }
};
