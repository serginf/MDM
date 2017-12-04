/**
 * Created by snadal on 29/05/16.
 */

function removeOntology(graph) {
    $.ajax({
        url: 'artifacts/PHYSICAL_ONTOLOGY/'+encodeURIComponent(graph),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}

function notImplemented(graph) {
    alert("Not implemented - How should we handle changes in the graph name?");
}

function getPhysicalOntologies() {
    $.get("/artifacts/PHYSICAL_ONTOLOGY", function(data) {
        var i = 1;
        $.each(data, function(key, value) {
            var theObj = JSON.parse(value);
            $('#physicalOntologies').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.name)
                    ).append($('<td>')
                        .text(theObj.graph)
                    ).append($('<td>')
                        .text(theObj.dataset)
                    ).append($('<td>')
                        .text(theObj.user)
                    ).append($('<td>').append($('<a href="/view_physical_ontology?graph='+encodeURIComponent(theObj.graph)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    //).append($('<td>').append($('<span class="glyphicon glyphicon-edit"></span>'))
                    ).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
                    ).append($('<td>').append($('<a onClick="removeOntology(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getPhysicalOntologies();
});
