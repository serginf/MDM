/**
 * Created by snadal on 29/05/16.
 */
/*
function removeDataset(dataset) {
    $.ajax({
        url: 'releases/'+(dataset),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}
*/
function getReleases() {
    $.get("/release", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = (value);
            $('#releases').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.event)
                    ).append($('<td>')
                        .text(theObj.schemaVersion)
                    ).append($('<td>')
                        .text(theObj.kafkaTopic)
                    ).append($('<td>')
                        .text(theObj.dispatcherPath ? theObj.dispatcherPath : '-')
                    ).append($('<td>').append($('<a href="/view_release?releaseID='+(theObj.releaseID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td>').append($('<a href="/view_source_level?graph='+(theObj.graph)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getReleases();
});
