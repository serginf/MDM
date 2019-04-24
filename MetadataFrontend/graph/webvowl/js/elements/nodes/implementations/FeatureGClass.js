var RoundNode = require("../RoundNode");
var metamodel = require("../../metamodel")();
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type("G:Feature")
            .iri(metamodel.Global().FEATURE.iri)
            .background(metamodel.Global().FEATURE.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
