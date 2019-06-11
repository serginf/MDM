module.exports = function () {
    var config = {},
        global_Graph_Edit = {
            editorMode: "true",
            selectSG: "false",
            OMQ_mode: "false",
            bdi: "false"
        },
        mappings_Graph_select = {
            editorMode: "false",
            selectSG: "true",
            OMQ_mode: "false",
            bdi: "false"
        },
        omq = {
            editorMode: "false",
            selectSG: "false",
            OMQ_mode: "true",
            bdi: "false"
        },
        bdi_visualization = {
            bdi: "true",
            editorMode: "false",
            selectSG: "false",
            OMQ_mode: "false"
        },
        default_Option = {
            editorMode: "false",
            selectSG: "false",
            OMQ_mode: "false",
            bdi: "false"
        };

    config.getConf = function (type) {
        switch (type) {
            case "mappings_Graph_select":
                return mappings_Graph_select;
            case "global_Graph_Edit":
                return global_Graph_Edit;
            case "omq":
                return omq;
            case "bdi_visualization":
                return bdi_visualization;
            default:
                return default_Option;
        }
        return global_Graph_Edit;
    };

    config.global_Graph_Edit = function () {
        return global_Graph_Edit;
    };
    config.mappings_Graph_select = function () {
        return mappings_Graph_select;
    };
    config.default_Option = function () {
        return default_Option;
    };
    config.omq = function () {
        return omq;
    };
    config.bdi_visualization = function () {
        return bdi_visualization;
    };

    return config;

};
