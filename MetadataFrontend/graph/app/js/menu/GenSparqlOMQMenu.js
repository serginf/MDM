module.exports = function (graph) {

    var clearQuery = {},
        clearQueryButton;

    clearQuery.setup = function () {

        clearQueryButton = d3.select("#generate-sparql-omq-button")
            .on("click", function (d) {

                $("#dataModal").modal("show");

                var selection = graph.prepareSelectionObject();
                var graphical_omq = new Object();
                if (selection.length == 0) alert("Select subgraph first");
                else {
                    graphical_omq.selection = selection;
                    graphical_omq.projectedFeatures = $("#projectedFeatures").val();
                    // console.log(graphical_omq.selection);
                    $.ajax({
                        url: '/OMQ/fromGraphicalToSPARQL',
                        method: "POST",
                        data: graphical_omq
                    }).done(function (res) {
                        var sparql_omq = new Object();
                        sparql_omq.sparql = res.sparql;
                        // sparql_omq.namedGraph = currGlobalGraph.namedGraph;
                        sparql_omq.namedGraph = graph.options().loadingModule().currentGlobalGraph().namedGraph;
                        sparql_omq.features = $('#projectedFeatures').val();
                        $.ajax({
                            url: '/OMQ/fromSPARQLToRA',
                            method: "POST",
                            data: sparql_omq
                        }).done(function (res) {
                            currOMQ = res;

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

                        }).fail(function (err) {
                            alert("error " + JSON.stringify(err));
                        });
                    }).fail(function (err) {
                        alert("error " + JSON.stringify(err));
                    });
                }
            });
    };

    clearQuery.hide = function (flag) {
        if(flag){
            d3.select("#c_generate-sparql-omq").style("display","none")
        }else{
            d3.select("#c_generate-sparql-omq").style("display","")
        }
    };

    return clearQuery;
};

