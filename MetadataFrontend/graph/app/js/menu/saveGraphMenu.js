module.exports = function (graph) {

    var saveGraphMenu = {},
        saveButton,
        loadingModule,
        exportMenu;

    saveGraphMenu.setup = function () {


        saveButton = d3.select("#save-button")
            .on("click", function (d) {
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


                // alert("Saved graph");
            });
    };

    return saveGraphMenu;
};
