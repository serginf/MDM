function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $("#theTitle").text(getParameterByName("iri"));

    $.get("/graph/"+encodeURIComponent(getParameterByName("iri")), function(data) {
        $("#xml").text(data.rdf);
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    });

});
