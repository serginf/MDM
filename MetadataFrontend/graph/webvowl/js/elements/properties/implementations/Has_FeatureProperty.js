var BaseProperty = require("../BaseProperty");

module.exports = (function () {

    var o = function (graph) {
        BaseProperty.apply(this, arguments);

        this.attributes(["object"]) //?
            .styleClass("objectproperty")
            .type(Global.HAS_FEATURE.name)
            .guiLabel(Global.HAS_FEATURE.gui_name)
            .baseIri(Namespaces.G)
            .iriType(Global.HAS_FEATURE.iri);

        var label = Global.HAS_FEATURE.iri.split("/").slice(-1)[0];

        // Disallow overwriting the label
        this.label = function (p) {
            if (!arguments.length) return label;
            return this;
        };
    };
    o.prototype = Object.create(BaseProperty.prototype);
    o.prototype.constructor = o;

    return o;
}());


