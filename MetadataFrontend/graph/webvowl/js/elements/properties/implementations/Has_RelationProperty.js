var BaseProperty = require("../BaseProperty");

module.exports = (function () {

    var o = function (graph) {
        BaseProperty.apply(this, arguments);

        this.attributes(["object"]) //?
            .styleClass("objectproperty")
            .type(Global.HAS_RELATION.name)
            .guiLabel(Global.HAS_RELATION.gui_name)
            .label(Global.HAS_RELATION.iri.split("/").slice(-1)[0])
            .baseIri(Namespaces.G)
            .iriType(Global.HAS_RELATION.iri);

    };
    o.prototype = Object.create(BaseProperty.prototype);
    o.prototype.constructor = o;

    return o;
}());


