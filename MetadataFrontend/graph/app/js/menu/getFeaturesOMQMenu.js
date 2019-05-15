module.exports = function (graph) {

    var getFeatures = {},
        getFeaturesButton;

    getFeatures.setup = function () {

        getFeaturesButton = d3.select("#get-features-omq-button")
            .on("click", function (d) {
                //logic
            });
    };

    getFeatures.hide = function (flag) {
        if(flag){
            d3.select("#c_get-features-omq").style("display","none")
        }else{
            d3.select("#c_get-features-omq").style("display","")
        }
    };

    return getFeatures;
};

