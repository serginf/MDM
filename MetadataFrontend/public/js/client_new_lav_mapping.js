/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var currGlobalGraph;
var currDataSource;

$(function() {
    $.get("/wrapper", function(data) {
        _.each(data, function(element,index,list) {
            var obj = JSON.parse(element);
            $("#wrapper").append($('<option value="'+obj.wrapperID+'">').text(obj.name));
        });
    }).done(function() {
        $("#wrapper").change(function() {
            alert("eii");
        });
        $("#wrapper").trigger("change");
    });

    $.get("/globalGraph", function(data) {
        _.each(data, function(element,index,list) {
            currGlobalGraph = JSON.parse(element);
            $("#globalGraph").append($('<option value="'+currGlobalGraph.globalGraphID+'">').text(currGlobalGraph.name));
        });
    }).done(function() {
        $("#globalGraph").change(function() {
            $.get("/globalGraph/"+encodeURIComponent(currGlobalGraph.namedGraph)+"/features", function(features) {
                alert(features);
            });

            alert("eii2");
        });
        $("#globalGraph").trigger("change");
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