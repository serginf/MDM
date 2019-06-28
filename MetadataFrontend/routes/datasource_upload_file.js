var formidable = require('formidable'),
    fs = require('fs'),
    config = require(__dirname + '/../config'),
    upload_path = config.FILES_PATH;

exports.uploadFile = function (req, res) {

    var form = new formidable.IncomingForm({uploadDir: upload_path});
    var resultFile = new Object();

    form.on('file', function(field, file) {
        var newFilePath = form.uploadDir + "/"+Date.now() + '-' + file.name
        fs.rename(file.path, newFilePath,
            function (err) {if (err) throw err;});

        resultFile.path = newFilePath;

    });

    form.on('error', function (err) {
        console.log('An error has occured with datasource form upload' + err);
    });

    // Parse the incoming form fields.
    form.parse(req, function (err, fields, files) {
        res.status(200).json(resultFile);
    });
};
