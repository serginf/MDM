/**
 * Created by snadal on 29/05/16.
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
                    ).append($('<td id="events_in_last_5_min_'+theObj.kafkaTopic+'">')
                        .text('0')
                    ).append($('<td>').append($('<a href="/view_release?releaseID='+(theObj.releaseID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getReleases();
});

$(function() {
    var events_in_last_5_min_socket = io("/events_in_last_5_min");

    events_in_last_5_min_socket.on('/events_in_last_5_min', function (fromSocket) {
        var socketMsg = JSON.parse(Object.keys(JSON.parse(fromSocket.message))[0]);
        $.each(socketMsg, function(k,v) {
            $('#events_in_last_5_min_'+k).text(v);
        });
    });

});

