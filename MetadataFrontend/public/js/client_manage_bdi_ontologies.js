/**
 * Created by snadal on 29/05/16.
 */


function getOntologies() {
    $.get("/bdi_ontology", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var ontologyObj = JSON.parse(value);

            var releases = "";
            $.each(ontologyObj.releasesData, function(k, v) {
               releases += "<a href='/view_release?releaseID="+v.releaseID+"' target='_blank'>"+v.event + " ("+v.schemaVersion+")</a>, ";
            });
            $('#bdi_ontologies').find('tbody')
                .append($('<tr>')
                        .append($('<td>')
                            .text(i)
                        ).append($('<td>')
                            .text(ontologyObj.name)
                        ).append($('<td>')
                            .html(releases)
                        ).append($('<td>').append($('<a href="/view_global_level?graph='+(ontologyObj.globalLevel)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                        ).append($('<td>').append($('<a href="/view_mappings?bdi_ontologyID='+(ontologyObj.bdi_ontologyID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                        ).append($('<td>').append($('<a href="/view_bdi_ontology?graph='+(ontologyObj.O)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                        //).append($('<td>').append($('<a href="/view_eca_rules?graph='+(ontologyObj.rules)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                        //).append($('<td>').append($('<a href="/view_source_level?graph='+(theObj.graph)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))

                        //).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
//                    ).append($('<td>').append($('<a onClick="removeDataset(\''+((theObj.datasetID))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                        )

                );

            releases = releases.substring(0, releases.length-2);
            ++i;
        });
    });
}

$(function() {
    getOntologies();
});
