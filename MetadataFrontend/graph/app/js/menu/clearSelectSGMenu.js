module.exports = function (graph) {

    var selectGraphMenu = {},
        selectSGButton,
        selectGMod,
        graphSelector,
        timeout;

    selectGraphMenu.setup = function () {

        selectSGButton = d3.select("#clear-select-sg-button")
            .on("click", function (d) {
                // graph.clearSelectionSubGraph();
            });
    };

    return selectGraphMenu;
};

