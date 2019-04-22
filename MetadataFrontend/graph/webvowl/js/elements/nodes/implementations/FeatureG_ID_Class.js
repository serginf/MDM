var RoundNode = require("../RoundNode");
var metamodel = require("../../metamodel")();
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type("G:Feature (ID subclass)")
            .iri(metamodel.namespaces().G+"Feature")
            .background(metamodel.colors().feature_id);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
