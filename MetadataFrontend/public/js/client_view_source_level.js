function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/artifacts/SOURCE/"+encodeURIComponent(getParameterByName("graph")), function(data) {
        $("#theTitle").text(data.name);
        $("#theURL").text(data.graph);
    });

    $.get("/artifacts/SOURCE/"+encodeURIComponent(getParameterByName("graph"))+"/content", function(data) {
        $("#xml").text((data));
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    });

});
