/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var dataSources = [];
var currDataSource;

$(function(){
    $(document).on('click', '.btn-add', function(e) {
        e.preventDefault();
        var controlForm = $('.controls form:first'),
            currentEntry = $(this).parents('.entry:first'),
            newEntry = $(currentEntry.clone()).appendTo(controlForm);
        newEntry.find('input').val('');
        controlForm.find('.entry:not(:last) .btn-add')
            .removeClass('btn-add').addClass('btn-remove')
            .removeClass('btn-success').addClass('btn-danger')
            .html('<span class="fa fa-minus"></span>');
    }).on('click', '.btn-remove', function(e) {
        $(this).parents('.entry:first').remove();
        e.preventDefault();
        return false;
    });
});

$(function(){
    $(document).on('click', '.btn-preview', function(e) {
        $("#previewModal").modal("show");
        var query;
        if (currDataSource.type == "avro") query = $("#sparksqlQuery").val();
        else if (currDataSource.type == "mongodb") query = $("#mongodbQuery").val();
        else if (currDataSource.type == "neo4j") query = $("#cypherQuery").val();
        else if (currDataSource.type == "parquet") query = $("#sparksqlQuery").val();
        else if (currDataSource.type == "plaintext") query = $("#fileseparator").val();
        else if (currDataSource.type == "restapi") query = $("#restapiQuery").val();
        else if (currDataSource.type == "sqldatabase") query = $("#sqlQuery").val();

        var previewObj = new Object();
        previewObj.dataSourceID = currDataSource.dataSourceID;
        previewObj.query = query;
        previewObj.attributes = new Array();
        $('input[name^="attributeSet"]').each(function() { previewObj.attributes.push(($(this).val()));});
        $.ajax({
            url: '/wrapper/preview',
            method: "POST",
            data: previewObj
        }).done(function(data) {
            $("#spinner").hide();
            $('input[name^="attributeSet"]').each(function() {
                $('#dataTable').find('thead > tr').append($('<td>').text($(this).val()));
            });
            $('#dataTable').show();
            _.each(data.data,function(row) {
                $('#dataTable').find('tbody').append($('<tr>'));
                _.each(row,function(item) {
                    $('#dataTable').find('tbody > tr:last').append($('<td>').text(item.value));
                });
            });
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });

        //reset modal when hidden
        $('#previewModal').on('hidden.bs.modal', function (e) {
            $('#dataTable').find('thead > tr').remove();
            $('#dataTable').find('tbody > tr').remove();
            $('#dataTable').hide();
            $("#spinner").show();
        })
    });
});

$(function() {
    $.get("/dataSource", function(data) {
        _.each(data, function(element,index,list) {
            var obj = JSON.parse(element);
            dataSources.push(obj);
            $("#dataSource").append($('<option value="'+obj.dataSourceID+'">').text(obj.name + " ("+obj.type+")"));
        });
    }).done(function() {
        $("#dataSource").change(function() {
            for (var i=0;i<dataSources.length;++i) {
                if(dataSources[i].dataSourceID == $(this).val()) currDataSource = dataSources[i];
            }
            $("#sparksqlQueryForm").hide(); $("#mongodbQueryForm").hide(); $("#cypherQueryForm").hide(); $("#fileseparatorForm").hide();
            $("#restapiQueryForm").hide(); $("#sqlQueryForm").hide();

            if (currDataSource.type == "avro") query = $("#sparksqlQueryForm").show();
            else if (currDataSource.type == "mongodb") query = $("#mongodbQueryForm").show();
            else if (currDataSource.type == "neo4j") query = $("#cypherQueryForm").show();
            else if (currDataSource.type == "parquet") query = $("#sparksqlQueryForm").show();
            else if (currDataSource.type == "plaintext") query = $("#fileseparatorForm").show();
            else if (currDataSource.type == "restapi") query = $("#restapiQueryForm").show();
            else if (currDataSource.type == "sqldatabase") query = $("#sqlQueryForm").show();
        });
        $("#dataSource").trigger("change");
    });


    $('#submitWrapper').on("click", function(e){
        e.preventDefault();

        var wrapper = new Object();
        wrapper.name = $("#name").val();
        wrapper.dataSourceID = $("#dataSource").val();

        wrapper.attributes = new Array();
        $('input[name^="attributeSet"]').each(function() {
            var attribute = new Object();
            attribute.name = $(this).val();
            attribute.isID = $(this).parent().find('input[type=checkbox]').is(':checked');
            wrapper.attributes.push(attribute);
        });

        if (currDataSource.type == "avro") wrapper.query = $("#sparksqlQuery").val();
        else if (currDataSource.type == "mongodb") wrapper.query = $("#mongodbQuery").val();
        else if (currDataSource.type == "neo4j") wrapper.query = $("#cypherQuery").val();
        else if (currDataSource.type == "parquet") wrapper.query = $("#sparksqlQuery").val();
        else if (currDataSource.type == "plaintext") wrapper.query = $("#fileseparator").val();
        else if (currDataSource.type == "restapi") wrapper.query = $("#restapiQuery").val();
        else if (currDataSource.type == "sqldatabase") query = $("#sqlQuery").val();

        $.ajax({
            url: '/wrapper',
            method: "POST",
            data: wrapper
        }).done(function() {
            window.location.href = '/manage_wrappers';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});