function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $.get("/dataSource/"+getParameterByName("dataSourceID"), function(data) {
        var dataSource = (data);
        $("#id").val(dataSource.dataSourceID);
        $("#iri").val(dataSource.iri);
        $("#name").val(dataSource.name);

        switch (dataSource.type) {
            case "file":
                $("#file-tab").addClass("active"); $("#file").addClass("active");
                $("#file_path").val(dataSource.file_path);
                $("#file_format").val(dataSource.file_format);
                break;
            case "mongodb":
                $("#mongodb-tab").addClass("active"); $("#mongodb").addClass("active");
                $("#mongodb_hostname").val(dataSource.mongodb_hostname);
                $("#mongodb_port").val(dataSource.mongodb_port);
                $("#mongodb_username").val(dataSource.mongodb_username);
                $("#mongodb_password").val(dataSource.mongodb_password);
                $("#mongodb_database").val(dataSource.mongodb_database);
                break;
            case "restapi":
                $("#restapi-tab").addClass("active"); $("#restapi").addClass("active");
                $("#restapi_url").val(dataSource.restapi_url);
                $("#restapi_format").val(dataSource.restapi_format);
                break;
            case "sqldatabase":
                $("#sqldatabase-tab").addClass("active"); $("#sqldatabase").addClass("active");
                $("#sql_jdbc").val(dataSource.sql_jdbc);
                break;
        }

        //$("#editGlobalGraph").append($('<a href="/edit_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">'));
    });

});
