/**
 * Created by serginadalfrancesch on 17/05/16.
 */

$(function() {

    $('#userRegisterForm').submit(function(event) {
        var formData = new Object();
        formData.username = $('#username').val();
        formData.password = $('#password').val();
        $.ajax({
            type: 'POST',
            url: '/users',
            data: formData,
            success:function(data, textStatus, jqXHR) {
                window.location.href = '/';
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (errorThrown == "") window.location.href = '/';
            }
        });
        event.preventDefault();
        event.unbind();

    });
});