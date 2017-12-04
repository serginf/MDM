function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/release/"+getParameterByName("releaseID"), function(data) {
        var releaseObj = (data);

        $("#event").val(releaseObj.event);
        $("#schemaVersion").val(releaseObj.schemaVersion);
        $("#kafkaTopic").val(releaseObj.kafkaTopic);
        if (releaseObj.dispatch === "true") $("#dispatch").prop('checked', true);
        $("#dispatchTo").val(releaseObj.dispatcherPath ? releaseObj.dispatcherPath : '-');
        $("input:checkbox").click(function() { return false; });

        var container = document.getElementById("jsoneditor");
        $("#jsoneditor").css("background-color","white");
        var options = {
            "mode": "view",
            "indentation": 2
        };
        var editor = new JSONEditor(container, options);
        editor.set(JSON.parse(releaseObj.jsonInstances));

    });

});
