function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function () {
    // Set color for the metamodel selectors
    //, style="background-color: #33CCCC"
    $("#"+(Global.CONCEPT.name)).css("background-color",Global.CONCEPT.color);
    $("#"+(Global.FEATURE.name)).css("background-color",Global.FEATURE.color);
    $("#"+(Global.INTEGRITY_CONSTRAINT.name)).css("background-color",Global.INTEGRITY_CONSTRAINT.color);
    $("#"+(Global.DATATYPE.name)).css("background-color",Global.DATATYPE.color);

    $("#"+(Source.EVENT.name)).css("background-color",Source.EVENT.color);
    $("#"+(Source.SCHEMA_VERSION.name)).css("background-color",Source.SCHEMA_VERSION.color);
    $("#"+(Source.MEDIA_TYPE.name)).css("background-color",Source.MEDIA_TYPE.color);
    $("#"+(Source.EMBEDDED_OBJECT.name)).css("background-color",Source.EMBEDDED_OBJECT.color);
    $("#"+(Source.ARRAY.name)).css("background-color",Source.ARRAY.color);
    $("#"+(Source.ATTRIBUTE.name)).css("background-color",Source.ATTRIBUTE.color);
    $("#"+(Source.KAFKA_TOPIC.name)).css("background-color",Source.KAFKA_TOPIC.color);

});

$(window).load(function() {
    $.get("/bdi_ontology/graph/"+encodeURIComponent(getParameterByName("graph")), function(data) {
        $("#theTitle").text(data.name);
        $("#theURL").text(data.O);
    });

});
