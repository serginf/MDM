var RoundNode = require("../RoundNode");
var metamodel = require("../../metamodel")();
module.exports = (function () {

    var o = function (graph) {
        RoundNode.apply(this, arguments);
        this.type("G:Concept")
            .iri(metamodel.Global().CONCEPT.iri)
            .background(metamodel.Global().CONCEPT.color);
    };
    o.prototype = Object.create(RoundNode.prototype);
    o.prototype.constructor = o;

    return o;
}());
