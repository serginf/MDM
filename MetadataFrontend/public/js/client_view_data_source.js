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
            case "avro":
                $("#avro-tab").addClass("active"); $("#avro").addClass("active");
                $("#avro_path").val(dataSource.avro_path);
                break;
            case "mongodb":
                $("#mongodb-tab").addClass("active"); $("#mongodb").addClass("active");
                $("#mongodb_connectionString").val(dataSource.mongodb_connectionString);
                $("#mongodb_database").val(dataSource.mongodb_database);
                break;
            case "neo4j":
                $("#neo4j-tab").addClass("active"); $("#neo4j").addClass("active");
                break;
            case "parquet":
                $("#parquet-tab").addClass("active"); $("#parquet").addClass("active");
                $("#parquet_path").val(dataSource.parquet_path);
                break;
            case "json":
                $("#json-tab").addClass("active"); $("#json").addClass("active");
                $("#json_path").val(dataSource.json_path);
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
