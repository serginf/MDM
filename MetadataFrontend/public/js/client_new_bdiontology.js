/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/release", function(data) {
        _.each(data, function(element,index,list) {
            var obj = (element);
            $("#releases").append($('<option value="'+obj.releaseID+'">').text(obj.event + " ("+obj.schemaVersion+")"));
        });
        $("#releases").select2({
            theme: "bootstrap"
        });
    });

    $.get("/bdi_ontology_generation_strategies", function(data) {
        _.each(data, function(element,index,list) {
            var obj = (element);
            $("#generationStrategy").append($('<option value="'+obj.key+'">').text(obj.val));
        });
        $("#generationStrategy").select2({
            theme: "bootstrap"
        });
    });


    $('#submit_bdiontology').on("click", function(e){
        e.preventDefault();

        var ontology = new Object();
        ontology.name = $("#name").val();
        ontology.releases = new Array();

        $.each($('#releases option:selected'), function(i) {
            ontology.releases.push($(this)[0].value);
        });

        ontology.generationStrategy = $("#generationStrategy").val();

        $.ajax({
            url: '/bdi_ontology',
            type: 'POST',
            data: ontology
        }).done(function() {
            window.location.href = '/manage_bdi_ontologies';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});