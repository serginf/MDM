/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/datasets/"+encodeURIComponent(getParameterByName("graph")), function(data) {
        _.each(data, function(element,index,list) {
            $("#dataset").append($('<option value="'+element.datasetID+'">').text(element.name + " ("+element.type+")"));
        });
        $("#dataset").select2();
    });


    $('#save').on("click", function(e){
        e.preventDefault();

        var formData = new FormData($('#sourceLevelForm')[0]);
        formData.append('dataset',$('#dataset option:selected').val());

        $.ajax({
            url: '/sourceLevel',
            type: 'POST',
            data:formData,
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_source_levels';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

});