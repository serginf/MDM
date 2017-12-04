/**
 * Created by snadal on 30/04/16.
 */
$(document).on('change', '.btn-file :file', function() {
    var input = $(this),
        numFiles = input.get(0).files ? input.get(0).files.length : 1,
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
});


$(document).ready( function() {

    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;
        if( input.length ) {
            input.val(log);
        } else {
            if( log ) alert(log);
        }
    });

    $('#save').on("click", function(e){
        e.preventDefault();
        $.ajax({
            url: '/domainOntology',
            type: 'POST',
            data: new FormData($('#domainOntologyForm')[0]),
            //cache: false,
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_domain_ontologies';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
            //window.location.href = '/';
        });

    });
});