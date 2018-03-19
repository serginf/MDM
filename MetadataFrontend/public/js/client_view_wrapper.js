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
    $.get("/wrapper/"+getParameterByName("wrapperID"), function(data) {
        var wrapper = (data);
        $("#id").val(wrapper.wrapperID);
        $("#iri").val(wrapper.iri);
        $("#name").val(wrapper.name);
        $("#dataSource").val(wrapper.dataSourceID);

        _.each(wrapper.attributes,function(attribute) {
            $('#controls').append($('<input class="form-control" type="text" required="required" readonly="">').val(attribute.name));
        });

        $.get("/dataSource/"+wrapper.dataSourceID, function(ds) {
            switch (ds.type) {
                case "avro":
                    $("#sparksqlQueryForm").show();
                    $("#sparksqlQuery").val(wrapper.query);
                    break;
                case "mongodb":
                    $("#mongodbQueryForm").show();
                    $("#mongodbQuery").val(wrapper.query);
                    break;
                case "neo4j":
                    $("#cypherQueryForm").show();
                    $("#cypherQuery").val(wrapper.query);
                    break;
                case "parquet":
                    $("#sparksqlQueryForm").show();
                    $("#sparksqlQuery").val(wrapper.query);
                    break;
                case "plaintext":
                    $("#fileseparatorForm").show();
                    $("#fileseparator").val(wrapper.query);
                    break;
                case "restapi":
                    $("#restapiQueryForm").show();
                    $("#restapiQuery").val(wrapper.query);
                    break;
                case "sqldatabase":
                    $("#sqlQueryForm").show();
                    $("#sqlQuery").val(wrapper.query);
                    break;
            }
        });

    });

});
