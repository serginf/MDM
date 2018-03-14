/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var currDataSource;

$(function(){
    /*
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
    */
});

$(function(){
    $(document).on('click', '.btn-preview', function(e) {
        $("#previewModal").modal("show");
        var query;
        if (currDataSource.type == "file") query = $("#sparksqlQuery").val();
        else if (currDataSource.type == "mongodb") query = $("#mongodbQuery").val();
        else if (currDataSource.type == "sqldatabase") query = $("#sqlQuery").val();
        $.get("/wrapper/preview/"+encodeURIComponent(currDataSource.dataSourceID)+"/"+encodeURIComponent(query), function(data) {
            //$('#releases').find('thead > tr').append()
            $("#spinner").hide();
            $('input[name^="attributeSet"]').each(function() {
                $('#dataTable').find('thead > tr').append($('<td>').text($(this).val()));
            });

            $('#dataTable').show();

            _.each(data.data,function(row) {
                $('#dataTable').find('tbody').append($('<tr>'));
                _.each(row,function(item) {
                    $('#dataTable').find('tbody > tr:last').append($('<td>').text(item));
                });
            });

            console.log(data);
            //$(".modal-body").text(JSON.stringify(data));
        });
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
            $("#sparksqlQueryForm").hide(); $("#mongodbQueryForm").hide(); $("#sqlQueryForm").hide();
            if (currDataSource.type == "file") $("#sparksqlQueryForm").show();
            if (currDataSource.type == "mongodb") $("#mongodbQueryForm").show();
            if (currDataSource.type == "sqldatabase") $("#sqlQueryForm").show();
        });
        $("#dataSource").trigger("change");
    });


    $('#submitWrapper').on("click", function(e){
        e.preventDefault();

        $('input[name^="fieldSet"]').each(function() {
            alert($(this).val());
        });

        /*
        var wrapper = new Object();
        wrapper.name = $("#name").val();
        wrapper.dataSourceID = $("#dataSource").val();

        $.ajax({
            url: '/wrapper',
            method: "POST",
            data: wrapper
        }).done(function() {
            window.location.href = '/manage_wrappers';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
        */
    });

});