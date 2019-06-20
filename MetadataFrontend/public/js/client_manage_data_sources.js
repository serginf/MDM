/**
 * Created by snadal on 29/05/16.
 */

var selectedDataSourceID = "";

function getDataSources() {
    $.get("/dataSource", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var dataSource = JSON.parse(value);
            var wrappersLength = 0;
            if(dataSource.wrappers){
                wrappersLength = dataSource.wrappers.length;
            }
            $('#dataSources').find('tbody')
                .append($('<tr>')
                        .append($('<td>')
                            .text(i)
                        ).append($('<td>')
                            .text(dataSource.name)
                        ).append($('<td>')
                             .text(dataSource.type)
                        ).append($('<td>')
                            .text(wrappersLength)
                    ).append($('<td>').append($('<a href="/view_data_source?dataSourceID='+(dataSource.dataSourceID)+'">').append($('<span class="fa fa-search"></span>')))
                    ).append($('<td>').append($('<a href="/view_source_graph?iri='+(dataSource.iri)+'">').append($('<span class="fa fa-search"></span>')))
                    ).append($('<td>').append($('<a onclick="showModal(\''+(dataSource.dataSourceID)+'\')">').append($('<span class="fa fa-trash"></span>')))
                    )

                );

            ++i;
        });
    });
}

$(function() {
    getDataSources();
    $("#deleteBtn").click(function (e) {
        e.preventDefault();
        deleteDataSource();
    });
});

function showModal(id){
    console.log("showing")
    selectedDataSourceID = id;
    $('#confirm-delete').modal('show');
}

function deleteDataSource(){
    $('#confirm-delete').modal('hide');
    $.ajax({
        url: '/dataSource/'+selectedDataSourceID,
        method: "DELETE"
    }).done(function() {
        window.location.href = '/manage_data_sources';
    }).fail(function(err) {
        alert("There was a problem deleting the element. ");
    });
    selectedDataSourceID = "";
}

