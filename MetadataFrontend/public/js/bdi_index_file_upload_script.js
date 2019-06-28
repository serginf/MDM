/**
 * Author Kashif-Rabbani
 */
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function () {

    $('#dataSourceForm').on("submit", function (e) {
        e.preventDefault();
        var dataSource = new FormData();
        switch ($('.nav-tabs .active').attr('id')) {

            case "json-tab":
                if ($("#file_path").get(0).files.length === 0) {
                    console.log("jsonfile");
                    return false;
                }
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "json");
                // Get the files from input, create new FormData.
                var files = $('#file_path').get(0).files;

                // Append the files to the formData.
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    dataSource.append('JSON_FILE', file, file.name);
                }
                break;

            case "xml-tab":
                if ($("#xml_path").get(0).files.length === 0) {
                    console.log("xmltab");
                    return false;
                }
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "xml");
                // Get the files from input, create new FormData.
                var filesXML = $('#xml_path').get(0).files;

                // Append the files to the formData.
                for (var x = 0; x < filesXML.length; x++) {
                    var fileXML = filesXML[x];
                    dataSource.append('XML_FILE', fileXML, fileXML.name);
                }

                break;

            case "csv-tab":
                if ($("#csv_path").get(0).files.length === 0) {
                    console.log("csvtab");
                    return false;
                }
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "csv");
                // Get the files from input, create new FormData.
                var filesCSV = $('#xml_path').get(0).files;

                // Append the files to the formData.
                for (var k = 0; k < filesCSV.length; k++) {
                    var fileCSV = filesCSV[k];
                    dataSource.append('CSV_FILE', fileCSV, fileCSV.name);
                }

                break;

            case "sqldatabase-tab":

                if ($("#sql_path").val() === '') {
                    console.log("sqldb");
                    return false;
                }
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "SQL");
                dataSource.append("sql_jdbc", $("#sql_path").val());
                break;
        }
        handler(dataSource);
    });
    handleProgressBar();
});

function handler(dataSource) {
    $.ajax({
        url: '/fileupload',
        method: "POST",
        data: dataSource,
        processData: false,
        contentType: false,
        async: true,
        xhr: function () {
            var xhr = new XMLHttpRequest();
            if (dataSource.get("givenType") !== 'SQL') {
                // Add progress event listener to the upload.
                xhr.upload.addEventListener('progress', function (event) {
                    var progressBar = $('.progress-bar');

                    if (event.lengthComputable) {
                        var percent = (event.loaded / event.total) * 100;
                        progressBar.width(percent + '%');

                        if (percent === 100) {
                            progressBar.removeClass('active');
                        }
                    }
                });
            }
            return xhr;
        }
    }).done(function (data) {
        console.log("Return: " + JSON.stringify(data));
        if (data[0].status === true) {
            parseSource(data);
        } else {
            //TODO handle it, notify the user about the invalid format
            console.log("Invalid format");
        }

    }).fail(function (err) {
        alert("Error " + JSON.stringify(err));
    });
}

function parseSource(data) {
    console.log(JSON.stringify(data));
    toggleModal();
    var clickHandler = function (ee) {
        ee.preventDefault();
        ee.stopImmediatePropagation();
        toggleModal();
        $.ajax({
            type: 'POST',
            data: JSON.stringify(data),
            contentType: 'application/json',
            url: '/triggerExtraction',
            async: true,
            success: function (response) {
                console.log('Success');
                console.log(JSON.stringify(response));
                window.location.href = '/bdi';
            },
            error: function (response) {
                alert('Upload failed ' + response.toString());
                console.log(JSON.stringify(response));
            }
        });
        return false;
    };
    $('#ModalProceedButton').one('click', clickHandler);
}



function toggleModal() {
    $('#confirmationModal').modal('toggle');
}

function handleProgressBar() {
    $('#json-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#xml-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#csv-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#sqldatabase-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#json_pathForm').on('click', function () {
        $('.progress-bar').width('0%');
    });

    $('#xml_pathForm').on('click', function () {
        $('.progress-bar').width('0%');
    });

    $('#sql_jdbcForm').on('click', function () {
        $('.progress-bar').width('0%');
    });
}