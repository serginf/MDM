/**
 * Created by snadal on 16/01/17.
 */
var tabCount = 1;

function registerCloseEvent() {
    $(".closeTab").click(function () {
        var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $('#tabPanel a:last').tab('show'); // Select first tab
        $(tabContentId).remove(); //remove respective tab content
    });
}

function openNewTab(name, href, properties) {
    ++tabCount;

    $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+name+'<button type="button" class="close closeTab">&nbsp &times;</button></a></li>'));

    $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill"><iframe id="iframe_'+(tabCount)+'" '+ properties +' src="'+(href)+'" frameborder="0"></iframe></div>'));
    registerCloseEvent();

    $('#tabPanel li:first').tab('show');
}

function getReleases() {
    openNewTab( "Select a tab", "", "seamless scrolling='no' style=\"height: 90%; width : 70%\"");

    $.get("/release", function(data) {
        $.each((data), function(key, value) {
            var theObj = (value);
            openNewTab(theObj.event/* + "("+theObj.schemaVersion+")"*/, "/data_source_statistics?topic="+theObj.kafkaTopic, "seamless scrolling='no' style=\"height: 90%; width : 100%\"");
        });
    });

}

$(function() {
    getReleases();
});