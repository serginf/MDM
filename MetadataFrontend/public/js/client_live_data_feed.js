/**topic
 * Created by snadal on 16/01/17.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    var topic = getParameterByName("topic");
    var socket = io("/raw_data");

    socket.on('/raw_data', function (fromSocket) {
        /*console.log(JSON.parse(fromSocket.message).topic)
        console.log(JSON.parse(fromSocket.message).message);*/
        var socketMsg = JSON.parse(fromSocket.message)/*JSON.parse(Object.keys(JSON.parse(fromSocket.message))[0])*/;
        if (socketMsg.topic == topic) {
            var current = new Date();
            var datetime = (current.getHours() < 10? '0' : '') + current.getHours() + ":"
                + (current.getMinutes() < 10? '0' : '') + current.getMinutes() +
                ":" + (current.getSeconds() < 10? '0' : '') + current.getSeconds();


            $('#liveDataFeed').find('tbody')
                .prepend($('<tr>')
                    .append($('<td>')
                        .text(datetime + " - " +socketMsg.message)
                    )
                );
            $("#liveDataFeed tr:eq(10)").remove();
        }
    });

});
