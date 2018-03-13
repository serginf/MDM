/**
 * Created by snadal on 29/05/16.
 */


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
                            .text(globalGraph.defaultNamespace)
                        ).append($('<td>')
                            .text(globalGraph.namedGraph)
                        ).append($('<td>').append($('<a href="/view_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">').append($('<span class="fa fa-search"></span>')))
                        ).append($('<td>').append($('<a href="/edit_global_graph?globalGraphID='+(globalGraph.globalGraphID)+'">').append($('<span class="fa fa-search"></span>')))
                    )

                );

            ++i;
        });
    });
}

$(function() {
    getGlobalGraphs();
});
