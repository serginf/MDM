var RoundNode = require("../RoundNode");
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type(Global.FEATURE.name)
            .guiLabel(Global.FEATURE.gui_name)
            .iri(Global.FEATURE.iri)
            .iriType(Global.FEATURE.iri)
            .background(Global.FEATURE.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
