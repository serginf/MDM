/**
 * Created by snadal on 29/05/16.
 */

/*
function removeDataset(dataset) {
    $.ajax({
        url: 'releases/'+(dataset),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}
*/

function generateFile(rule) {
    $.get("/eca_rule/" + rule + "/generate_config_file", function (data) {
        var file = new Blob ([data], {type: "text/plain;charset=utf-8"})
        saveAs(file, rule+".ttl");
    });
}

function getRules() {
    $.get("/eca_rule", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = (value);
            console.log(JSON.stringify(value));
            $('#rules').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.ruleName)
                    ).append($('<td>').append($('<a href="/view_eca_rule?eca_ruleID='+(theObj.eca_ruleID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td>').append($('<btn onclick="generateFile(\'' + encodeURIComponent(theObj.graph) +'\')"> </btn>').append($('<span class="glyphicon glyphicon-plus-sign"></span>'))))
                );
            ++i;
        });
    });
}

$(function() {
    getRules();
});


