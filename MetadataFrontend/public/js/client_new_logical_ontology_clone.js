/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getPhysicalOntologies() {
    $.get("/artifacts/PHYSICAL_ONTOLOGY", function(data) {
        _.each(data, function(element,index,list) {
            var theObj = JSON.parse(element);
            $("#physicalOntology").append($('<option value="'+theObj.graph+'">').text(theObj.name));
        });
        $("#physicalOntology").select2();
    });

}

$(window).load(function() {


    $('#save').on("click", function(e){
        e.preventDefault();

        var formData = new FormData($('#logicalOntologyForm')[0]);
        formData.append('physicalOntology',$('#physicalOntology option:selected').val());

        $.ajax({
            url: '/logicalOntology/clone',
            type: 'POST',
            data:formData,
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_logical_ontologies';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});

$(function() {
    getPhysicalOntologies();
});