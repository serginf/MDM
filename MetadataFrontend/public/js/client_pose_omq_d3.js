function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function euclidean(xA,yA,xB,yB) {
    return Math.sqrt(Math.pow(xB-xA,2)+Math.pow(yB-yA,2));
}

function drawGraph(globalGraphIRI) {
    d3.json('/graph/GLOBAL/'+encodeURIComponent(globalGraphIRI)+'/graphical', function (error, graph) {
        newNodes = graph.nodes;
        newEdges = graph.links;


        var width = $(window).width()*0.9;
        var height = $(window).height()*0.8;
        var nodeRadius = 12;

        outer = d3.select("#globalGraphWrapper").append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("pointer-events", "all");

        var svg = outer
            .append('g')
            .attr('id', 'markers');

        var simulation = d3.forceSimulation()
            .force("charge", d3.forceManyBody().strength(-1100))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collide", d3.forceCollide().radius(function(d) { return d.r + 0.5; }).iterations(20))
            .force("x", d3.forceX(width / 2).strength(.01))
            .force("y", d3.forceY(height / 2).strength(.01))
            .force("link", d3.forceLink().distance(100).strength(1));
/*
        force = cola.d3adaptor()
            .linkDistance(75)
            .size([width, height])
            ;

        force
            .nodes(graph.nodes)
            .links(graph.links)
            .jaccardLinkLengths(120,0.7)
            .avoidOverlaps(true)
            .start(10,15,20);
*/
        //.start(50);

        var defs = svg.append('svg:defs');

        var marker = defs.selectAll('marker')
            .data(graph.links)
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
            .attr('fill', function(d,i) { return d.color});

        var path = svg.selectAll(".link")
            .data(graph.links)
            .enter().append('svg:path')
            .attr('id', function(d,i) { return 'edgepath'+i; })
            .attr('stroke',function(d) {
                return d.color
            })
            .attr("data-type", function (d) {
                return d.iri.toLowerCase().includes("hasfeature") ? "feature" : "concept";
            })
            .attr('marker-start', function(d,i){ return 'url(#marker_' + d.name + ')' })
            .attr('marker-end', function(d,i){ return 'url(#marker_' + d.name  + ')' })
            .attr('class', 'link');


        var edgelabels = svg.selectAll(".edgelabel")
            .data(graph.links)
            .enter()
            .append('text')
            .attr('id', function(d,i) { return 'edgelabel'+i; })
            .style("pointer-events", "none")
            .attr('dx',function(d,i){ return 10;})//euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;})
            .attr('dy',20)
            .attr('fill','#aaa')
            .attr('class','edgelabel');
//            .attr({'class':'edgelabel',
                //'id':function(d,i){return 'edgelabel'+i},
//                'dx':function(d,i){ return euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;},
//                'dy':20,
//                'fill':'#aaa'});

        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .attr("startOffset", "30%")
            .attr("data-type", function (d) {
                return d.name.toLowerCase().includes("hasfeature") ? "feature" : "concept";
            })
            .attr("value", function (d) {
                return d.name;
            })
            .text(function (d) {
                return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);
            });

/*        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .text(function(d,i){ return d.name});
*/
        var node = svg.selectAll(".node")
            .data(graph.nodes)
            .enter()
            .append("circle")
            .attr("r", nodeRadius)
            .attr("cx", 10)
            .attr("cy", 10)
            .attr("data-type", function (d) {
                return d.namespace.toLowerCase().includes("concept") ? "concept" : "feature";
            })
            .style("fill", function(d) {
                return d.color;
            });

        var nodeTexts = svg.selectAll("text.label")
            .data(graph.nodes)
            .enter().append("text")
            .attr("class", "label")
            .attr("data-type", function (d) {
                return d.namespace.toLowerCase().includes("concept") ? "concept" : "feature";
            })
            .attr("value", function (d) {
                return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);
            })
            .text(function (d) {
                return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);
            });

        simulation
            .nodes(graph.nodes);
            //.on("tick", ticked);

        simulation.force("link")
            .links(graph.links);

        simulation.on("tick", function () {
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

        var line = d3.line().curve(d3.curveBasis);
        console.log(line);

        selection = [];

        var svg = d3.select("svg")
            .attr("pointer-events", "all")
            .call(d3.drag()
                .container(function() { return this; })
                .subject(function() { var p = [d3.event.x, d3.event.y]; return [p, p]; })
                .on("start", dragstarted));

        convexHull = [];
        function dragstarted() {
            var d = d3.event.subject,
                active = svg.append("path").datum(d)
                    .attr("class","selection")
                    .attr("stroke", "#ff3f27")
                    .attr("fill", "#ffb4a2")
                    .attr("stroke-width", 3)
                    .style("fill-opacity", 0.2),
                x0 = d3.event.x,
                y0 = d3.event.y;
            d3.event.on("drag", function() {
                convexHull.push([d3.event.x,d3.event.y]);
                var x1 = d3.event.x,
                    y1 = d3.event.y,
                    dx = x1 - x0,
                    dy = y1 - y0;
                if (dx * dx + dy * dy > 100) d.push([x0 = x1, y0 = y1]);
                else d[d.length - 1] = [x1, y1];
                active.attr("d", line);
            });
        }

    });
