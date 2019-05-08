module.exports = function () {
    var options = {},
        global_Graph_Edit={
            editorMode : "true"
        },
        mappings_Graph_select = {
            editorMode : "false"
        },
        default_Option = {
            editorMode : "false"
        };

    options.getConf=function (type) {
        switch (type) {
            case "mappings_Graph_select":
                return mappings_Graph_select;
            case "global_Graph_Edit":
                return global_Graph_Edit;
            default:
                return default_Option;
        }
        return global_Graph_Edit;
    };

    options.global_Graph_Edit=function () {
        return global_Graph_Edit;
    };
    options.mappings_Graph_select=function () {
        return mappings_Graph_select;
    };
    options.default_Option=function () {
        return default_Option;
    };

    return options;

};
