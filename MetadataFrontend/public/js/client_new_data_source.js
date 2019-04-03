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

    $('#submitDataSource').on("click", function(e){
        e.preventDefault();

        var dataSource = new Object();
        dataSource.name = $("#name").val();
        switch ($('.nav-tabs .active').attr('id')) {
            case "avro-tab":
                dataSource.type = "avro";
                dataSource.avro_path = $("#avro_path").val();
                break;

            case "mongodb-tab":
                dataSource.type = "mongodb";
                dataSource.mongodb_connectionString = $("#mongodb_connectionString").val();
                dataSource.mongodb_database = $("#mongodb_database").val();
                break;

            case "neo4j-tab":
                dataSource.type = "neo4j";
                break;

            case "parquet-tab":
                dataSource.type = "parquet";
                dataSource.parquet_path = $("#parquet_path").val();
                break;

            case "json-tab":
                dataSource.type = "json";
                dataSource.json_path = $("#json_path").val();
                break;

            case "restapi-tab":
                dataSource.type = "restapi";
                dataSource.restapi_url = $("#restapi_url").val();
                dataSource.restapi_format = $("#restapi_format").val();
                break;

            case "sqldatabase-tab":
                dataSource.type = "sql";
                dataSource.sql_jdbc = $("#sql_jdbc").val();
                break;
        }
        $.ajax({
            url: '/dataSource',
            method: "POST",
            data: dataSource
        }).done(function() {
            window.location.href = '/manage_data_sources';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});