/**
 * Created by snadal on 29/05/16.
 */

function removeLevel(graph) {
    $.ajax({
        url: 'artifacts/GLOBAL/'+encodeURIComponent(graph),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}

function notImplemented(graph) {
    alert("Not implemented - How should we handle changes in the graph name?");
}

function getGlobalLevels() {
    $.get("/artifacts/GLOBAL", function(data) {
        var i = 1;
        $.each(data, function(key, value) {
            var theObj = JSON.parse(value);
            $('#globalLevels').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.name)
                    ).append($('<td>')
                        .text(theObj.graph)
                    ).append($('<td>')
                        .text(theObj.user)
                    ).append($('<td>').append($('<a href="/view_global_level?graph='+encodeURIComponent(theObj.graph)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    //).append($('<td>').append($('<span class="glyphicon glyphicon-edit"></span>'))
                    ).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
                    ).append($('<td>').append($('<a onClick="removeLevel(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getGlobalLevels();
});
