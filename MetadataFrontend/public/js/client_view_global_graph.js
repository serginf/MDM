function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $.get("/globalGraph/"+encodeURIComponent(getParameterByName("globalGraphID")), function(data) {
        $('#defaultNamespace').val(data.defaultNamespace);
        $('#name').val(data.name);
        $('#namedGraph').val(data.namedGraph);
        $('#id').val(data.globalGraphID);

    });
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
    })
});

