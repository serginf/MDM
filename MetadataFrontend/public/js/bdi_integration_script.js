var integratedDataInfoObject = {};
var alignmentsInfo = [];

var bdi_integrate_url = window.location.href;
//var url_prefix = bdi_integrate_url.split('/integration/')[0];
var url_suffix = bdi_integrate_url.split('/integration/')[1];

var params = url_suffix.split('&');
// console.log(params);
var ds1_id = params[0];
var ds2_id = params[1];
var integrationType;
var selected_ds1 = "";
var selected_ds2 = "";
var url_iframe= "/bdi_graph_alignment";

function integrationTypeChecker() {

    if (ds1_id.includes("INTEGRATED-") && ds2_id.includes("INTEGRATED-")) {
        console.log("Integration of a global graph vs global graph.");
        integrationType = "GLOBAL-vs-GLOBAL";
    } else if (ds1_id.includes("INTEGRATED-")) {
        console.log("Integration of a Global graph vs Local graph.");
        integrationType = "GLOBAL-vs-LOCAL";
        $('#graphIframe1').attr('src',url_iframe+"?IntegratedDataSourceID="+ds1_id);
        $('#graphIframe2').attr('src',url_iframe+"?dataSourceID="+ds2_id);
    } else {
        console.log("Integration between local graphs");
        integrationType = "LOCAL-vs-LOCAL";
        $('#graphIframe1').attr('src',url_iframe+"?dataSourceID="+ds1_id);
        $('#graphIframe2').attr('src',url_iframe+"?dataSourceID="+ds2_id);
    }
}

console.log(params);

/*function getIntegratedDataSourceInfo() {
    $("#overlay").fadeIn(100);
    $.get('/bdiIntegratedDataSources/' + ids_id, function (data) {
        //$("#overlay").fadeOut(100);
        console.log(data);
        integratedDataInfoObject = data;
        var dataSources = integratedDataInfoObject.dataSources;
        var constructedAlignmentsIRI;
        var allDataSources = {};
        dataSources.forEach(function (val) {
           console.log(val);
        });
        //getAlignments(data.alignmentsIRI);
    });
}*/

function removeURI(iri) {
    return iri.split("http://www.BDIOntology.com/")[1];
}

function ifCollectionClasses(a, b) {
    if ((a.includes("_Collection") && !b.includes("_Collection")) || (!a.includes("_Collection") && b.includes("_Collection"))) {
        return true;
    } else {
        return false;
    }
}

$(function () {
    $("#infoButton").popover('show');
    showAlert(false);
    window.addEventListener('clickEle_msg', function(e) {

        if(e.detail.id == ds1_id ){
            if(e.detail.isSelected){
                if(selected_ds2 == e.detail.type || selected_ds2 == ""){
                    $("#selectElement1").val(e.detail.iri);
                    $("#typeElement1").text(e.detail.type);
                    selected_ds1 = e.detail.type;
                    showAlert(false);
                }else{
                    showAlert(true);
                }
            }else {
                $("#selectElement1").val("");
                $("#typeElement1").text("");
                selected_ds1 = "";
            }
        }else{
            if(e.detail.isSelected) {
                if(selected_ds1 == e.detail.type || selected_ds1 == ""){
                    $("#selectElement2").val(e.detail.iri);
                    $("#typeElement2").text(e.detail.type);
                    selected_ds2 = e.detail.type;
                    showAlert(false);
                }else{
                    showAlert(true);
                }

            }else{
                $("#selectElement2").val("");
                $("#typeElement2").text("");
                selected_ds2 = "";
            }

        }

        console.log("received msg from id: "+e.detail.id+ " element selected "+e.detail.isSelected+" is "+e.detail.type+" selected: "+e.detail.iri);
    }, false);
});

function showAlert(flag) {
    if(flag)
        $("#alertSelectElement").show();
    else
        $("#alertSelectElement").hide();
}

