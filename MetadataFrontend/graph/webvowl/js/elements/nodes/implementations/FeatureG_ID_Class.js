var RoundNode = require("../RoundNode");
var metamodel = require("../../metamodel")();
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type("G:Feature (ID subclass)")
            .iri(metamodel.Global().FEATURE_ID.iri)
            .background(metamodel.Global().FEATURE_ID.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
