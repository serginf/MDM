/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function checkSelected(val) {
    var ret = false;
    $(".variable_priority option:selected").each(function() {
        if ($(this).val() === val) {
            ret = true;
        }
    });
    return ret;
}

var globalGraphs = [];
var wrappers = [];

var currGlobalGraph;
var currWrapper;

var currFeatures = [];
var currAttributes = [];

$(function() {
    $.get("/wrapper", function(data) {
        _.each(data, function(element) {
            var obj = JSON.parse(element);
            wrappers.push(obj);
            $("#wrapper").append($('<option value="'+obj.wrapperID+'">').text(obj.name));
        });
    }).done(function() {
        $.get("/globalGraph", function(data) {
            _.each(data, function(element) {
                var obj = JSON.parse(element);
                globalGraphs.push(obj);
                $("#globalGraph").append($('<option value="'+obj.globalGraphID+'">').text(obj.name));
            });
        }).done(function() {
            $("#globalGraph").change(function() {
                for (var i=0;i<globalGraphs.length;++i) {
                    if(globalGraphs[i].globalGraphID == $(this).val()) currGlobalGraph = globalGraphs[i];
                }
                $.get("/globalGraph/"+encodeURIComponent(currGlobalGraph.namedGraph)+"/features", function(features) {
                    currFeatures = features;
                    var i = 0;
                    _.each(currAttributes,function(attribute) {
                        $("#features"+i).empty().end();
                        var j = 1;
                        $('#features'+i).append($('<option>', { value:"feature0", text:"" } ));
                        _.each(currFeatures,function(feature) {
                            $('#features'+i).append($('<option>', { value:"feature"+j, text:feature } ));
                            ++j;
                        });
                        ++i;
                    });

                });
            });

            $("#wrapper").change(function() {
                $('#attributes').empty().end();
                $('#features').empty().end();

                for (var i=0;i<wrappers.length;++i) {
                    if(wrappers[i].wrapperID == $(this).val()) currWrapper = wrappers[i];
                }
                $.get("/wrapper/"+encodeURIComponent(currWrapper.iri)+"/attributes", function(attributes) {
                    currAttributes = attributes;
                    var i = 0;
                    _.each(attributes,function(attribute) {
                        $('#attributes').append($('<input class="form-control" id="attribute'+i+'" type="text" required="required" readonly="">').val(attribute));

                        $('#features').append($('<select class="form-control variable_priority unique required" id="features'+i+'" type="text" name="features[]" required="required"></select>'));
                        var j = 1;
                        $('#features'+i).append($('<option>', { value:"feature0", text:"" } ));
                        _.each(currFeatures,function(feature) {
                            $('#features'+i).append($('<option>', { value:"feature"+j, text:feature } ));
                            ++j;
                        });
                        ++i;
                    });
                    $('.variable_priority').change(function() {
                        $('option', this).each(function() {
                            if (checkSelected($(this).val()) && $(this).val() != "feature0") {
                                $('.variable_priority option[value=' + $(this).val() + ']').attr('disabled', true);
                            } else {
                                $('.variable_priority option[value=' + $(this).val() + ']').removeAttr('disabled');
                            }
                        });
                    });
                });
            });
            $("#wrapper").trigger("change");
            $("#globalGraph").trigger("change");
        });
    });


    $('#submitLAVMapping').on("click", function(e){
        e.preventDefault();

        var lav_mapping = new Object();

        lav_mapping.wrapperID = currWrapper.wrapperID;
        lav_mapping.globalGraphID = currGlobalGraph.globalGraphID;

        lav_mapping.sameAs = new Array();
        for (var i = 0; i < $('#attributes input').length; ++i) {
            var from = $('#attribute' + i).val();
            var to = $('#features' + i+' option:selected').text();
            if (to != "") {
                var oneMapTo = new Object();
                oneMapTo.attribute = from;
                oneMapTo.feature = to;
                lav_mapping.sameAs.push(oneMapTo);
            }
        }

        $.ajax({
            url: '/LAVMapping/sameAs',
            method: "POST",
            data: lav_mapping
        }).done(function() {
            window.location.href = '/manage_lav_mappings';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});