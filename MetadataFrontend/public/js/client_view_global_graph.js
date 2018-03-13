function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $.get("/globalGraph/"+getParameterByName("globalGraphID"), function(data) {
        var globalGraph = (data);
        $("#id").val(globalGraph.globalGraphID);
        $("#name").val(globalGraph.name);
        $("#defaultNamespace").val(globalGraph.defaultNamespace);
        $("#namedGraph").val(globalGraph.namedGraph);

        //$("#editGlobalGraph").append($('<a href="/edit_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">'));
    });

});
