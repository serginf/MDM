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

    var width = $(window).width()*0.75;
    var height = $(window).height()*0.75;
    var nodeRadius = 9;

   /* outer = d3.select("#sourceLevelWrapper").append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("pointer-events", "all");

    outer.append('rect')
        .style('opacity',0)
        .attr('width', "100%")
        .attr('height', "100%")

    var svg = outer
        .append('g')
        .attr('id', 'markers');*/

    d3.json('/artifacts/GLOBAL/'+encodeURIComponent(getParameterByName('graph')), function (error, graph) {
        var thisGraph = new Object();
        var jsonObj = JSON.parse(graph.graphicalGraph);

        newNodes = jsonObj.nodes;
        newNodes.forEach(function(e, i) {
            newNodes[i] =  {
                id: e.id,
                iri: e.iri,
                color: e.color,
                name: e.title
            };
        });
        thisGraph.nodes = newNodes;

        newEdges = jsonObj.edges;
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

        /*
         * d3 v4 force graph: http://bl.ocks.org/fancellu/2c782394602a93921faff74e594d1bb1
         */

        var svg = d3.select("svg"),
            node,
            link;

        svg.append('defs').append('marker')
            .attr('id', 'arrowhead')
            .attr('viewBox','-0 -5 10 10')
            .attr('refX', 13)
            .attr('refY', 0)
            .attr('orient', 'auto')
            .attr('markerWidth', 6)
            .attr('markerHeight', 6)
            .attr('xoverflow', 'visible')
            .append('svg:path')
            .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
            .attr('fill', '#aaa')
            .style('stroke','none');

        var simulation = d3.forceSimulation()
            .force("charge", d3.forceManyBody().strength(-500))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collide", d3.forceCollide().radius(function(d) { return d.r + 0.5; }).iterations(20))
            .force("x", d3.forceX(width / 2).strength(.05))
            .force("y", d3.forceY(height / 2).strength(.05))
            .force("link", d3.forceLink().distance(120).strength(1));

        link = svg.selectAll(".link")
            .data(thisGraph.edges)
            .enter()
            .append("line")
            .attr("class", "link")
            .attr('marker-end','url(#arrowhead)')

        link.append("title")
            .text(function (d) {return d.name;});

        edgepaths = svg.selectAll(".edgepath")
            .data(thisGraph.edges)
            .enter()
            .append('path')
            .attr('class', 'edgepath')
            .attr('id', function (d, i) {return 'edgepath' + i})
            .style("pointer-events", "none");

        edgelabels = svg.selectAll(".edgelabel")
            .data(thisGraph.edges)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attr('class', 'edgelabel')
            .attr('id', function (d, i) {return 'edgelabel' + i})
            .attr('font-size', 10)

        edgelabels.append('textPath')
            .attr('fill', '#aaa')
            .attr('xlink:href', function (d, i) {return '#edgepath' + i})
            .style("text-anchor", "middle")
            .style("pointer-events", "none")
            .attr("startOffset", "50%")
            .text(function (d) {return d.name});

        node = svg.selectAll(".node")
            .data(thisGraph.nodes)
            .enter()
            .append("g")
            .attr("class", "node")
            /*.call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended)
            );*/

        node.append("circle")
            .attr("r", nodeRadius)
            .style("fill", function (d) {return d.color;})

        node.append("text")
            .attr("dx", function(d) { return d.name.length/2 - 20})
            .attr("dy", 30)
            .attr('fill', '#000000')
            .style("font-size", "10px")
            .text(function (d) {return d.name;});

        simulation
            .nodes(thisGraph.nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(thisGraph.edges);

        function ticked() {
            link
                .attr("x1", function (d) {return d.source.x;})
                .attr("y1", function (d) {return d.source.y;})
                .attr("x2", function (d) {return d.target.x;})
                .attr("y2", function (d) {return d.target.y;});

            node
                .attr("transform", function (d) {return "translate(" + d.x +  ", " + d.y + ")";});

            edgepaths.attr('d', function (d) {
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

            edgelabels.attr('transform', function (d) {
                if (d.target.x < d.source.x) {
                    var bbox = this.getBBox();

                    rx = bbox.x + bbox.width / 2;
                    ry = bbox.y + bbox.height / 2;
                    return 'rotate(180 ' + rx + ' ' + ry + ')';
                }
                else {
                    return 'rotate(0)';
                }
            });
        }

      /*  function dragstarted(d) {
            if (!d3.event.active) simulation.alphaTarget(0.3).restart()
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }*/
    });

    /*
        Based in Mike Bostock's Line drawing
        https://bl.ocks.org/mbostock/f705fc55e6f26df29354
     */
    var line = d3.line().curve(d3.curveBasis),
        xmin = Number.MAX_VALUE,
        xmax = Number.MIN_VALUE,
        ymin = Number.MAX_VALUE,
        ymax = Number.MIN_VALUE;

    var svg = d3.select("svg")
        .attr("pointer-events", "all")
        .call(d3.drag()
            .container(function() { return this; })
            .subject(function() { var p = [d3.event.x, d3.event.y]; return [p, p]; })
            .on("start", dragstarted));

    function dragstarted() {
       // if (d3.event.shiftKey){
            var d = d3.event.subject,
                active = svg.append("path").datum(d)
                    .attr("stroke", "#ff3f27")
                    .attr("fill", "#ffb4a2")
                    .attr("stroke-width", 3)
                    .style("fill-opacity", 0.2),
                x0 = d3.event.x,
                y0 = d3.event.y;

            d3.event.on("drag", function() {
                var x1 = d3.event.x,
                    y1 = d3.event.y,
                    dx = x1 - x0,
                    dy = y1 - y0;

                if (dx * dx + dy * dy > 100) d.push([x0 = x1, y0 = y1]);
                else d[d.length - 1] = [x1, y1];
                if (x1 < xmin) xmin = x1;
                if (x1 > xmax) xmax = x1;
                if (y1 < ymin) ymin = y1;
                if (y1 > ymax) ymax = y1;
                active.attr("d", line);
                calculateArea();
            });

      //  }

    }

    function calculateArea() {
        console.log(xmin + ", " + xmax + ", " + ymin + ", " + ymax);
        var selection = [];

        newNodes.forEach(function(e, i) {
            if (e.x < xmax && e.x > xmin && e.y < ymax && e.y > ymin) {
                //console.log("inside area");
                selection.push(e);
            }
        });

        newEdges.forEach(function(e) {
            var s = false,
                t = false;
            for (var i = 0; i < selection.length; ++i) {
                if (selection[i] == e.source) s = true;
                if (selection[i] == e.target) t = true;
            }
            if (s && t) selection.push(e);
        })

        console.log(selection);
    }
});