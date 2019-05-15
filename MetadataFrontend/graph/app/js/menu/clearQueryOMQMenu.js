module.exports = function (graph) {

    var clearQuery = {},
        clearQueryButton;

    clearQuery.setup = function () {

        clearQueryButton = d3.select("#clear-query-omq-button")
            .on("click", function (d) {
                //logic
            });
    };

    clearQuery.hide = function (flag) {
        if(flag){
            d3.select("#c_clear-query-omq").style("display","none")
        }else{
            d3.select("#c_clear-query-omq").style("display","")
        }
    };

    return clearQuery;
};

