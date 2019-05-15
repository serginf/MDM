module.exports = function (graph) {

    var clearSelectionMenu = {},
        selectSGButton;

    clearSelectionMenu.setup = function () {

        selectSGButton = d3.select("#clear-select-sg-button")
            .on("click", function (d) {
                graph.clearSelectionSubGraph();
            });
    };

    clearSelectionMenu.hide = function (flag) {
        if(flag){
            d3.select("#c_clear-select-sg").style("display","none")
        }else{
            d3.select("#c_clear-select-sg").style("display","")
        }
    };



    return clearSelectionMenu;
};

