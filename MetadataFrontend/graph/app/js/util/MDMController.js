/*
 * Hide button of mdm depending the selecting mode.
 */
module.exports = function (graph) {

    var controller = {},
        //Selection
        clearSelectSGMenu,
        //OMG
        clearQueryOMQMenu,
        genSparqlOMQ,
        getFeaturesOMQ,
        graphDropdown,
        //Selection & editor
        saveGraphMenu,
        config,
        setup = false;

    controller.updateGui = function () {
        if(setup){
            if(config.editorMode === "true"){
                hideSelection(true);
                hideOMQ(true)
                saveGraphMenu.hide(false);
            }else if(config.selectSG_mode === "true"){
                hideSelection(false);
                hideOMQ(true);
                saveGraphMenu.hide(false);
            }else if (config.OMQ_mode === "true") {
                hideOMQ(false);
                hideSelection(true);
                saveGraphMenu.hide(true);
            }else{
                //no mdm mode selected
                hideOMQ(true);
                hideSelection(true);
                saveGraphMenu.hide(true);
            }
        }
    };

    function hideOMQ(flag){
        clearQueryOMQMenu.hide(flag);
        genSparqlOMQ.hide(flag);
        getFeaturesOMQ.hide(flag);
        graphDropdown.hide(flag);
    }

    function hideSelection(flag){
        clearSelectSGMenu.hide(flag);
    }

    controller.setup = function () {
        config = graph.options().defaultConfig();
        setup = true;
        clearSelectSGMenu = graph.options().clearSelectSGMenu();
        genSparqlOMQ = graph.options().generateSparqlOMQMenu();
        clearQueryOMQMenu = graph.options().clearQueryOMQMenu();
        graphDropdown = graph.options().graphOMQdropdown();
        getFeaturesOMQ = graph.options().getFeaturesOMQMenu();
        saveGraphMenu = graph.options().saveGraphMenu();
        controller.updateGui();
    };

    return controller;
};
