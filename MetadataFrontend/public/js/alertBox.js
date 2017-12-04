/**
 * Created by snadal on 17/05/16.
 */
$(function() {

    if ($('#hiddenMessage').text()) {
        $('#alertBoxText').text($('#hiddenMessage').text());
        $('#alertBox').show();
    }

});