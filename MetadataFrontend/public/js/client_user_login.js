$(function() {

    if ($('#hiddenMessage').text()) {
        $('#alertBoxText').text($('#hiddenMessage').text());
        $('#alertBox').show();
    }

    $("body").css("background-color","#F7F7F7");
    $("body").css("padding","20px 100px");

});