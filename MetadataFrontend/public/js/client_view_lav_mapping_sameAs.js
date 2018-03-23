/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    $.get("/LAVMapping/"+getParameterByName("LAVMappingID"), function(data) {
        var LAVMapping = (data);

        $.get("/wrapper/"+LAVMapping.wrapperID, function(wrapper) {
            $("#wrapper").val(wrapper.name);
        });

        $.get("/globalGraph/"+LAVMapping.globalGraphID, function(globalGraph) {
            $("#globalGraph").val(globalGraph.name);
        });

        _.each(data.sameAs,function(sameAs) {
            $('#attributes').append($('<input class="form-control" type="text" required="required" readonly="">').val(sameAs.attribute));
            $('#features').append($('<input class="form-control" type="text" required="required" readonly="">').val(sameAs.feature));

        });

    });

});
