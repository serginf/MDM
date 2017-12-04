/**
 * Created by snadal on 29/05/16.
 */

function removeDataset(dataset) {
    $.ajax({
        url: 'datasets/'+(dataset),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}

function getDatasets() {
    $.get("/datasets", function(data) {
        var i = 1;
        $.each(data, function(key, value) {
            var theObj = (value);
            $('#datasets').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.name)
                    ).append($('<td>')
                        .text(theObj.datasetID)
                    ).append($('<td>')
                        .text(theObj.type)
                    ).append($('<td>')
                        .text(theObj.user)
                    ).append($('<td>').append($('<a href="/view_dataset?datasetID='+(theObj.datasetID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    //).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
                    ).append($('<td>').append($('<a onClick="removeDataset(\''+((theObj.datasetID))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getDatasets();
});
