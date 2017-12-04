/**
 * Created by snadal on 07/06/16.
 */

var tabCount = 0;
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function registerCloseEvent() {
    $(".closeTab").click(function () {
        var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $('#tabPanel a:last').tab('show'); // Select first tab
        $(tabContentId).remove(); //remove respective tab content
         --tabCount;
    });
}

function getEvents() {
    var patterns = $("#bdiOntology").val();
    $("#event"+tabCount).empty().trigger('change');
    for (i = 0; i < patterns.length; ++i) {
        $.get("/release/"+patterns[i], function(release) {
            $("#event"+tabCount).append($('<option value="' + release.releaseID + '">').text(release.event));
        });
        $("#event"+tabCount).select2({
            theme: "bootstrap"
        });
    }
}

function getComparators() {
    $.get("/eca_rule_predicate_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#comparator"+tabCount).append($('<option value="'+element.key+'">').text(element.key + " (" + element.val + ")"));
        });
        $("#comparator"+tabCount).select2({
            theme: "bootstrap"
        });
    });
}

$(window).load(function() {
    $('#tabPanel li:first').tab('show'); // Select first tab

    $("#bdiOntology").on("select2:select", function (evt) {
        var element = evt.params.data.element;
        var $element = $(element);

        $element.detach();
        $(this).append($element);
     //   $(this).trigger("change");
    });

    $.get("/release", function(data) {
        $.each((data), function(key, value) {
            var obj = (value);
            $("#bdiOntology").append($('<option value="'+obj.releaseID+'">').text(obj.event));
        });
        $("#bdiOntology").select2({
            theme: "bootstrap"
        })
        $("#bdiOntology").trigger('change');
    });

    $("#bdiOntology").on('change', function() {
        $("#actionParameters").empty().trigger('change');
        $(".closeTab").click();
        tabCount = 0;
        var pattern = $("#bdiOntology").val();
        for (i = 0; i <pattern.length; ++i) {
            $.get("/release/"+pattern[i], function(release) {
                $.get("/release/"+encodeURIComponent(release.graph)+"/attributes", function(data) {
                    _.each(JSON.parse(data), function(element,index,list) {
                        $("#actionParameters").append($('<option value="'+element.iri+'">').text(release.event + '.' + element.name +" ("+element.iri+")"));
                    });
                    $("#actionParameters").select2({
                        theme: "bootstrap"
                    });;
                });
            });
        }

    });


    $.get("/eca_rule_action_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#actionType").append($('<option value="'+element.key+'">').text(element.val));
        });
        $("#actionType").select2({
            theme: "bootstrap"
        });
    });

    $('#addSimpleClause').on("click", function(e) {
        e.preventDefault();
        ++tabCount;
        $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Filter '+(tabCount)+'<button type="button" class="close closeTab">&nbsp &times;</button></a></li>'));
        $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill" style="border:1px solid; padding:5px">'+'<div class="form-group"> <label class="col-lg-2 control-label">'+'Name '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="name'+(tabCount)+'" type="text" required="required"> </input></div></div>'+
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Event '+(tabCount)+'</label><div class="col-lg-10"><select class="event" id="event'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Left Operand '+(tabCount)+'</label><div class="col-lg-10"><select id="leftOperator'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Comparator '+(tabCount)+'</label><div class="col-lg-10"><select id="comparator'+(tabCount)+'" style="width:100%"> </select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Right Operand '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="rightOperator'+(tabCount)+'" type="text" required="required"> </input></div></div></div>'));

        getEvents();
        $("#event"+tabCount).change(function(o) {
            $.get("/release/"+$("#event"+tabCount).val(), function(release) {
                $.get("/release/"+encodeURIComponent(release.graph)+"/attributes", function(data) {
                    $("#leftOperator"+tabCount).empty().trigger('change');
                    _.each(JSON.parse(data), function(element,index,list) {
                        $("#leftOperator"+tabCount).append($('<option value="'+element.iri+'">').text(element.name +" ("+element.iri+")"));
                    });
                    $("#leftOperator"+tabCount).select2({
                        theme: "bootstrap"
                    });;
                });
            });
        });
        registerCloseEvent();
        getComparators();
    });

    $('#submitEcaRule').on("click", function(e){
        e.preventDefault();

        var Eca_Rule = new Object();
        Eca_Rule.ruleName = $("#ruleName").val();

        Eca_Rule.pattern = $("#bdiOntology").val();
        /*for(i = 0; i < $("#bdiOntology").val().length; ++i) {
            $.get("/bdi_ontology/"+$("#bdiOntology").val()[i], function(ontology) {
                var p = new Object();
                p.graph = ontology.rules;
                p.globalLevel = ontology.globalLevel;
                Eca_Rule.pattern.push(p);
            });

        }*/
        Eca_Rule.condition = $("#condition").val();

        Eca_Rule.filters = new Array();
        for (i = 1; i <= tabCount; ++i) {
            var sc = new Object();
            sc.name = $("#name"+i).val();
            sc.event = $("#event"+i).val();
            sc.leftOperand = $("#leftOperator"+i).val();
            sc.comparator = $("#comparator"+i).val();
            sc.rightOperand = $("#rightOperator"+i).val();
            Eca_Rule.filters.push(sc);
        }

        Eca_Rule.action = new Object();
        Eca_Rule.action.name = $("#actionName").val();
        Eca_Rule.action.type = $("#actionType").val();
        Eca_Rule.action.parameters = $("#actionParameters").val();

        Eca_Rule.windowTime = $("#windowTime").val();
        Eca_Rule.windowSize = $("#windowSize").val();

        $.ajax({
            url: '/eca_rule',
            type: 'POST',
            data: Eca_Rule
        }).done(function() {
            window.location.href = '/manage_eca_rules';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});