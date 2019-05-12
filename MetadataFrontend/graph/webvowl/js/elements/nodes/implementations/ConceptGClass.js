var RoundNode = require("../RoundNode");

module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type(Global.CONCEPT.name)
            .guiLabel(Global.CONCEPT.gui_name)
            .iri(Global.CONCEPT.iri)
            .iriType(Global.CONCEPT.iri)
            .background(Global.CONCEPT.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
