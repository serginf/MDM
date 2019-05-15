module.exports = function (graph) {

    var globalGraphDropdown = {},
        globalGraphDropdownButton,
        globalGraphs = [];

    globalGraphDropdown.setup = function () {

        // globalGraphDropdownButton = d3.select("#opt-global-graph-omq-select")
        //     .on("click", function (d) {
                $.get("/globalGraph", function(data) {
                    _.each(data, function(element) {
                        var obj = JSON.parse(element);
                        globalGraphs.push(obj);
                        $("#opt-global-graph-omq-select").append($('<option value="'+obj.globalGraphID+'">').text(obj.name));
                    });
                }).done(function() {
                    $("#globalGraph").change(function() {
                        // $("#projectedFeatures").empty().end();
                        // $("svg").empty().end();
                        // selection = [];
                        //
                        // for (var i=0;i<globalGraphs.length;++i) {
                        //     if(globalGraphs[i].globalGraphID == $(this).val()) currGlobalGraph = globalGraphs[i];
                        // }

                        // $.get("/globalGraph/"+encodeURIComponent(currGlobalGraph.namedGraph)+"/features", function(features) {
                        //     _.each(features,function(feature) {
                        //         $('#projectedFeatures').append($('<option value="'+feature+'">').text(feature));
                        //     });
                        //     $("#projectedFeatures").select2({
                        //         theme: "bootstrap"
                        //     });
                        // });

                        // drawGraph(currGlobalGraph.namedGraph);
                    });
                    $("#globalGraph").trigger("change");
                });
            // });
    };

    globalGraphDropdown.hide = function (flag) {
        if(flag){
            d3.select("#c_opt-global-graph-omq").style("display","none")
        }else{
            d3.select("#c_opt-global-graph-omq").style("display","")
        }
    };

    return globalGraphDropdown;
};

