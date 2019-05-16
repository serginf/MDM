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
                if (graph.options().defaultConfig().OMQ_mode === "true") {
                    console.log("changed")
                    for (var i = 0; i < globalGraphs.length; ++i) {
                        if (globalGraphs[i].globalGraphID == $(this).val()) currGlobalGraph = globalGraphs[i];
                    }
                    graph.options().loadingModule().parseUrlAndLoadOntology(true, currGlobalGraph.globalGraphID);
                }
            });
            $(selectIdentifier).trigger("change");

            if (graph.options().defaultConfig().OMQ_mode !== "true") {
                var gr = graph.options().loadingModule().currentGlobalGraph();
                if(gr)
                    setSelected(gr.globalGraphID)
            }
        });
    };

    function setSelected(id) {
        console.log("set:"+id);
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

