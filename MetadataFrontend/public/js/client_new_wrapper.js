/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getJSONQueryByType() {
    var queryParameters = new Object();
    if (currDataSource.type == "avro") queryParameters.query = $("#sparksqlQuery").val();
    else if (currDataSource.type == "csv") {
        queryParameters.csvColumnDelimiter = $("#csvColumnDelimiter").val();
        queryParameters.csvRowDelimiter = $("#csvRowDelimiter").val();
        queryParameters.headersInFirstRow = $('#headersInFirstRow:checked').is(":checked");
    }
    else if (currDataSource.type == "mongodb") queryParameters.query = $("#mongodbQuery").val();
    else if (currDataSource.type == "neo4j") queryParameters.query = $("#cypherQuery").val();
    else if (currDataSource.type == "parquet") queryParameters.query = $("#sparksqlQuery").val();
    else if (currDataSource.type == "json") {
        //queryParameters.explodeLevels = new Array();
        //$('input[name^="explodeLevels"]').each(function() { queryParameters.explodeLevels.push(($(this).val()));});

        //queryParameters.array = $("#array").val();
        //queryParameters.key = $("#key").val();
        //queryParameters.values = $("#values").val();
        //queryParameters.copyToParent = $("#copyToParent").val();
    }
    else if (currDataSource.type == "restapi") queryParameters.query = $("#restapiQuery").val();
    else if (currDataSource.type == "sql") queryParameters.query = $("#sqlQuery").val();
    return queryParameters;
}

var dataSources = [];
var currDataSource;

$(function(){
    $(document).on('click', '.btn-add', function(e) {
        e.preventDefault();
        var controlForm = $(this).parents('.controls form:first'),
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
    $(document).on('click', '#previewWrapper', function(e) {
        e.preventDefault();

        $("#previewModal").modal("show");

        var previewObj = new Object();
        previewObj.dataSourceID = currDataSource.dataSourceID;
        previewObj.query = JSON.stringify(getJSONQueryByType());

        previewObj.attributes = new Array();
        $('input[name^="attributeSet"]').each(function() { previewObj.attributes.push(($(this).val()));});

        $.ajax({
            url: '/wrapper/preview',
            method: "POST",
            data: previewObj
        }).done(function(data) {

            var tableCol = [];
            var atributesSize = $('input[name^="attributeSet"]').length;
            for(i=0; i<atributesSize;i++){
                var col = new Object();
                col.title = $('input[name^="attributeSet"]')[i].value;
                col.field = $('input[name^="attributeSet"]')[i].value;
                col.align = "center";
                col.headerFilter = true;
                tableCol.push(col);
            }

            var tabledata = [];
            _.each(data.data,function(row) {
                var rowT = new Object();
                _.each(row,function(item) {
                    rowT[item.attribute] = item.value;
                });
                tabledata.push(rowT);
            });

            $("#spinner").hide();
            $('#dataTable').show();

            var table = new Tabulator("#dataTable", {
                data:tabledata, //assign data to table
                layoutColumnsOnNewData:true,
                autoResize:true,
                columnMinWidth:120,
                selectable:true,
                layout:"fitColumns", //fit columns to width of table (optional)
                columns:tableCol
            });
            table.redraw(true);

            $( "#btnDownload_csv" ).click(function() {
                if(table)
                    table.download("csv", "data.csv", {delimiter:","});
            });
            $( "#btnDownload_xlsx" ).click(function() {
                if(table)
                    table.download("xlsx", "data.xlsx", {sheetName:"data"});
            });

            $( "#btnDownload_json" ).click(function() {
                if(table)
                    table.download("json", "data.json");
            });
            $( "#btnDownload_pdf" ).click(function() {
                if(table)
                    table.download("pdf", "data.pdf", {
                        orientation:"portrait", //set page orientation to portrait
                    });
            });
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });

        //reset modal when hidden
        $('#previewModal').on('hidden.bs.modal', function (e) {
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
            $("#sparksqlQueryForm").hide(); $("#mongodbQueryForm").hide(); $("#cypherQueryForm").hide(); $("#jsonForm").hide();
            $("#restapiQueryForm").hide(); $("#sqlQueryForm").hide(); $("#csvForm").hide();

            if (currDataSource.type == "avro") query = $("#sparksqlQueryForm").show();
            else if (currDataSource.type == "csv") $("#csvForm").show();
            else if (currDataSource.type == "mongodb") query = $("#mongodbQueryForm").show();
            else if (currDataSource.type == "neo4j") query = $("#cypherQueryForm").show();
            else if (currDataSource.type == "parquet") query = $("#sparksqlQueryForm").show();
            else if (currDataSource.type == "json") {
                $("#jsonArrayExplodeForm").show();
                $("#jsonValueToAttribute").show();
            }
            else if (currDataSource.type == "restapi") query = $("#restapiQueryForm").show();
            else if (currDataSource.type == "sql") query = $("#sqlQueryForm").show();
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
        wrapper.query = JSON.stringify(getJSONQueryByType());

        $.ajax({
            url: '/wrapper',
            method: "POST",
            data: wrapper
        }).done(function() {
            window.location.href = '/manage_wrappers';
        }).fail(function(err) {
            // var jsonResponse = JSON.parse(responseText);
            alert(err.responseText);
        });
    });

    $('#inferSchema').on("click", function(e) {
        e.preventDefault();

        var inferObj = new Object();
        inferObj.dataSourceID = $("#dataSource").val();
        inferObj.query = JSON.stringify(getJSONQueryByType());

        $.ajax({
            url: '/wrapper/inferSchema',
            method: "POST",
            data: inferObj
        }).done(function(data) {
            var controlForm = $('input[name="attributeSet[]"]').parents('.controls form:last'),
                 currentEntry = $('input[name="attributeSet[]"]').parents('.entry:last'),
            newEntry = $(currentEntry.clone());
            controlForm.empty();
            _.each(JSON.parse(data.schema), function (att) {
                // var controlForm = $('input[name="attributeSet[]"]').parents('.controls form:last'),
                    currentEntry = $('input[name="attributeSet[]"]').parents('.entry:last');

                    if(currentEntry.length){
                        newEntry = $(currentEntry.clone()).appendTo(controlForm);
                    }else{
                        //there is no inputs
                        newEntry = newEntry.appendTo(controlForm);
                    }

                    newEntry.find('input').val(att);
                controlForm.find('.entry:not(:last) .btn-add')
                    .removeClass('btn-add').addClass('btn-remove')
                    .removeClass('btn-success').addClass('btn-danger')
                    .html('<span class="fa fa-minus"></span>');
            });

        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });

    });

});