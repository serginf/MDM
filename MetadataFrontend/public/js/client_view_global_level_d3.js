function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function euclidean(xA,yA,xB,yB) {
    return Math.sqrt(Math.pow(xB-xA,2)+Math.pow(yB-yA,2));
}

$(window).load(function() {

    d3.json('/artifacts/GLOBAL/'+encodeURIComponent(getParameterByName('graph'))+'/graphical', function (error, graph) {

        var width = $(window).width()*0.75;
        var height = $(window).height()*0.5;
        var nodeRadius = 10;

        outer = d3.select("#globalLevelWrapper").append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("pointer-events", "all");

        outer.append('rect')
            .style('opacity',0)
            .attr('width', "100%")
            .attr('height', "100%")
            .call(d3.behavior.zoom().on("zoom", redraw));

        var svg = outer
            .append('g');

        function redraw() {
            svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");
        }

        force = cola.d3adaptor()
            .linkDistance(75)
            .size([width, height])
            .avoidOverlaps(true);

        force
            .nodes(graph.nodes)
            .links(graph.links)
            .jaccardLinkLengths(120,0.7)
            .start(50);

        svg.append('svg:defs').append('svg:marker')
            .attr('id', 'end-arrow')
            .attr('viewBox', '0 -5 10 10')
            .attr('refX', 6)
            .attr('markerWidth', 7)
            .attr('markerHeight', 7)
            .attr('orient', 'auto')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5');

        var path = svg.selectAll(".link")
            .data(graph.links)
            .enter().append('svg:path')
            .attr('id', function(d,i) { return 'edgepath'+i; })
            .attr('class', 'link');

        var edgelabels = svg.selectAll(".edgelabel")
            .data(graph.links)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attr({'class':'edgelabel',
                'id':function(d,i){return 'edgelabel'+i},
                'dx':function(d,i){ return euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;},
                'dy':20});

        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .text(function(d,i){ return d.name});

        var node = svg.selectAll(".node")
            .data(graph.nodes)
            .enter()
            .append("circle")
            .attr("r", nodeRadius)
            .attr("cx", 10)
            .attr("cx", 10)
            //.style("fill", "#000026") //red
            .call(force.drag);

        var nodeTexts = svg.selectAll("text.label")
            .data(graph.nodes)
            .enter().append("text")
            .attr("class", "label")
            .attr("fill", "white")
            .style("font-size", "11px")
            .attr("value", function (d) {
                return d.name;
            })
            .text(function (d) {
                return d.name;
            });

        force.on("tick", function () {
            node.attr("cx", function (d) { return d.x; })
                .attr("cy", function (d) { return d.y; });

            nodeTexts.attr("transform", function (d) {
                return "translate(" + (d.x - this.getBBox().width/2) + "," + (d.y + 25) + ")";
            });

            path.attr('d', function (d) {
                var deltaX = d.target.x - d.source.x,
                    deltaY = d.target.y - d.source.y,
                    dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY),
                    normX = deltaX / dist,
                    normY = deltaY / dist,
                    sourcePadding = nodeRadius,
                    targetPadding = nodeRadius + 5,
                    sourceX = d.source.x + (sourcePadding * normX),
                    sourceY = d.source.y + (sourcePadding * normY),
                    targetX = d.target.x - (targetPadding * normX),
                    targetY = d.target.y - (targetPadding * normY);
                return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
            });

            edgelabels.attr('transform',function(d,i){
                if (d.target.x<d.source.x){
                    bbox = this.getBBox();
                    rx = bbox.x+bbox.width/2;
                    ry = bbox.y+bbox.height/2;
                    return 'rotate(180 '+rx+' '+ry+')';
                }
                else {
                    return 'rotate(0)';
                }
            });

        });

        force
            .nodes(graph.nodes)
            .links(graph.links)
            .start();

    });
});
