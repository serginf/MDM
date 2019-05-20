module.exports = function (graph) {

    var clearQuery = {},
        clearQueryButton;

    clearQuery.setup = function () {

        clearQueryButton = d3.select("#clear-query-omq-button")
            .on("click", function (d) {
                // e.preventDefault();
                d3.selectAll(".selection").remove();
                $("#text").remove();
                // $("#labelGeneratedSparql").remove();
                // $('pre code').each(function(i, e) {hljs.highlightBlock(e)});
                // selection=[];
                graph.clearSelectionSubGraph();
                $("#projectedFeatures").select2({
                    theme: "bootstrap"
                });
                // $("#generateRelationalAlgebraButton").addClass("invisible");
                // $("#relationalAlgebraArea").addClass("invisible");
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

