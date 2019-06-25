/**
 * Created by snadal on 29/05/16.
 */

var selectedGlobalGraphID = "";

function getGlobalGraphs() {
    $.get("/globalGraph", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var globalGraph = JSON.parse(value);

            $('#globalGraphs').find('tbody')
                .append($('<tr>')
                        .append($('<td>')
                            .text(i)
                        ).append($('<td>')
                            .text(globalGraph.name)
                        ).append($('<td>')
                            .text(globalGraph.namedGraph)
                        ).append($('<td>').append($('<a href="/view_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">').append($('<span class="fa fa-search"></span>')))
                        ).append($('<td>').append($('<a href="/edit_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">').append($('<span class="fa fa-search"></span>')))
                        ).append($('<td>').append($('<a onclick="showModal(\''+(globalGraph.globalGraphID)+'\')">').append($('<span class="fa fa-trash"></span>')))
                        )

                );

            ++i;
        });
    });
}

$(function() {
    getGlobalGraphs();
    $("#deleteBtn").click(function (e) {
        e.preventDefault();
        deleteGlobalGraph();
    });
});

function showModal(id){
    selectedGlobalGraphID = id;
    $('#confirm-delete').modal('show');
}

function deleteGlobalGraph(){
    $('#confirm-delete').modal('hide');
    $.ajax({
        url: '/globalGraph/'+selectedGlobalGraphID,
        method: "DELETE"
    }).done(function() {
        window.location.href = '/manage_global_graphs';
    }).fail(function(err) {
        alert("There was a problem deleting the global graph. ");
    });
    selectedGlobalGraphID = "";
}