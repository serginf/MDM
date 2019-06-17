/**
 * Created by snadal on 29/05/16.
 */

var selectLAVMappingID = "";
function getLAVMappings() {
    $.get("/LAVMapping", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var LAVMapping = JSON.parse(value);
            var wrapper;
            var globalGraph;
            $.get("/wrapper/"+LAVMapping.wrapperID, function(wrapperData) {
                wrapper = wrapperData;
            }).done(function () {
                $.get("/globalGraph/"+LAVMapping.globalGraphID, function(globalGraphData) {
                    globalGraph = globalGraphData;
                }).done(function() {
                    $('#LAVMappings').find('tbody')
                        .append($('<tr>')
                            .append($('<td>')
                                .text(i)
                            ).append($('<td>').append($('<a href="/view_wrapper?wrapperID='+(LAVMapping.wrapperID)+'">'+wrapper.name+'</a>'))
                            ).append($('<td>').append($('<a href="/view_global_graph?globalGraphID='+(LAVMapping.globalGraphID)+'">'+globalGraph.name+'</a>'))
                            ).append($('<td>').append($('<a href="/view_lav_mapping_sameAs?LAVMappingID='+(LAVMapping.LAVMappingID)+'">').append($('<span class="fa fa-search"></span>')))
                            ).append($('<td>').append($('<a href="/view_lav_mapping_subgraph?LAVMappingID='+(LAVMapping.LAVMappingID)+'">').append($('<span class="fa fa-search"></span>')))
                            ).append($('<td>').append($('<a onclick="showModal(\''+(LAVMapping.LAVMappingID)+'\')">').append($('<span class="fa fa-trash"></span>')))
                            )
                        );
                    ++i;
                });
            });
        });
    });
}

$(function() {
    getLAVMappings();
    $("#deleteBtn").click(function (e) {
        e.preventDefault();
        deleteLAVMapping();
    });
});

function showModal(id){
    console.log("showing")
    selectLAVMappingID = id;
    $('#confirm-delete').modal('show');
}

function deleteLAVMapping(){
    $('#confirm-delete').modal('hide');
    $.ajax({
        url: '/LAVMapping/'+selectLAVMappingID,
        method: "DELETE"
    }).done(function() {
        window.location.href = '/manage_lav_mappings';
    }).fail(function(err) {
        alert("There was a problem deleting the element. ");
    });
    selectLAVMappingID = "";
}

