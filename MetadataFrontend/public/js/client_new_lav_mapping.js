/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var globalGraphs = [];
var wrappers = [];

var currGlobalGraph;
var currWrapper;

var currFeatures = [];

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
                });
            });

            $("#wrapper").change(function() {
                $('#attributes').empty().end();
                $('#features').empty().end();

                for (var i=0;i<wrappers.length;++i) {
                    if(wrappers[i].wrapperID == $(this).val()) currWrapper = wrappers[i];
                }
                $.get("/wrapper/"+encodeURIComponent(currWrapper.iri)+"/attributes", function(attributes) {
                    var i = 0;
                    _.each(attributes,function(attribute) {
                        $('#attributes').append($('<input class="form-control" id="attr'+i+'" type="text" required="required" readonly="">').val(attribute));

                        //
                        $('#features').append($('<select class="form-control" id="features'+i+'" type="text" name="name" required="required"></select>'));
                        var j = 1;
                        $('#features'+i).append($('<option>', { value:"feature0", text:"" } ));
                        _.each(currFeatures,function(feature) {
                            $('#features'+i).append($('<option>', { value:"feature"+j, text:feature } ));
                            ++j;
                        });
                        ++i;
                    });
                    //Update features when one is selected
                    for (i = 0; i < $('#attributes input').length; ++i) {
                        console.log("modifying features"+i);
                        $('#features'+i).change(function(handler) {
                            for (var j = 0; j < $('#attributes input').length; ++j) {
                                if (handler.currentTarget.id != "features"+j) {
                                    console.log(handler.currentTarget.id + " - " + "features"+j + " -- "+$(this).val());
                                    $('#features'+j+' option[value="'+$(this).val()+'"]').remove();
                                }
                            }
                        });
                    }
                });
            });

            $("#wrapper").trigger("change");
            $("#globalGraph").trigger("change");

        });
    });


    $('#submitLAVMapping').on("click", function(e){
        e.preventDefault();

        var lav_mapping = new Object();

        $.ajax({
            url: '/LAVMapping',
            method: "POST",
            data: lav_mapping
        }).done(function() {
            window.location.href = '/manage_lav_mappings';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});