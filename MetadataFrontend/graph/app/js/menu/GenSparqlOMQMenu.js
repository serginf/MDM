module.exports = function (graph) {

    var clearQuery = {},
        clearQueryButton;

    clearQuery.setup = function () {

        clearQueryButton = d3.select("#generate-sparql-omq-button")
            .on("click", function (d) {



                var selection = graph.prepareSelectionObject();
                var graphical_omq = new Object();
                if (selection.length == 0){
                    alert("Select subgraph first");
                    return;
                }else {
                    $("#dataModal").modal("show");
                    graphical_omq.selection = selection;
                    graphical_omq.projectedFeatures = graph.getSelectedFeatures();
                    $.ajax({
                        url: '/OMQ/fromGraphicalToSPARQL',
                        method: "POST",
                        data: graphical_omq
                    }).done(function (res) {
                        var sparql_omq = new Object();
                        sparql_omq.sparql = res.sparql;
                        sparql_omq.namedGraph = graph.options().loadingModule().currentGlobalGraph().namedGraph;
                        sparql_omq.features = graph.getSelectedFeatures();
                        $.ajax({
                            url: '/OMQ/fromSPARQLToRA',
                            method: "POST",
                            data: sparql_omq
                        }).done(function (res) {
                            currOMQ = res;

                            var sql_omq = new Object();
                            sql_omq.sql = currOMQ.sql
                            sql_omq.wrappers = currOMQ.wrappers;
                            sql_omq.features = graph.getSelectedFeatures();

                            $.ajax({
                                url: '/OMQ/fromSQLtoDATA',
                                method: "POST",
                                data: sql_omq
                            }).done(function(data) {
                                $("#spinner").hide();
                                var tableCol = [];
                                _.each(graph.getSelectedFeatures(), function(f) {

                                    var col = new Object();
                                    col.title = f;
                                    col.field = getLastElementURI(f);
                                    col.align = "center";
                                    col.headerFilter = true;
                                    tableCol.push(col);
                                    // $('#dataTable').find('thead > tr').append($('<td>').append($('<b>').text(f)));
                                });
                        

                                $('#dataTable').show();

                                var tabledata = [];
                                _.each(data.data,function(row) {
                                    var rowT = new Object();
                                    _.each(row,function(item) {
                                        rowT[getLastElementURI(item.feature)] = item.value;
                                    });
                                    tabledata.push(rowT);
                                });

                                var table = new Tabulator("#dataTable", {
                                    data:tabledata, //assign data to table
                                    layout:"fitColumns", //fit columns to width of table (optional)
                                    columns:tableCol,
                                    pagination:"local",
                                    paginationSize:10,
                                    selectable:true,
                                    paginationSizeSelector:[5, 10, 15, 20,50,100],
                                });

                                $( "#btnDownload_csv" ).click(function() {
                                    if(table)
                                        table.download("csv", "data.csv", {delimiter:","});
                                });
                                $( "#btnDownload_xlsx" ).click(function() {
                                    if(table)
                                        table.download("xlsx", "data.xlsx", {sheetName:"data"});
                                });

                                $( "#btnDownload_json" ).click(function() {
                                    if(table)
                                        table.download("json", "data.json");
                                });
                                $( "#btnDownload_pdf" ).click(function() {
                                    if(table)
                                        table.download("pdf", "data.pdf", {
                                            orientation:"portrait", //set page orientation to portrait
                                        });
                                });

                            }).fail(function(err) {
                                alert("error "+JSON.stringify(err));
                            });

                            //reset modal when hidden
                            $('#dataModal').on('hidden.bs.modal', function (e) {
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

    function getLastElementURI(ele){
        var parts = ele.split("/");
        if(parts.length >1)
            return parts[parts.length-1]
        else
            ele
    }

    return clearQuery;
};

