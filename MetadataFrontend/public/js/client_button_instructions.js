/**
 * Created by snadal on 07/06/16.
 */

$(function(){
    $("#instructionsButton").on('click', function(e) {
        e.preventDefault();
        if ($("#instructionsButton > svg").attr('class').includes("fa-chevron-circle-down")) {
            $("#instructionsButton > svg").removeClass('fa-chevron-circle-down').addClass("fa-chevron-circle-up")
        } else {
            $("#instructionsButton > svg").removeClass('fa-chevron-circle-up').addClass("fa-chevron-circle-down")
        }
    });
});
