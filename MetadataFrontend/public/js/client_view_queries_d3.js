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

    d3.json('/artifacts/GLOBAL/'+encodeURIComponent(getParameterByName('graph')), function (error, graph) {
        var thisGraph = new Object();
        var jsonObj = JSON.parse(graph.graphicalGraph);

        var newNodes = jsonObj.nodes;
        newNodes.forEach(function(e, i) {
            newNodes[i] =  {
                id: e.id,
                iri: e.iri,
                color: e.color,
                name: e.title
            };
        });
        thisGraph.nodes = newNodes;

        var newEdges = jsonObj.edges;
        newEdges.forEach(function (e, i) {
            newEdges[i] = {
                source: thisGraph.nodes.filter(function (n) {
                    return n.id == e.source;
                })[0],
                target: thisGraph.nodes.filter(function (n) {
                    return n.id == e.target;
                })[0],
                name: e.name
            };
        });
        thisGraph.edges = newEdges;
        console.log(thisGraph);

        var width = $(window).width()*0.75;
        var height = $(window).height()*0.75;
        var nodeRadius = 7;

        outer = d3.select("#sourceLevelWrapper").append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("pointer-events", "all");

        outer.append('rect')
            .style('opacity',0)
            .attr('width', "100%")
            .attr('height', "100%")
            .call(d3.behavior.zoom().on("zoom", redraw));

        var svg = outer
            .append('g')
            .attr('id', 'markers');

        function redraw() {
            svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");
        }

        force = cola.d3adaptor()
            .linkDistance(75)
            .size([width, height])
            .avoidOverlaps(true);

        // A la release les arestes estan guardades com a "links", pero aquí són "edges"
        force
            .nodes(thisGraph.nodes)
            .links(thisGraph.edges)
            .jaccardLinkLengths(120,0.7)
            .start(50);

        var defs = svg.append('svg:defs');

        var marker = defs.selectAll('marker')
            .data(thisGraph.edges)
            .enter()
            .append('svg:marker')
            .attr('id', function(d){ return 'marker_' + d.name})
            .attr('markerWidth', 7)
            .attr('markerHeight', 7)
            .attr('orient', 'auto')
            .attr('refX', 6)
            .attr('viewBox', '0 -5 10 10')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5')
            .attr('fill', '#aaa');

        var path = svg.selectAll(".link")
            .data(thisGraph.edges)
            .enter().append('svg:path')
            .attr('id', function(d,i) { return 'edgepath'+i; })
            .attr('stroke', "#aaa")
            .attr('marker-start', function(d,i){ return 'url(#marker_' + d.name + ')' })
            .attr('marker-end', function(d,i){ return 'url(#marker_' + d.name  + ')' })
            .attr('class', 'link');

        var edgelabels = svg.selectAll(".edgelabel")
            .data(thisGraph.edges)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attr({'class':'edgelabel',
                'id':function(d,i){return 'edgelabel'+i},
                'dx':function(d,i){ return euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;},
                'dy':20,
                'fill':'#aaa'});

        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .text(function(d,i){ return d.name});

        var node = svg.selectAll(".node")
            .data(thisGraph.nodes)
            .enter()
            .append("circle")
            .attr("r", nodeRadius)
            .attr("cx", 10)
            .attr("cx", 10)
            .style("fill", function(d) {
                return d.color;
            })
            .call(force.drag);

        var nodeTexts = svg.selectAll("text.label")
            .data(thisGraph.nodes)
            .enter().append("text")
            .attr("class", "label")
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

        /*
            Based in Mike Bostock's Line drawing
            https://bl.ocks.org/mbostock/f705fc55e6f26df29354
         */
       /* var line = d3.svg.line()
            .curve(d3.curveBasis);

        var svg = d3.select("svg")
            .call(d3.drag()
                .container(function() { return this; })
                .subject(function() { var p = [d3.event.x, d3.event.y]; return [p, p]; })
                .on("start", dragstarted));

        function dragstarted() {
            var d = d3.event.subject,
                active = svg.append("path").datum(d),
                x0 = d3.event.x,
                y0 = d3.event.y;

            d3.event.on("drag", function() {
                var x1 = d3.event.x,
                    y1 = d3.event.y,
                    dx = x1 - x0,
                    dy = y1 - y0;

                if (dx * dx + dy * dy > 100) d.push([x0 = x1, y0 = y1]);
                else d[d.length - 1] = [x1, y1];
                active.attr("d", line);
            });
        }*/

    });
});