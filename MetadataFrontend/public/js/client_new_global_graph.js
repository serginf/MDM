/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {

    $('#submitGlobalGraph').on("click", function(e){
        e.preventDefault();
        var graph = new Object();
        graph.name = $("#name").val();
        graph.defaultNamespace = $("#defaultNamespace").val();

        $.ajax({
            url: '/globalGraph',
            method: "POST",
            data: graph
        }).done(function() {
            window.location.href = '/manage_global_graphs';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});