function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/datasets/"+getParameterByName("datasetID"), function(data) {
        $("#theTitle").text(data.name + " ("+data.type+")");

        if (data.type == "JSON") {
            $('#text')
                .append($('<code class="json hljs" id="theText">')
                );
            $("#theText").text((data.jsonInstances));
        }
        else if (data.type == "XML") {
            $('#text')
                .append($('<code class="xml hljs" id="theText">')
                );
            $("#theText").text((data.xmlInstances));
        }

        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    });

});