function getAlignments() {
    $("#overlay").fadeIn(100);
    console.log("Requesting bdiAlignments");
    $.get('/bdiAlignments/' + ds1_id + '&' + ds2_id, function (data) {
        //console.log(data);
        $("#overlay").fadeOut(100);
        var i = 1;
        data.forEach(function (val) {
            var graphBadgeA = "Source Graph";
            var graphBadgeB = "Source Graph";
            if (integrationType === "GLOBAL-vs-LOCAL") {
                graphBadgeA = "Global Graph";
                graphBadgeB = "New Source Graph";
            }

            var n = i - 1;
            if (val.mapping_type === 'DATA-PROPERTY') {
                alignmentsInfo.push(val);

                var classA = removeURI(val.s).split("/")[removeURI(val.s).split("/").length - 1];
                var classB = removeURI(val.p).split("/")[removeURI(val.p).split("/").length - 1];
                $('#alignments').find('#alignmentsBody')
                    .append($('<tr id="row' + n + '">')
                        /*.append($('<td>').text(i))*/
                            .append($('<td>')
                                .text(classA).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.s)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">' + graphBadgeA + '</span>'))
                            ).append($('<td>')
                                .text(classB).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.p)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">' + graphBadgeB + '</span>'))
                            ).append($('<td>').text((Math.round(val.confidence * 100) / 100) * 100 + '%' + '')
                            )
                            .append($('<td class="accept-reject-buttons">').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(val) + '">Accept</button> '))
                            .append($('<td class="accept-reject-buttons">').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                    );
            }

            if (val.mapping_type === 'OBJECT-PROPERTY') {
                alignmentsInfo.push(val);
                var classAobjectP = removeURI(val.s).split("/")[removeURI(val.s).split("/").length - 1];
                var classBobjectP = removeURI(val.p).split("/")[removeURI(val.p).split("/").length - 1];

                $('#alignmentsObjProp').find('#alignmentsBodyObjectProperties')
                    .append($('<tr id="row' + n + '">')
                        // .append($('<td>').text(i))
                            .append($('<td>')
                                .text(classAobjectP).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.s)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">' + graphBadgeA + '</span>'))
                            ).append($('<td>')
                                .text(classBobjectP).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.p)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">' + graphBadgeB + '</span>'))
                            ).append($('<td>').text((Math.round(val.confidence * 100) / 100) * 100 + '%' + '')
                            )
                            .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(val) + '">Accept</button> '))
                            .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                    )
            }

            if (integrationType === "GLOBAL-vs-LOCAL") {
                $('#GlobalVsLocalRow').removeClass('d-none');
                if (val.mapping_type === 'CLASS') {
                    var otherClasses = val.other_classes;
                    var superClasses = val.super_classes;
                    //superClasses.push(superClasses[0]);
                    var temp = 1;
                    /*if (superClasses.length > 0) {
                        $("#SuperClassNote").append("<h5 style=\"color:deepskyblue\">Sub Classes are collapsed under Super Classes. It is recommended to align with the Super Class.</h5>");
                    }*/

                    superClasses.forEach(function (superClass) {
                        var iteratorCount = 0;
                        var headerRow;
                        var bodyRows = [];
                        var indexOfAlignment;
                        superClass.forEach(function (alignment) {

                            // First row will always be a Super Class
                            if (iteratorCount === 0) {
                                alignment.classType = "SUPERCLASS";
                                alignmentsInfo.push(alignment);
                                indexOfAlignment = alignmentsInfo.indexOf(alignment);
                                var cA = removeURI(alignment.s).split("/")[removeURI(alignment.s).split("/").length - 1];
                                var cB = removeURI(alignment.p).split("/")[removeURI(alignment.p).split("/").length - 1];
                                if (ifCollectionClasses(cA, cB)) {
                                    headerRow =
                                        "<tr id=\"row" + alignmentsInfo.indexOf(alignment) + "\">\n" +
                                        "\t<td>" + cA + " <span class=\"badge badge-info\">" + (removeURI(alignment.s)).split("/")[0] + " IRI </span>  " + '<span class="badge badge-primary">Global Graph</span> ' +
                                        " <span class=\"badge badge-success\"> Super Class</span> <i class=\"fa fa-info-circle\" data-toggle=\"tooltip\" data-placement=\"top\" title=\"It is recommended to align the matching class with the super class instead of its subclasses.\" aria-hidden=\"true\"></i> <span class=\"badge badge-warning\"><i class=\"fa fa-exclamation-triangle\" aria-hidden=\"true\" data-toggle=\"tooltip\" data-placement=\"top\" title=\"Not Recommended. Collection classes can only be aligned with collection classes.\">Not Recommended</i></span> </td>\n" +
                                        "\t<td>" + cB + " <span class=\"badge badge-info\">" + (removeURI(alignment.p)).split("/")[0] + " IRI</span> <span class=\"badge badge-primary\">New Source Graph</span> <span class=\"badge badge-warning\"><i class=\"fa fa-exclamation-triangle\" aria-hidden=\"true\" data-toggle=\"tooltip\" data-placement=\"top\" title=\"Not Recommended. Collection classes can only be aligned with collection classes.\">Not Recommended</i></span></td>\n" +
                                        "\t<td class='confidence-td'>" + (Math.round(alignment.confidence * 100) / 100) * 100 + "%</td>\n" +
                                        '\t<td> <input type="text" class="form-control" name="newClassName" id="classNameValueId' + indexOfAlignment + '" placeholder="Name the Super Class of ' + cA + ' & ' + cB + '" ></td>\n' +
                                        "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"acceptAlignment\" class=\"btn btn-success\" value=\"" + alignmentsInfo.indexOf(alignment) + "\">Accept</button> </td>\n" +
                                        "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"rejectAlignment\" class=\"btn btn-danger\">Reject</button> </td>\n" +
                                        "</tr>";
                                } else {
                                    headerRow =
                                        "<tr id=\"row" + alignmentsInfo.indexOf(alignment) + "\">\n" +
                                        "\t<td>" + cA + " <span class=\"badge badge-info\">" + (removeURI(alignment.s)).split("/")[0] + " IRI </span>  " + '<span class="badge badge-primary">Global Graph</span> ' +
                                        " <span class=\"badge badge-success\"> Super Class</span> <i class=\"fa fa-info-circle\" data-toggle=\"tooltip\" data-placement=\"top\" title=\"It is recommended to align the matching class with the super class instead of its subclasses.\" aria-hidden=\"true\"></i>  </td>\n" +
                                        "\t<td>" + cB + " <span class=\"badge badge-info\">" + (removeURI(alignment.p)).split("/")[0] + " IRI</span> <span class=\"badge badge-primary\">New Source Graph</span> </td>\n" +
                                        "\t<td class='confidence-td'>" + (Math.round(alignment.confidence * 100) / 100) * 100 + "%</td>\n" +
                                        '\t<td> <input disabled type="text" class="form-control" value="Not required in this case..." name="newClassName" id="classNameValueId' + indexOfAlignment + '" placeholder="Not required in this case..." ></td>\n' +
                                        "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"acceptAlignment\" class=\"btn btn-success\" value=\"" + alignmentsInfo.indexOf(alignment) + "\">Accept</button> </td>\n" +
                                        "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"rejectAlignment\" class=\"btn btn-danger\">Reject</button> </td>\n" +
                                        "</tr>";
                                }
                            } else {
                                // Other rows will be sub classes of that super class
                                alignment.classType = "LOCALCLASS";
                                alignmentsInfo.push(alignment);

                                var cAO = removeURI(alignment.s).split("/")[removeURI(alignment.s).split("/").length - 1];
                                var cBO = removeURI(alignment.p).split("/")[removeURI(alignment.p).split("/").length - 1];

                                bodyRows.push(
                                    "<tr id=\"row" + alignmentsInfo.indexOf(alignment) + "\">\n" +
                                    "\t<td>" + cAO + " <span class=\"badge badge-info\">" + (removeURI(alignment.s)).split("/")[0] + " IRI </span>  " + ' <span class="badge badge-primary">Global Graph</span> ' +
                                    " <span class=\"badge badge-success\">SubClass</span></td>\n" +
                                    "\t<td>" + cBO + " <span class=\"badge badge-info\">" + (removeURI(alignment.p)).split("/")[0] + " IRI </span> <span class=\"badge badge-primary\">New Source Graph</span> </td>\n" +
                                    "\t<td class='confidence-td'>" + (Math.round(alignment.confidence * 100) / 100) * 100 + "%</td>\n" +
                                    '\t<td> <input type="text" class="form-control" name="newClassName" id="classNameValueId' + alignmentsInfo.indexOf(alignment) + '" placeholder="Name the Super Class of ' + cAO + ' & ' + cBO + '" ></td>\n' +

                                    "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"acceptAlignment\" class=\"btn btn-success\" value=\"" + alignmentsInfo.indexOf(alignment) + "\">Accept</button> </td>\n" +
                                    "\t<td class='accept-reject-buttons'><button type=\"button\" id=\"rejectAlignment\" class=\"btn btn-danger\">Reject</button> </td>\n" +
                                    "</tr>");
                            }
                            iteratorCount++;
                        });


                        var cardElement = constructHTMLComponent(temp, headerRow, bodyRows, indexOfAlignment);

                        $("#superClass").append(cardElement);
                        temp++;
                    });

                    otherClasses.forEach(function (classVal) {
                        classVal.classType = "LOCALCLASS";
                        alignmentsInfo.push(classVal);

                        var cA = removeURI(classVal.s).split("/")[removeURI(classVal.s).split("/").length - 1];
                        var cB = removeURI(classVal.p).split("/")[removeURI(classVal.p).split("/").length - 1];

                        //console.log(ifCollectionClasses(cA, cB));

                        if (ifCollectionClasses(cA, cB)) {
                            $('#alignmentsClass').find('#alignmentsBodyClasses')
                                .append($('<tr id="row' + alignmentsInfo.indexOf(classVal) + '">')
                                    // .append($('<td>').text(i))
                                        .append($('<td>')
                                            .text(cA).append($('<span class="badge-margin badge badge-info"> <i aria-hidden="true" data-toggle="tooltip" data-placement="top" title="' + classVal.s + '">' + ' ' + (removeURI(classVal.s)).split("/")[0] + ' IRI</i></span> ' +
                                                '<span class="badge badge-primary">Global Graph</span> ' +
                                                '<span class="badge badge-warning"><i class="fa fa-exclamation-triangle" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Not Recommended. Collection classes can only be aligned with collection classes.">Not Recommended</i></span>'))
                                        ).append($('<td>')
                                            .text(cB).append($('<span class="badge-margin badge badge-info"> <i aria-hidden="true" data-toggle="tooltip" data-placement="top" title="' + classVal.p + '">' + ' ' + (removeURI(classVal.p)).split("/")[0] + ' IRI</i></span> ' +
                                            '<span class="badge badge-primary">New Source Graph</span> ' +
                                            '<span class="badge badge-warning"><i class="fa fa-exclamation-triangle" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Not Recommended. Collection classes can only be aligned with collection classes.">Not Recommended</i></span>'))
                                        ).append($('<td>').text((Math.round(classVal.confidence * 100) / 100) * 100 + '%' + '')
                                        )
                                        .append($('<td>').append('<input type="text" class="form-control" name="newClassName" id="classNameValueId' + alignmentsInfo.indexOf(classVal) + '" placeholder="Name the Super Class of ' + cA + ' & ' + cB + '" >'))
                                        .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(classVal) + '">Accept</button> '))
                                        .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                                );
                        } else {
                            $('#alignmentsClass').find('#alignmentsBodyClasses')
                                .append($('<tr id="row' + alignmentsInfo.indexOf(classVal) + '">')
                                    // .append($('<td>').text(i))
                                        .append($('<td>')
                                            .text(cA).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(classVal.s)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">Global Graph</span>'))
                                        ).append($('<td>')
                                            .text(cB).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(classVal.p)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">New Source Graph</span>'))
                                        ).append($('<td>').text((Math.round(classVal.confidence * 100) / 100) * 100 + '%' + '')
                                        )
                                        .append($('<td>').append('<input type="text" class="form-control" name="newClassName" id="classNameValueId' + alignmentsInfo.indexOf(val) + '" placeholder="Name the Super Class of ' + cA + ' & ' + cB + '" >'))
                                        .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(classVal) + '">Accept</button> '))
                                        .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                                );
                        }
                    });
                }
            }
            if (integrationType === "LOCAL-vs-LOCAL") {
                $('#LocalVsLocalRow').removeClass('d-none');
                if (val.mapping_type === 'CLASS') {
                    val.classType = "LOCALCLASS";
                    alignmentsInfo.push(val);

                    var cA = removeURI(val.s).split("/")[removeURI(val.s).split("/").length - 1];
                    var cB = removeURI(val.p).split("/")[removeURI(val.p).split("/").length - 1];
                    //console.log(ifCollectionClasses(cA, cB));

                    if (ifCollectionClasses(cA, cB)) {
                        $('#localAlignmentsClass').find('#localAlignmentsClassBody')
                            .append($('<tr id="row' + alignmentsInfo.indexOf(val) + '">')
                                // .append($('<td>').text(i))
                                    .append($('<td>')
                                        .text(cA).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.s)).split("/")[0] + ' IRI</span> ' +
                                            '<span class="badge badge-primary">Source Graph</span>' +
                                            '<span class="badge badge-warning"><i class="fa fa-exclamation-triangle" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Not Recommended. Collection classes can only be aligned with collection classes.">Not Recommended</i></span>'))
                                    ).append($('<td>')
                                        .text(cB).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.p)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">Source Graph</span>' +
                                        '<span class="badge badge-warning"><i class="fa fa-exclamation-triangle" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Not Recommended. Collection classes can only be aligned with collection classes.">Not Recommended</i></span>'))
                                    ).append($('<td>').text((Math.round(val.confidence * 100) / 100) * 100 + '%' + '')
                                    )
                                    .append($('<td>').append('<input type="text" class="form-control" name="newClassName" id="classNameValueId' + alignmentsInfo.indexOf(val) + '" placeholder="Name the Super Class of ' + cA + ' & ' + cB + '" >'))
                                    .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(val) + '">Accept</button> '))
                                    .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                            );

                    } else {
                        $('#localAlignmentsClass').find('#localAlignmentsClassBody')
                            .append($('<tr id="row' + alignmentsInfo.indexOf(val) + '">')
                                // .append($('<td>').text(i))
                                    .append($('<td>')
                                        .text(cA).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.s)).split("/")[0] + ' IRI</span> ' +
                                            '<span class="badge badge-primary">Source Graph</span>'))
                                    ).append($('<td>')
                                        .text(cB).append($('<span class="badge-margin badge badge-info"> ' + ' ' + (removeURI(val.p)).split("/")[0] + ' IRI</span> <span class="badge badge-primary">Source Graph</span>'))
                                    ).append($('<td>').text((Math.round(val.confidence * 100) / 100) * 100 + '%' + '')
                                    )
                                    .append($('<td>').append('<input type="text" class="form-control" name="newClassName" id="classNameValueId' + alignmentsInfo.indexOf(val) + '" placeholder="Name the Super Class of ' + cA + ' & ' + cB + '" >'))
                                    .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + alignmentsInfo.indexOf(val) + '">Accept</button> '))
                                    .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                            );
                    }
                }
            }

            ++i;
        });
    });
    console.log("Alignments Info");
    console.log(alignmentsInfo);
}

