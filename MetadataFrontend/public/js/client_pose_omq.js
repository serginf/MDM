function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var globalGraphs = [];
var currGlobalGraph;

var currOMQ;

$(function() {
    $.get("/globalGraph", function(data) {
        _.each(data, function(element) {
            var obj = JSON.parse(element);
            globalGraphs.push(obj);
            $("#globalGraph").append($('<option value="'+obj.globalGraphID+'">').text(obj.name));
        });
    }).done(function() {
        $("#globalGraph").change(function() {
            $("#projectedFeatures").empty().end();
            $("svg").empty().end();
            selection = [];

            for (var i=0;i<globalGraphs.length;++i) {
                if(globalGraphs[i].globalGraphID == $(this).val()) currGlobalGraph = globalGraphs[i];
            }

            $.get("/globalGraph/"+encodeURIComponent(currGlobalGraph.namedGraph)+"/features", function(features) {
                _.each(features,function(feature) {
                    $('#projectedFeatures').append($('<option value="'+feature+'">').text(feature));
                });
                $("#projectedFeatures").select2({
                    theme: "bootstrap"
                });
            });

            drawGraph(currGlobalGraph.globalGraphID);
        });
        $("#globalGraph").trigger("change");
    });
});

$(function() {
    $("#clearQueryButton").on("click", function(e) {
        e.preventDefault();
        d3.selectAll(".selection").remove();
        $("#text").remove();
        $("#labelGeneratedSparql").remove();
        $('pre code').each(function(i, e) {hljs.highlightBlock(e)});
        selection=[];
        $("#projectedFeatures").select2({
            theme: "bootstrap"
        });
        $("#generateRelationalAlgebraButton").addClass("invisible");
        $("#relationalAlgebraArea").addClass("invisible");

    });
});

$(function() {
    $("#generateSparqlButton").on("click", function(e) {
        e.preventDefault();
        var graphical_omq = new Object();
        if (selection.length == 0) alert("Select subgraph first");
        else {
            graphical_omq.selection = selection;
            graphical_omq.projectedFeatures = $("#projectedFeatures").val();
            $.ajax({
                url: '/OMQ/fromGraphicalToSPARQL',
                method: "POST",
                data: graphical_omq
            }).done(function (res) {
                $("#sparqlAreaForm")
                    .append($('<h4 id="labelGeneratedSparql">Generated SPARQL</h4>'))
                    .append($('<pre id="text">').append($('<code class="sql" id="sparqlArea">')));
                $("#sparqlArea").text(res.sparql);
                $('pre code').each(function(i, block) {
                    hljs.highlightBlock(block);
                });
                $("#generateRelationalAlgebraButton").removeClass("invisible");
            }).fail(function (err) {
                alert("error " + JSON.stringify(err));
            });
        }
    });
});

$(function() {
    $("#generateRelationalAlgebraButton").on("click", function(e) {
        e.preventDefault();
        var sparql_omq = new Object();
        sparql_omq.sparql = $("#text").text();
        sparql_omq.namedGraph = currGlobalGraph.namedGraph;

        $.ajax({
            url: '/OMQ/fromSPARQLToRA',
            method: "POST",
            data: sparql_omq
        }).done(function (res) {
            $("#relationalAlgebraAreaForm")
                .append($('<h4 id="labelGeneratedRelationalAlgebra">Rewritten query over the wrappers</h4>'))
                .append($('<pre id="text">').append($('<code class="" id="relationalAlgebraArea">')));
            $("#relationalAlgebraArea").text(res.ra);
            currOMQ = res;
            console.log(currOMQ);
            $('pre code').each(function(i, block) {
                hljs.highlightBlock(block);
            });
            $("#executeQueryButton").removeClass("invisible");
        }).fail(function (err) {
            alert("error " + JSON.stringify(err));
        });
    });
});

$(function(){
    $(document).on('click', '.btn-query', function(e) {
        $("#dataModal").modal("show");
        //$("#spinner").show();

        var sql_omq = new Object();
        sql_omq.sql = currOMQ.sql
        sql_omq.wrappers = currOMQ.wrappers;
        sql_omq.features = $('#projectedFeatures').val();

        $.ajax({
            url: '/OMQ/fromSQLtoDATA',
            method: "POST",
            data: sql_omq
        }).done(function(data) {
            $("#spinner").hide();
            _.each($('#projectedFeatures').val(), function(f) {
                $('#dataTable').find('thead > tr').append($('<td>').append($('<b>').text(f)));
            });
            $('#dataTable').show();
            _.each(data.data,function(row) {
                $('#dataTable').find('tbody').append($('<tr>'));
                _.each(row,function(item) {
                    $('#dataTable').find('tbody > tr:last').append($('<td>').text(item.value));
                });
            });
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });

        //reset modal when hidden
        $('#dataModal').on('hidden.bs.modal', function (e) {
            $('#dataTable').find('thead > tr').remove();
            $('#dataTable').find('tbody > tr').remove();
            $('#dataTable').hide();
            $("#spinner").show();
        });
    });
});

