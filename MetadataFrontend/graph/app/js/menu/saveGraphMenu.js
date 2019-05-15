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
                        success: function(data) {
                            console.log("success");
                            graph.options().alertModule().showAlert("Information","Graph saved",1);
                        }
                    });
                } else if(config.mode_selectSG === "true"){
                    var subGraph = new Object();
                    subGraph.selection = graph.prepareSelectionObject();
                    subGraph.LAVMappingID = getParameterByName("LAVMappingID");
                    $.ajax({
                        url: '/LAVMapping/subgraph',
                        type: 'POST',
                        data: subGraph,
                        success: function(data) {
                            console.log("success");
                            graph.options().alertModule().showAlert("Information","Mappings saved",1);
                        }
                    });
                }
            });
    };

    saveGraphMenu.hide = function (flag) {
        if(flag){
            d3.select("#c_save").style("display","none")
        }else{
            d3.select("#c_save").style("display","")
        }
    };

    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    return saveGraphMenu;
};
