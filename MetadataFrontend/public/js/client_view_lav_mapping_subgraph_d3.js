function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function euclidean(xA,yA,xB,yB) {
    return Math.sqrt(Math.pow(xB-xA,2)+Math.pow(yB-yA,2));
}

function drawGraph(globalGraphID) {
    var width = $(window).width()*0.45;
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

    d3.json('/globalGraph/'+globalGraphID, function (error, graph) {
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

        /*
         * d3 v4 force graph: http://bl.ocks.org/fancellu/2c782394602a93921faff74e594d1bb1
         */

        var svg = d3.select("svg"),
            node,
            link;

        svg.append('defs').append('svg:marker')
            .attr('id', 'end-arrow')
            .attr('viewBox','-0 -5 10 10')
            .attr('refX', 8)
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
            .force("charge", d3.forceManyBody().strength(-550))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collide", d3.forceCollide().radius(function(d) { return d.r + 0.5; }).iterations(20))
            .force("x", d3.forceX(width / 2).strength(.05))
            .force("y", d3.forceY(height / 2).strength(.05))
            .force("link", d3.forceLink().distance(140).strength(1));

        var path = svg.selectAll(".link")
            .data(thisGraph.edges)
            .enter().append('svg:path')
            .attr('id', function(d,i) { return 'edgepath'+i; })
            .attr('stroke', "#aaa")
            .attr('marker-end','url(#end-arrow)')
            .attr('class', 'link');

        var edgelabels = svg.selectAll(".edgelabel")
            .data(thisGraph.edges)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attr('class', 'edgelabel')
            .attr('id', function(d,i){return 'edgelabel'+i})
            .attr('dx', function(d,i){ return euclidean(d.source.x,d.source.y,d.target.x,d.target.y)/3-2*d.name.length;})
            .attr('dy', 20)
            .attr('fill', '#aaa');

        edgelabels.append('textPath')
            .attr('xlink:href',function(d,i) {return '#edgepath'+i})
            .style("pointer-events", "none")
            .attr("startOffset", "30%")
            .attr("value", function (d) {
                return d.name;
            })
            .text(function(d,i){ return d.name.substring(d.name.lastIndexOf("/")+1,d.name.length);});

        var node = svg.selectAll(".node")
            .data(thisGraph.nodes)
            .enter()
            .append("circle")
            .attr("r", nodeRadius)
            .attr("cx", 10)
            .attr("cy", 10)
            .style("fill", function(d) {
                return d.color;
            })
            .attr("x",function(d) {
                return d.x;
            })
            .attr("y",function(d) {
                return d.y;
            });

        var nodeTexts = svg.selectAll("text.label")
            .data(thisGraph.nodes)
            .enter().append("text")
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

    /*
        Based in Mike Bostock's Line drawing
        https://bl.ocks.org/mbostock/f705fc55e6f26df29354
     */
    var line = d3.line().curve(d3.curveBasis),
        xmin = Number.MAX_VALUE,
        xmax = Number.MIN_VALUE,
        ymin = Number.MAX_VALUE,
        ymax = Number.MIN_VALUE;

    selection = [];

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
                    .attr("class","selection")
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

            });
            calculateArea(d);
      //  }
    }

    function calculateArea(d) {
        selection = [];

        newNodes.forEach(function (e, i) {
            if (e.x < xmax && e.x > xmin && e.y < ymax && e.y > ymin) {
                //console.log("inside area");
                selection.push(e);
            }
            /* var nxmin = 0,
                 nxmax = 0,
                 nymin = 0,
                 nymax = 0;
             for(var i = 0; i < d.length; ++i) {
                 if (e.x < xmax && e.x > xmin && e.y < ymax && e.y > ymin) {
                     if (e.x < d[i][0]) ++nxmin;
                     //if (e.x > d[0]) ++nxmax;
                     //if (e.y < d[1]) ++nymin;
                     //if (e.y > d[1]) ++nymax;
                 }
             }
             if (nxmin%2 == 0) selection.push(e);*/
        });

        newEdges.forEach(function (e) {
            var s = false,
                t = false;
            for (var i = 0; i < selection.length; ++i) {
                if (selection[i] == e.source) s = true;
                if (selection[i] == e.target) t = true;
            }
            if (s && t) selection.push(e);
        })

        console.log("in selection "+selection);
    }

    $("#clearSelectionButton").on("click", function(e) {
        e.preventDefault();
        d3.select(".selection").remove();

    });

    $("#saveButton").on("click", function(e) {
        e.preventDefault();

        if (selection.length == 0) alert("Select subgraph first");
        else{
            var subGraph = new Object();
            subGraph.selection = selection;
            subGraph.LAVMappingID = getParameterByName("LAVMappingID");
            $.ajax({
                url: '/LAVMapping/subgraph',
                type: 'POST',
                data: subGraph
            }).done(function(res) {
                window.location.href = '/manage_lav_mappings';
            }).fail(function(err) {
                console.log("error "+JSON.stringify(err));
            });
        }
    });
}