/**
 * Created by Kashif-Rabbani
 */
function getIntegratedFileDetails() {
    $.get("/bdiIntegratedDataSources", function (data) {
        console.log(data);
        var i = 1;
        $.each((data), function (key, value) {

            var dataSource = JSON.parse(value);

            var dataSourcesNames = [];

            dataSource.dataSources.forEach(function (ds) {
                //console.log(ds.dataSourceName);
                dataSourcesNames.push(ds.dataSourceName);
            });

            $('#integratedDataSources').find('tbody')
                .append($('<tr>')
                    .append($('<td>').append('<input type="checkbox" class="dataSourceCheckbox" name="dataSource" value = "' + dataSource.dataSourceID + "__" + dataSource.name + '" /> '))
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>').text(dataSource.name))
                    .append($('<td>')
                        .text(dataSourcesNames.join(", "))
                    ).append($('<td>')
                        .text(dataSource.iri)
                    )//.append($('<td>').append($('<a href="/view_data_source?dataSourceID=' + (dataSource.dataSourceID) + '">').append($('<span class="fa fa-search"></span>'))))
                    .append($('<td class="text-center">').append($('<a href="/view/' + (dataSource.integratedVowlJsonFileName) + '&Integrated' + '">').append($('<span class="fa fa-search"></span>')))
                    ).append($('<td class="text-center deleteWrapper">')
                        .append($('<button  value="' + dataSource.dataSourceID + '" class="btn btn-outline-light pop-function delete-button" rel="popover" >').append($('<span color="red" class="fa fa-trash"></span></button>'))))
                );

            ++i;
        });
    });
}

function getParsedFileDetails() {
    $.get("/bdiDataSources", function (data) {
        console.log(data);
        var i = 1;
        $.each((data), function (key, value) {
            var dataSource = JSON.parse(value);
            $('#dataSources').find('tbody')
                .append($('<tr>')
                    .append($('<td>').append('<input type="checkbox" class="dataSourceCheckbox" name="dataSource" value = "' + dataSource.dataSourceID + "__" + dataSource.name + '" /> '))
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(dataSource.name)
                    ).append($('<td>')
                        .text(dataSource.type)
                    ).append($('<td>')
                        .text(dataSource.iri)
                    )//.append($('<td>').append($('<a href="/view_data_source?dataSourceID=' + (dataSource.dataSourceID) + '">').append($('<span class="fa fa-search"></span>'))))
                    .append($('<td class="text-center">').append($('<a href="/view/' + (dataSource.vowlJsonFileName) + '&' + dataSource.name + '">').append($('<span class="fa fa-search"></span>')))
                    )
                    .append($('<td class="text-center deleteWrapper">')
                        .append($('<button  value="' + dataSource.dataSourceID + '" class="btn btn-outline-light pop-function delete-button" rel="popover" >').append($('<span color="red" class="fa fa-trash"></span></button>'))))
                );

            ++i;
        });
    });
}

function handler(dataSource) {
    $.ajax({
        url: '/fileupload',
        method: "POST",
        data: dataSource,
        processData: false,
        contentType: false,
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

$(function () {
    getParsedFileDetails();
    getIntegratedFileDetails();
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
    $('#integrateDataSourcesButton').on("click", function (e) {
        e.preventDefault();
        if (!$("#integrateDataSourcesButton").hasClass('disabled')) {
            console.log("Clicked #integrateDataSourcesButton");

            var dataSources = [];
            $.each($("input[name='dataSource']:checked"), function () {
                dataSources.push($(this).val());
            });
            console.log("Selected data Sources are: " + dataSources.join(", "));
            var object = {};
            object.id1 = dataSources[0].split("__")[0];
            object.id2 = dataSources[1].split("__")[0];
            object.s1Name = dataSources[0].split("__")[1];
            object.s2Name = dataSources[1].split("__")[1];
            console.log(object);

            if (object.id1.includes("INTEGRATED-") && object.id2.includes("INTEGRATED-")) {
                console.log("Integration of Global Graphs not allowed yet.");
            } else {
                window.location.href = '/integration/' + object.id1 + '&' + object.id2 + '&' + object.s1Name + '&' + object.s2Name;
            }


            /*$.ajax({
                url: '/integrateDataSources',
                method: "POST",
                data: object
            }).done(function (data) {
                console.log('Success');
                console.log(data);
                goToAlignmentsView(data);
            }).fail(function (err) {
                alert("Error Integrating sources " + JSON.stringify(err));
            });*/
        }
    });
    handleProgressBar();
});


$(document).ready(function () {
    $(document).ajaxSend(function () {
        $("#overlay").fadeIn(100);
    });

    $(document).ajaxStop(function () {
        $("#overlay").fadeOut(100);
    });

    var popOverSettings = {
        placement: 'top',
        container: 'body',
        html: true,
        selector: '[rel="popover"]',
        trigger: "focus"
        /*content: function () {
            return $(bodyPopOver).html();
        }*/
    };

    $('body').popover(popOverSettings);

    $("body").on('click', '.delete-button', function () {
        var bodyPopOver =
            '<div id="popover-content">\n' +
            '    <p>Are you sure? <br></p> <button value="' + $(this).val() + '" class="btn btn-sm btn-danger delete-confirmed" id="DeleteButtonSaysYes">Yes</button>\n' +
            '</div>';

        if ($('.popover').hasClass('in')) {
            $(this).popover('hide');
        } else {
            $(this).attr('data-content', bodyPopOver);
            $(this).popover('show');
        }
    });

    $("body").on('click', '.delete-confirmed', function () {
        console.log($(this).val());
        $.get("/deleteDataSource/" + $(this).val(), function (data) {
            console.log(data);
            if (data === "DELETED") {
                window.location.href = "/bdi";
            }
        });
    });


    $("body").on('change', 'input[type=checkbox]', function () {
        //$('[data-toggle="popover"]').popover();
        var checkedCheckedBoxes = $('input[type=checkbox]:checked').length;
        var integrateButton = $("#integrateDataSourcesButton");
        if (checkedCheckedBoxes > 2) {
            $(this).prop('checked', false);
            console.log("You can maximum select two sources at a time.");
        }

        if (checkedCheckedBoxes === 2) {
            integrateButton.removeClass("disabled");
            integrateButton.prop('disabled', false);
        }
        if (checkedCheckedBoxes < 2) {
            integrateButton.addClass("disabled");
            integrateButton.prop('disabled', true);
        }
        /*if (!integrateButton.hasClass('disabled')) {

        }*/
    });


});

/*function goToAlignmentsView(data) {
    //console.log("Inside Alignments" + data + '/integration/:ids_id&ds1_id&:ds2_id&:ds1_name&:ds2_name&:align_iri');
    var d = JSON.parse(data);
    // var url = '/integration/' + d.integratedDataSourceID + '&' + d.dataSourceID1 + '&' + d.dataSourceID2 + '&' +
    //     d.dataSource1Name + '&' + d.dataSource2Name + '&' + d.alignmentsIRI + '&' + d.integratedIRI;
    var url = '/integration/' + d.integratedDataSourceID + '&' +
        d.dataSource1Name + '&' + d.dataSource2Name;
    console.log(url);

    $("#overlay").fadeOut(300);
    window.location.href = url;
}*/
