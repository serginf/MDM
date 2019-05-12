var RoundNode = require("../RoundNode");
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type(Global.FEATURE_ID.name)
            .guiLabel(Global.FEATURE_ID.gui_name)
            .iri(Global.FEATURE_ID.iri)
            .iriType(Global.FEATURE_ID.iri)
            .background(Global.FEATURE_ID.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
