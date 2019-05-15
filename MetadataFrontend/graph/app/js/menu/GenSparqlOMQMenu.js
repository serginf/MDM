module.exports = function (graph) {

    var clearQuery = {},
        clearQueryButton;

    clearQuery.setup = function () {

        clearQueryButton = d3.select("#generate-sparql-omq-button")
            .on("click", function (d) {
                //logic
            });
    };

    clearQuery.hide = function (flag) {
        if(flag){
            d3.select("#c_generate-sparql-omq").style("display","none")
        }else{
            d3.select("#c_generate-sparql-omq").style("display","")
        }
    };

    return clearQuery;
};

