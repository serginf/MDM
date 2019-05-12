module.exports = function (graph) {

    var saveGraphMenu = {},
        saveButton,
        loadingModule,
        exportMenu;

    saveGraphMenu.setup = function () {


        saveButton = d3.select("#save-button")
            .on("click", function (d) {
                var config = graph.options().defaultConfig();
                if(config.editorMode === "true"){
                    loadingModule=graph.options().loadingModule();
                    exportMenu = graph.options().exportMenu();
                    $.ajax({
                        type: "POST",
                        url: '/globalGraph/'+loadingModule.currentGlobalGraph().globalGraphID+'/graphicalGraph',
                        data: {graphicalGraph: exportMenu.getJson()}
                    });

                    $.ajax({
                        type: "POST",
                        url: '/globalGraph/'+encodeURIComponent(loadingModule.currentGlobalGraph().namedGraph)+'/TTL',
                        data:  {'ttl': exportMenu.exportTurtleText()},
                    });
                } else if(config.mode_selectSG === "true"){
                    var subGraph = new Object();
                    subGraph.selection = graph.prepareSelectionObject();
                    subGraph.LAVMappingID = getParameterByName("LAVMappingID");
                    $.ajax({
                        url: '/LAVMapping/subgraph',
                        type: 'POST',
                        data: subGraph
                    }).done(function(res) {
                        //alert
                    }).fail(function(err) {
                        console.log("error "+JSON.stringify(err));
                    });
                }



                // alert("Saved graph");
            });
    };

    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    return saveGraphMenu;
};
