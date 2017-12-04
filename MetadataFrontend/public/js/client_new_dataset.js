/**
 * Created by snadal on 30/04/16.
 */
$(document).on('change', '.btn-file :file', function() {
    var input = $(this),
        numFiles = input.get(0).files ? input.get(0).files.length : 1,
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
});

var types = ['typeXML','typeJSON','typeRelationalDB'];
var containers = ['xmlContainer','jsonContainer','relationalDBContainer'];
function showOne(name) {
    var valid = 0;
    _.each(types, function(element, index, list) {
        if (name != element) {
            $("#"+containers[index]).hide();
        } else {
            valid = index;
        }
    });
    $("#"+containers[valid]).show();
}

$(document).ready( function() {
    showOne(types[0]);
    _.each(types, function(element, index, list) {
       $("#"+element).on('click', function() {
           showOne(element);
       });
    });

    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;
        if( input.length ) {
            input.val(log);
        } else {
            if( log ) alert(log);
        }
    });

    $('#saveXML').on("click", function(e){
        e.preventDefault();
        $.ajax({
            url: '/datasets/xml',
            type: 'POST',
            data: new FormData($('#xmlForm')[0]),
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_datasets';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

    $('#saveJSON').on("click", function(e){
        e.preventDefault();
        $.ajax({
            url: '/datasets/json',
            type: 'POST',
            data: new FormData($('#jsonForm')[0]),
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_datasets';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});