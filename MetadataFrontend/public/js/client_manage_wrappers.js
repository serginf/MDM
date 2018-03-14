/**
 * Created by snadal on 29/05/16.
 */


function getWrappers() {
    $.get("/wrapper", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var wrapper = JSON.parse(value);

            var dataSourceName = "";
            $.get("/dataSource/"+wrapper.dataSourceID, function(data) {
                dataSourceName = data.name;
            }).done(function () {
                $('#wrappers').find('tbody')
                    .append($('<tr>')
                        .append($('<td>')
                            .text(i)
                        ).append($('<td>')
                            .text(wrapper.name)
                        ).append($('<td>').append($('<a href="/view_data_source?dataSourceID='+(wrapper.dataSourceID)+'">'+dataSourceName+'</a>'))
                        ).append($('<td>').append($('<a href="/view_wrapper?wrapperID='+(wrapper.wrapperID)+'">').append($('<span class="fa fa-search"></span>')))
                        )
                    );
                ++i;
            });
        });
    });
}

$(function() {
    getWrappers();
});