/*
    var width = $(window).width()*0.5;
    var height = $(window).height()*0.65;
    var nodeRadius = 9;

    d3.json('/globalGraph/'+globalGraphID, function (error, graph) {
        thisGraph = new Object();
        var jsonObj = JSON.parse(graph.graphicalGraph);

        newNodes = jsonObj.nodes;
        newNodes.forEach(function(e, i) {
            newNodes[i] =  {
                id: e.id,
                iri: e.iri,
                color: e.color,
                name: e.title,
                namespace: e.namespace
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

        // d3 v4 force graph: http://bl.ocks.org/fancellu/2c782394602a93921faff74e594d1bb1


        var svg = d3.select("svg"),
            node,
            link;

        var simulation = d3.forceSimulation()
            .force("charge", d3.forceManyBody().strength(-1100))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collide", d3.forceCollide().radius(function(d) { return d.r + 0.5; }).iterations(20))
            .force("x", d3.forceX(width / 2).strength(.01))
            .force("y", d3.forceY(height / 2).strength(.01))
            .force("link", d3.forceLink().distance(100).strength(1));


        var marker = svg.selectAll('marker')
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
            .attr('fill', function(d,i) { return d.color});

        var path = svg.selectAll(".link")
            .data(thisGraph.edges)
            .enter().append('svg:path')
            .attr('id', function(d,i) { return 'edgepath'+i; })
            .attr("data-type", function (d) {
                return d.name.toLowerCase().includes("hasfeature") ? "feature" : "concept";
            })
            .attr('stroke', "#aaa")
            .attr('marker-start', function(d,i){ return 'url(#marker_' + d.source.iri + ')' })
            .attr('marker-end', function(d,i){ return 'url(#marker_' + d.target.iri  + ')' })
            .attr('class', 'link');

        var edgelabels = svg.selectAll(".edgelabel")
            .data(thisGraph.edges)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attr('class', 'edgelabel')
            .attr("data-type", function (d) {
                return d.name.toLowerCase().includes("hasfeature") ? "feature" : "concept";
            })
            .attr('id', function(d,i){return 'edgelabel'+i})
            .attr('dx', function(d,i){ return euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;})
            .attr('dy', 20)
            .attr('fill', '#aaa');

        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .attr("startOffset", "30%")
            .attr("data-type", function (d) {
                return d.name.toLowerCase().includes("hasfeature") ? "feature" : "concept";
            })
            .attr("value", function (d) {
                return d.name;
            })
            .text(function (d) {
                return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);
            });

        var node = svg.selectAll(".node")
            .data(thisGraph.nodes)
            .enter()
            .append("circle")
            .attr('id', function(d,i){return d.iri})
            .attr("r", nodeRadius)
            .attr("cx", 10)
            .attr("cy", 10)
            .attr("data-type", function (d) {
                return d.namespace.toLowerCase().includes("concept") ? "concept" : "feature";
            })
            .style("fill", function(d) {
                return d.color;
            });

        var nodeTexts = svg.selectAll("text.label")
            .data(thisGraph.nodes)
            .enter().append("text")
            .attr("data-type", function (d) {
                return d.namespace.toLowerCase().includes("concept") ? "concept" : "feature";
            })
            .attr("class", "label")
            .attr("value", function (d) {
                return d.name;
            })
            .text(function (d) {
                return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);
            });


        simulation
            .nodes(thisGraph.nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(thisGraph.edges);

        function ticked() {
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
        }
    });
*/
    //Based in Mike Bostock's Line drawing
    //    https://bl.ocks.org/mbostock/f705fc55e6f26df29354


/*
    $("#clearSelectionButton").on("click", function(e) {
        e.preventDefault();
        d3.select(".selection").remove();

    });
*/
};