$(document).ready(function () {
    /* window.addEventListener('beforeunload', (event) => {
            // Cancel the event as stated by the standard.
            console.log("beforeunload");
            event.preventDefault();
            // Chrome requires returnValue to be set.
            event.returnValue = "Please click 'Stay on this Page' and we will give you candy";
        });*/
    getAlignments();
    integrationTypeChecker();

    new Tablesort(document.getElementById('alignments'));
    new Tablesort(document.getElementById('alignmentsClass'));
    new Tablesort(document.getElementById('alignmentsObjProp'));

    $(document).on('click', '#acceptAlignment', function () {
        var acceptButton = $("#acceptAlignment");
        var index = $(this).val();
        console.log(index);
        var userProvidedNameForTheNewClass = $("#classNameValueId" + index).val();
        console.log(userProvidedNameForTheNewClass);
        //acceptButtonClickHandler(acceptButton, index);
    });

    // Click handler for Finish Integration Button
    $("#integratedDataSourcesButton").on('click', function () {
        $("#overlay").fadeIn(100);
        console.log("IntegratedDataSourcesButton Clicked");
        var postData = {};
        postData.schema_iri = params[0] + "-" + params[1];
        postData.integrationType = integrationType;
        postData.ds1_id = ds1_id;
        postData.ds2_id = ds2_id;
        console.log(postData);
        $.ajax({
            type: 'POST',
            data: JSON.stringify(postData),
            contentType: 'application/json',
            url: '/finishIntegration',
            success: function (response) {
                console.log('Success');
                window.location.href = '/bdi';
            },
            error: function (response) {
                alert('failure' + JSON.stringify(response));
                console.log(JSON.stringify(response));
                $("#overlay").fadeOut(200);
            }
        });
    });
});

