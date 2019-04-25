var BaseProperty = require("../BaseProperty");

module.exports = (function () {

    var o = function (graph) {
        BaseProperty.apply(this, arguments);

        this.attributes(["object"]) //?
            .styleClass("objectproperty")
            .type(Global.HAS_FEATURE.name);
    };
    o.prototype = Object.create(BaseProperty.prototype);
    o.prototype.constructor = o;

    return o;
}());


