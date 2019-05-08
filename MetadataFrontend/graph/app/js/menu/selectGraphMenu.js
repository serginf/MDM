module.exports = function (graph) {

    var selectGraphMenu = {},
        selectSGButton,
        selectGMod,
        graphSelector;

    selectGraphMenu.setup = function () {

        selectSGButton = d3.select("#select-sg-button")
            .on("click", function (d) {
                //add logic
            });
    };

    return selectGraphMenu;
};