function acceptButtonClickHandler(acceptButton, i) {
    $("#overlay").fadeIn(100);
    console.log("AcceptButtonClickHandler");
    console.log(i);
    console.log(alignmentsInfo[i]);
    var data = alignmentsInfo[i];
    data.integrated_iri = params[0] + '-' + params[1];
    data.ds1_id = params[0];
    data.ds2_id = params[1];
    data.actionType = "ACCEPTED";

    console.log(data);
    $.ajax({
        type: 'POST',
        data: JSON.stringify(data),
        contentType: 'application/json',
        url: '/alignmentsAccept',
        success: function (response) {
            $("#overlay").fadeOut(100);
            console.log(response);
            if (response === "AlignmentSucceeded") {
                $("#row" + i).addClass("d-none");
                if (data.classType === "SUPERCLASS") {
                    $("#dropDownButton" + i).addClass("d-none");
                }
            }
        },
        error: function (response) {
            alert('failure' + JSON.stringify(response));
            console.log(JSON.stringify(response));
            $("#overlay").fadeOut(200);
        }
    });
}


function constructHTMLComponent(temp, headerRow, bodyRows, index) {
    console.log(index);
    var cardElement = "<div class=\"card bg-light bg-white border-0\" id=\"card" + temp + "\" >\n" +
        "    <div class=\"card-header remove-padding bg-white border-bottom-0 \">\n" +
        "        <div class=\"row\">\n" +
        "            <div class=\"col-md-11\" id=\"id\">\n" +
        "                <table class=\"table table-hover remove-margin\" id=\"headerTable" + temp + "\">\n" +
        "                   <tbody id=\"headerTableBody" + temp + "\">\n" +
        "                    \n" + headerRow +
        "                  </tbody>\n" +
        "                </table>\n" +
        "            </div>\n" +
        "            <div class=\"col-md-1\"><button class=\"btn btn-link\" data-toggle=\"collapse\" data-target=\"#dropDownData" + temp + "\" id=\"dropDownButton" + index + "\"><i class=\"fas fa-chevron-circle-down fa-2x\" style=\"color:#18bc9c;\"></i></button></div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "    <div class=\"collapse\" id=\"dropDownData" + temp + "\" aria-labelledby=\"headingOne\" data-parent=\"#accordion\">\n" +
        "        <div class=\"card-body remove-top-padding\">\n" +
        "            <div class=\"row\">\n" +
        "               </div>  <div class=\"col-md-11\" id=\"idd\">\n" +
        "                    <table class=\"table table-hover remove-margin\" id=\"bodyTable" + temp + "\">\n" +
        "                       <tbody class='bg-light' id=\"bodyTableBody" + temp + "\">\n" +
        "                         \n" + bodyRows.join(" ") +
        "                       </tbody>\n" +
        "                    </table>\n" +
        "                </div>\n" +
        "                <div class=\"col-md-1 \"></div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>";
    return cardElement;
}

