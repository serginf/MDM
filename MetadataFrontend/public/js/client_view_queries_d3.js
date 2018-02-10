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

        /*
         * d3 v4 force graph: http://bl.ocks.org/fancellu/2c782394602a93921faff74e594d1bb1
         */

        var svg = d3.select("svg"),
            width = +svg.attr("width"),
            height = +svg.attr("height"),
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
            .force("link", d3.forceLink().id(function (d) {return d.id;}).distance(100).strength(1))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2));


        var zoom_handler = d3.zoom()
            .on("zoom", zoom_actions);

        zoom_handler(svg);

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
            .attr('fill-opacity', 0)
            .attr('stroke-opacity', 0)
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
            .attr('fill', '#aaa');

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
            .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended)
            );

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
                .attr("transform", function (d) {return "translate(" + d.x + ", " + d.y + ")";});

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

        function dragstarted(d) {
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
        }

        function zoom_actions(){
            svg.attr("transform", d3.event.transform)
        }
    });

    /*
        Based in Mike Bostock's Line drawing
        https://bl.ocks.org/mbostock/f705fc55e6f26df29354
     */
    var line = d3.line();

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
                    .attr("fill", "none")
                    .attr("stroke-width", 3),
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
      //  }

    }


    /*
        Based in Imad Boumzaoued's https://gist.github.com/RiseupDev/b07f7ccc1c499efc24e9
     */
    /*  var dragging = false, drawing = false, startPoint;
    var points = [], g;
    // behaviors
    var dragger = d3.behavior.drag()
        .on('drag', handleDrag)
        .on('dragend', function(d){
            dragging = false;
        });
    svg.on('mouseup', function(){
        if(dragging) return;
        drawing = true;
        startPoint = [d3.mouse(this)[0], d3.mouse(this)[1]];
        if(svg.select('g.drawPoly').empty()) g = svg.append('g').attr('class', 'drawPoly');
        if(d3.event.target.hasAttribute('is-handle')) {
            closePolygon();
            return;
        };
        points.push(d3.mouse(this));
        g.select('polyline').remove();
        var polyline = g.append('polyline').attr('points', points)
            .style('fill', 'none')
            .attr('stroke', '#000');
        for(var i = 0; i < points.length; i++) {
            g.append('circle')
                .attr('cx', points[i][0])
                .attr('cy', points[i][1])
                .attr('r', 4)
                .attr('fill', 'yellow')
                .attr('stroke', '#000')
                .attr('is-handle', 'true')
                .style({cursor: 'pointer'});
        }
    });
    function closePolygon() {
        svg.select('g.drawPoly').remove();
        var g = svg.append('g');
        g.append('polygon')
            .attr('points', points)
            .style('fill', getRandomColor());
        for(var i = 0; i < points.length; i++) {
            var circle = g.selectAll('circles')
                .data([points[i]])
                .enter()
                .append('circle')
                .attr('cx', points[i][0])
                .attr('cy', points[i][1])
                .attr('r', 4)
                .attr('fill', '#FDBC07')
                .attr('stroke', '#000')
                .attr('is-handle', 'true')
                .style({cursor: 'move'})
                .call(dragger);
        }
        points.splice(0);
        drawing = false;
    }
    svg.on('mousemove', function() {
        if(!drawing) return;
        var g = d3.select('g.drawPoly');
        g.select('line').remove();
        var line = g.append('line')
            .attr('x1', startPoint[0])
            .attr('y1', startPoint[1])
            .attr('x2', d3.mouse(this)[0] + 2)
            .attr('y2', d3.mouse(this)[1])
            .attr('stroke', '#53DBF3')
            .attr('stroke-width', 1);
    })
    function handleDrag() {
        if(drawing) return;
        var dragCircle = d3.select(this), newPoints = [], circle;
        dragging = true;
        var poly = d3.select(this.parentNode).select('polygon');
        var circles = d3.select(this.parentNode).selectAll('circle');
        dragCircle
            .attr('cx', d3.event.x)
            .attr('cy', d3.event.y);
        for (var i = 0; i < circles[0].length; i++) {
            circle = d3.select(circles[0][i]);
            newPoints.push([circle.attr('cx'), circle.attr('cy')]);
        }
        poly.attr('points', newPoints);
    }
    function getRandomColor() {
        var letters = '0123456789ABCDEF'.split('');
        var color = '#';
        for (var i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }*/
});