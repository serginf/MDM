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
    })
});
