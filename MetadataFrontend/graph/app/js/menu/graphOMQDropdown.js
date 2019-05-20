module.exports = function (graph) {

    var globalGraphDropdown = {},
        globalGraphs = [],
        currGlobalGraph,
        selectIdentifier = "#opt-global-graph-omq-select";

    globalGraphDropdown.setup = function () {

        $.get("/globalGraph", function (data) {
            _.each(data, function (element) {
                var obj = JSON.parse(element);
                globalGraphs.push(obj);
                $("#opt-global-graph-omq-select").append($('<option value="' + obj.globalGraphID + '">').text(obj.name));
            });
        }).done(function () {

            $(selectIdentifier).change(function () {
                if (graph.options().defaultConfig().OMQ_mode === "true")
                    changeSelectorGlobalGraph($(this).val());
            });
            $(selectIdentifier).trigger("change");

            if (graph.options().defaultConfig().OMQ_mode !== "true") {
                var gr = graph.options().loadingModule().currentGlobalGraph();
                if(gr)
                    setSelected(gr.globalGraphID)
            }
        });
    };

    globalGraphDropdown.getCurrGlobalGraph =function() {
        return currGlobalGraph;
    };

    function changeSelectorGlobalGraph(id) {
        graph.clearSelectionSubGraph();
        $("#projectedFeatures").empty().end();
        for (var i = 0; i < globalGraphs.length; ++i) {
            if (globalGraphs[i].globalGraphID == id) currGlobalGraph = globalGraphs[i];
        }
        $.get("/globalGraph/"+encodeURIComponent(currGlobalGraph.namedGraph)+"/features", function(features) {
            _.each(features,function(feature) {
                $('#projectedFeatures').append($('<option value="'+feature+'">').text(feature));
            });
            $("#projectedFeatures").select2({
                theme: "bootstrap"
            });
        });
        graph.options().loadingModule().parseUrlAndLoadOntology(true, currGlobalGraph.globalGraphID);
    }

    function setSelected(id) {
        $(selectIdentifier).val(id);
    }

    globalGraphDropdown.hide = function (flag) {
        if (flag) {
            d3.select("#c_opt-global-graph-omq").style("display", "none")
        } else {
            d3.select("#c_opt-global-graph-omq").style("display", "")
        }
    };

    return globalGraphDropdown;
};

