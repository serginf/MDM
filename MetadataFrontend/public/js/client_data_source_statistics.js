/**
 * Created by snadal on 16/01/17.
 */

var breakdown;
var containerIndex;
var plots;
var plotsData;
var starting_value = 5000;

$(function() {
    breakdown = new Object();
    plots = new Object();
    plotsData = new Object();
    containerIndex = 0;
});

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $('#refreshButton').on("click", function(e){
        e.preventDefault();
        location.reload();
    });
});

$(function() {
    var topic = getParameterByName("topic");
    var socket = io("/socket_data_source_statistics");

    socket.on('/socket_data_source_statistics', function (fromSocket) {
        var socketMsg = JSON.parse(fromSocket.message);
        if (socketMsg.kafkaTopic == topic) {
            if (!breakdown[socketMsg.iri]) {
                var html = '<div id="graph'+containerIndex+'" class="aGraph" style="position:relative;width:100%;height:250px"><h4>'+socketMsg.attribute+'</h4></div>';
                $("#allPlots").append(html);

                breakdown[socketMsg.iri] = new Object();
                plots[socketMsg.iri] = new Object();
                var newData = getNewDataElement();
                newData["displayNames"] = ["Current value"];
                newData["colors"] = ["yellow"];
                newData["scale"] = "linear";


                plotsData[socketMsg.iri] = newData;

                plots[socketMsg.iri].plot = new LineGraph({containerId: 'graph' + containerIndex, data: plotsData[socketMsg.iri]});

                ++containerIndex;
            }
            breakdown[socketMsg.iri].val = socketMsg.values;

            plotsData[socketMsg.iri].values = [[(breakdown[socketMsg.iri].val)]];
            plotsData[socketMsg.iri].start += plotsData[socketMsg.iri].step;
            plotsData[socketMsg.iri].end += plotsData[socketMsg.iri].step;
            plots[socketMsg.iri].plot.slideData(plotsData[socketMsg.iri]);
        }
    });

});

function getNewDataElement() {
    var ret = {"start":(new Date()).getTime()-6000*150+140000,"end":(((new Date()).getTime())+6000*30*6),"step":5000,"names":["Stats_count2xx"],"values":[[starting_value],[0]]};
    for (var i = 0; i < 150; ++i) {
        ret.values[0].push([0]);
    }
    return ret;
}
