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


$(function() {
    $('#showCompleteIRIs').change(function() {
        if ($(this).prop("checked")) {
            $('text').each(function() {
                //$(this).text("you fff");
            });
        } else {
            $('.edgelabel').each(function() {
                var iri = $(this).text();
                $(this).text(iri.substring(iri.lastIndexOf("/")+1,iri.length));
            });
            $('text').each(function() {
                var iri = $(this).text();
                $(this).text(iri.substring(iri.lastIndexOf("/")+1,iri.length));
            });
        }
        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function(d) { return d.id; }))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2));
        simulation.tick();
    })
});
