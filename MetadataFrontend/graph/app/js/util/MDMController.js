/*
 * Hide button of mdm depending the selecting mode.
 */
module.exports = function (graph) {

    var controller = {},
        clearSelectSGMenu,
        saveGraphMenu,
        config,
        setup = false;

    controller.updateGui = function () {
        if(setup){
            if(config.editorMode === "true"){
                clearSelectSGMenu.hide(true);
                saveGraphMenu.hide(false);
            }else if(config.mode_selectSG === "true"){
                clearSelectSGMenu.hide(false);
                saveGraphMenu.hide(false);
            }else{
                //no mdm mode selected
                saveGraphMenu.hide(true);
                clearSelectSGMenu.hide(true);
            }
        }
    };

    controller.setup = function () {
        config = graph.options().defaultConfig();
        setup = true;
        clearSelectSGMenu = graph.options().clearSelectSGMenu();
        saveGraphMenu = graph.options().saveGraphMenu();
        controller.updateGui();
    };

    return controller;
};
