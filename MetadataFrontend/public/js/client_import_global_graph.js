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

    //Trigger label for filename
    $('#owl_file').on('change',function(){
        var fileName = $(this).val().replace("C:\\fakepath\\", "");;
        $(this).next('.custom-file-label').html(fileName);
    });

    $('#submitGlobalGraph').on("click", function(e){
        e.preventDefault();

        var globalGraph = new FormData();
        globalGraph.append('file',$('#owl_file').prop('files')[0]);
        globalGraph.append("name", $("#name").val());

        $.ajax({
            url: '/globalGraph/import',
            method: "POST",
            data: globalGraph,
            processData: false,
            async: false,
            contentType: false,
        }).done(function() {
                window.location.href = '/manage_global_graphs';
        }).fail(function(err) {
                alert("error "+JSON.stringify(err));
        });

    });

});