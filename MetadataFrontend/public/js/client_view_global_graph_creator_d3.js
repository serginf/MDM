var current_metamodel_element = "";
var current_metamodel_element_text;
var currentGlobalGraph;

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function errorNotification(text) {
    noty(
        {
            text: text,
            type: 'error',
            animation: {
                open: {height: 'toggle'},
                close: {height: 'toggle'},
                easing: 'swing',
                speed: 500
            },
            timeout: 5000
        }
    );
}

/**
 * Shows the notification for the inserted triple and sends it to the server
 */
function addTriple(s, p, o) {
    var text = "Added triple < " + s + ", " + p + ", " + o + " >";
    noty(
        {
            text: text,
            type: 'success',
            animation: {
                open: {height: 'toggle'},
                close: {height: 'toggle'},
                easing: 'swing',
                speed: 500
            },
            timeout: 5000
        }
    );
    var tripleObj = new Object();
    //tripleObj.namedGraph = currentGlobalGraph.namedGraph;
    tripleObj.s = s; tripleObj.p = p; tripleObj.o = o;
    $.ajax({
        type: "POST",
        url: '/graph/'+encodeURIComponent(currentGlobalGraph.namedGraph)+'/triple/',
        data: tripleObj
    });

}

$(function () {
    // Set color for the metamodel selectors
    //, style="background-color: #33CCCC"
    $("#"+(Global.CONCEPT.name)).css("background-color",Global.CONCEPT.color);
    // $("#"+(Global.MEMBERCONCEPT.name)).css("background-color",Global.MEMBERCONCEPT.color);
    $("#"+(Global.FEATURE_ID.name)).css("background-color",Global.FEATURE_ID.color);
    $("#"+(Global.FEATURE.name)).css("background-color",Global.FEATURE.color);
    // $("#"+(Global.AGGREGATIONFUNCTION.name)).css("background-color",Global.AGGREGATIONFUNCTION.color);
});

$(function () {
    $(".metamodelButton").on("click", function () {
        if (current_metamodel_element != "") current_metamodel_element.removeClass("thick");
        current_metamodel_element = $(this);
        current_metamodel_element_text = $(this).text();
        current_metamodel_element.addClass("thick");
    });
    $(".metamodelButton")[0].click();
});

$(function () {
    $("#upload-input").click();
});

document.onload = (function (d3, saveAs, Blob, undefined) {
    "use strict";
    currentGlobalGraph = JSON.parse($.ajax({
        type: "GET",
        url: "/globalGraph/"+getParameterByName("globalGraphID"),
        async: false
    }).responseText);

    $("#namedGraph").text("Named graph: " + currentGlobalGraph.namedGraph);

    // TODO add user settings
    var consts = {
        defaultTitle: currentGlobalGraph.defaultNamespace
    };
    var settings = {
        appendElSpec: "#graph"
    };
    // define graphcreator object
    var GraphCreator = function (svg, nodes, edges) {
        var thisGraph = this;
        thisGraph.idct = 0;

        thisGraph.nodes = nodes || [];
        thisGraph.edges = edges || [];

        thisGraph.state = {
            selectedNode: null,
            selectedEdge: null,
            mouseDownNode: null,
            mouseDownLink: null,
            justDragged: false,
            justScaleTransGraph: false,
            lastKeyDown: -1,
            shiftNodeDrag: false,
            selectedText: null
        };

        // define arrow markers for graph links
        var defs = svg.append('svg:defs');
        defs.append('svg:marker')
            .attr('id', 'end-arrow')
            .attr('viewBox', '0 -5 10 10')
            .attr('refX', 13)
            .attr('markerWidth', 3.5)
            .attr('markerHeight', 3.5)
            .attr('orient', 'auto')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5');

        // define arrow markers for leading arrow
        defs.append('svg:marker')
            .attr('id', 'mark-end-arrow')
            .attr('viewBox', '0 -5 10 10')
            .attr('refX', 6)
            .attr('markerWidth', 3.5)
            .attr('markerHeight', 3.5)
            .attr('orient', 'auto')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5');

        thisGraph.svg = svg;
        thisGraph.svgG = svg.append("g")
            .classed(thisGraph.consts.graphClass, true);
        var svgG = thisGraph.svgG;

        // displayed when dragging between nodes
        thisGraph.dragLine = svgG.append('svg:path')
            .attr('class', 'link dragline hidden')
            .attr('d', 'M0,0L0,0')
            .style('marker-end', 'url(#mark-end-arrow)');

        // svg nodes and edges
        thisGraph.paths = svgG.append("g").selectAll("g");
        thisGraph.circles = svgG.append("g").selectAll("g");

        thisGraph.drag = d3.behavior.drag()
            .origin(function (d) {
                return {x: d.x, y: d.y};
            })
            .on("drag", function (args) {
                thisGraph.state.justDragged = true;
                thisGraph.dragmove.call(thisGraph, args);
            })
            .on("dragend", function () {
                // todo check if edge-mode is selected
            });

        // listen for key events
        d3.select(window).on("keydown", function () {
            thisGraph.svgKeyDown.call(thisGraph);
        })
            .on("keyup", function () {
                thisGraph.svgKeyUp.call(thisGraph);
            });

        svg.on("mousedown", function (d) {
            thisGraph.svgMouseDown.call(thisGraph, d);
        });

        svg.on("mouseup", function (d) {
            thisGraph.svgMouseUp.call(thisGraph, d);
        });

        // listen for dragging
        var dragSvg = d3.behavior.zoom()
            .on("zoom", function () {
                if (d3.event.sourceEvent.shiftKey) {
                    // TODO  the internal d3 state is still changing
                    return false;
                } else {
                    thisGraph.zoomed.call(thisGraph);
                }
                return true;
            })
            .on("zoomstart", function () {
                var ael = d3.select("#" + thisGraph.consts.activeEditId).node();
                if (ael) {
                    ael.blur();
                }
                if (!d3.event.sourceEvent.shiftKey) d3.select('body').style("cursor", "move");
            })
            .on("zoomend", function () {
                d3.select('body').style("cursor", "auto");
            });

        svg.call(dragSvg).on("dblclick.zoom", null);

        // listen for resize
        window.onresize = function () {
            thisGraph.updateWindow(svg);
        };

        // handle download data
        /**
         * Sergi:
         *
         * Updated to save the current graphical representation of the graph to the MDM Storage
         */
        d3.select("#download-input").on("click", function () {
            var saveEdges = [];
            thisGraph.edges.forEach(function (val, i) {
                saveEdges.push({source: val.source.id, target: val.target.id, name: val.title});
            });
            $.ajax({
                type: "POST",
                url: '/globalGraph/'+currentGlobalGraph.globalGraphID+'/graphicalGraph',
                data: {graphicalGraph: {"nodes": thisGraph.nodes,"edges": saveEdges} }
            });
            alert("Saved graph");
        });

        // handle uploaded data
        d3.select("#upload-input").on("click", function () {
            $.get("/globalGraph/"+getParameterByName("globalGraphID"), function(data) {
                if (data.graphicalGraph) {
                    var graphicalGraph = JSON.parse(data.graphicalGraph);
                    var jsonObj = graphicalGraph;
                    thisGraph.deleteGraph(true);
                    // change values x and y from string to integer
                    for(var i=0;i<jsonObj.nodes.length;i++){
                        jsonObj.nodes[i].x=parseInt(jsonObj.nodes[i].x)
                        jsonObj.nodes[i].y=parseInt(jsonObj.nodes[i].y)
                    }
                    thisGraph.nodes = jsonObj.nodes;
                    console.log(thisGraph.nodes);
                    thisGraph.setIdCt(jsonObj.nodes.length + 1);
                    var newEdges = jsonObj.edges;
                    console.log(jsonObj.edges)
                    newEdges.forEach(function (e, i) {
                        newEdges[i] = {
                            source: thisGraph.nodes.filter(function (n) {
                                return n.id == e.source;
                            })[0],
                            target: thisGraph.nodes.filter(function (n) {
                                return n.id == e.target;
                            })[0],
                            title: e.name
                        };
                    });
                    thisGraph.edges = newEdges;
                    thisGraph.updateGraph();
                }
            });
        });

        d3.select("#hidden-file-upload").on("change", function(){
            /*    if (window.File && window.FileReader && window.FileList && window.Blob) {
                var uploadFile = this.files[0];
                var filereader = new window.FileReader();
                filereader.onload = function(){
                var txtRes = filereader.result;
                // TODO better error handling
                try{
                var jsonObj = JSON.parse(txtRes);
                thisGraph.deleteGraph(true);
                thisGraph.nodes = jsonObj.nodes;
                thisGraph.setIdCt(jsonObj.nodes.length + 1);
                var newEdges = jsonObj.edges;
                newEdges.forEach(function(e, i){
                newEdges[i] = {source: thisGraph.nodes.filter(function(n){return n.id == e.source;})[0],
                target: thisGraph.nodes.filter(function(n){return n.id == e.target;})[0]};
                });
                thisGraph.edges = newEdges;
                thisGraph.updateGraph();
                }catch(err){
                window.alert("Error parsing uploaded file\nerror message: " + err.message);
                return;
                }
                };
                filereader.readAsText(uploadFile);
                } else {
                alert("Your browser won't let you save this graph -- try upgrading your browser to IE 10+ or Chrome or Firefox.");
                }
            */
        });

        /**
         * Graph load from RDF
         */
        d3.select("#loadGraph").on("click", function () {
            $("#upload-input").click();
            /*
            //var jsonObj = JSON.parse($("#loadGraph").text());
            //Rename attribute name to title in nodes
            jsonObj.nodes.forEach(function(e, i) {
                e.title = e.name;
                e.x = 450;
                e.y = 330+i;
                e.id = i;
            });
            //Rename attribute links to edges
            jsonObj.edges = jsonObj.links;
            alert(JSON.stringify(jsonObj));
            thisGraph.deleteGraph(true);
            thisGraph.nodes = jsonObj.nodes;
            thisGraph.setIdCt(jsonObj.nodes.length + 1);
            var newEdges = jsonObj.links;
            newEdges.forEach(function (e, i) {
                newEdges[i] = {
                    source: thisGraph.nodes.filter(function (n) {
                        return n.id == e.source;
                    })[0],
                    target: thisGraph.nodes.filter(function (n) {
                        return n.id == e.target;
                    })[0]
                };
            });
            thisGraph.edges = newEdges;
            thisGraph.updateGraph();*/
        });

        // handle delete graph
        d3.select("#delete-graph").on("click", function () {
            thisGraph.deleteGraph(true);
        });
    };

    GraphCreator.prototype.setIdCt = function (idct) {
        this.idct = idct;
    };

    GraphCreator.prototype.consts = {
        selectedClass: "selected",
        connectClass: "connect-node",
        circleGClass: "conceptG",
        graphClass: "graph",
        activeEditId: "active-editing",
        BACKSPACE_KEY: 8,
        DELETE_KEY: 46,
        ENTER_KEY: 13,
        nodeRadius: 10
    };

    /* PROTOTYPE FUNCTIONS */

    GraphCreator.prototype.dragmove = function (d) {
        var thisGraph = this;
        if (thisGraph.state.shiftNodeDrag) {
            thisGraph.dragLine.attr('d', 'M' + d.x + ',' + d.y + 'L' + d3.mouse(thisGraph.svgG.node())[0] + ',' + d3.mouse(this.svgG.node())[1]);
        } else {
            d.x += d3.event.dx;
            d.y += d3.event.dy;
            thisGraph.updateGraph();
        }
    };

    GraphCreator.prototype.deleteGraph = function (skipPrompt) {
        var thisGraph = this,
            doDelete = true;
        if (!skipPrompt) {
            doDelete = window.confirm("Press OK to delete this graph");
        }
        if (doDelete) {
            thisGraph.nodes = [];
            thisGraph.edges = [];
            thisGraph.updateGraph();
        }
    };

    /* select all text in element: taken from http://stackoverflow.com/questions/6139107/programatically-select-text-in-a-contenteditable-html-element */
    GraphCreator.prototype.selectElementContents = function (el) {
        var range = document.createRange();
        range.selectNodeContents(el);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    };


    /* insert svg line breaks: taken from http://stackoverflow.com/questions/13241475/how-do-i-include-newlines-in-labels-in-d3-charts */
    GraphCreator.prototype.insertTitleLinebreaks = function (gEl, title) {
        var words = title.split(/\s+/g),
            nwords = words.length;
        var el = gEl.append("text")
            .attr("text-anchor", "middle")
            .attr("dy", 25/*"-" + (nwords-1)*7.5*/);

        for (var i = 0; i < words.length; i++) {
            var tspan = el.append('tspan').text(words[i]);
            if (i > 0)
                tspan.attr('x', 0).attr('dy', '15');
        }
    };

    // remove edges associated with a node
    GraphCreator.prototype.spliceLinksForNode = function (node) {
        var thisGraph = this,
            toSplice = thisGraph.edges.filter(function (l) {
                return (l.source === node || l.target === node);
            });
        toSplice.map(function (l) {
            thisGraph.edges.splice(thisGraph.edges.indexOf(l), 1);
        });
    };

    GraphCreator.prototype.replaceSelectEdge = function (d3Path, edgeData) {
        var thisGraph = this;
        d3Path.classed(thisGraph.consts.selectedClass, true);
        if (thisGraph.state.selectedEdge) {
            thisGraph.removeSelectFromEdge();
        }
        thisGraph.state.selectedEdge = edgeData;
    };

    GraphCreator.prototype.replaceSelectNode = function (d3Node, nodeData) {
        var thisGraph = this;
        d3Node.classed(this.consts.selectedClass, true);
        if (thisGraph.state.selectedNode) {
            thisGraph.removeSelectFromNode();
        }
        thisGraph.state.selectedNode = nodeData;
    };

    GraphCreator.prototype.removeSelectFromNode = function () {
        var thisGraph = this;
        thisGraph.circles.filter(function (cd) {
            return cd.id === thisGraph.state.selectedNode.id;
        }).classed(thisGraph.consts.selectedClass, false);
        thisGraph.state.selectedNode = null;
    };

    GraphCreator.prototype.removeSelectFromEdge = function () {
        var thisGraph = this;
        thisGraph.paths.filter(function (cd) {
            return cd === thisGraph.state.selectedEdge;
        }).classed(thisGraph.consts.selectedClass, false);
        thisGraph.state.selectedEdge = null;
    };

    GraphCreator.prototype.pathMouseDown = function (d3path, d) {
        var thisGraph = this,
            state = thisGraph.state;
        d3.event.stopPropagation();
        state.mouseDownLink = d;

        if (state.selectedNode) {
            thisGraph.removeSelectFromNode();
        }

        var prevEdge = state.selectedEdge;
        if (!prevEdge || prevEdge !== d) {
            thisGraph.replaceSelectEdge(d3path, d);
        } else {
            thisGraph.removeSelectFromEdge();
        }
        if (d3.event.shiftKey) {
            state.shiftNodeDrag = d3.event.shiftKey;
            return;
        }
    };

    // mousedown on node
    GraphCreator.prototype.circleMouseDown = function (d3node, d) {
        var thisGraph = this,
            state = thisGraph.state;
        d3.event.stopPropagation();
        state.mouseDownNode = d;
        if (d3.event.shiftKey) {
            state.shiftNodeDrag = d3.event.shiftKey;
            // reposition dragged directed edge
            thisGraph.dragLine.classed('hidden', false)
                .attr('d', 'M' + d.x + ',' + d.y + 'L' + d.x + ',' + d.y);
            return;
        }
    };

    /* place editable text on node in place of svg text */
    GraphCreator.prototype.changeTextOfNode = function (d3node, d) {
        var thisGraph = this,
            consts = thisGraph.consts,
            htmlEl = d3node.node();
        d3node.selectAll("text").remove();
        var nodeBCR = htmlEl.getBoundingClientRect(),
            curScale = nodeBCR.width / consts.nodeRadius,
            placePad = 5 * curScale,
            useHW = curScale > 1 ? nodeBCR.width * 0.71 : consts.nodeRadius * 1.42;
        // replace with editableconent text
        var d3txt = thisGraph.svg.selectAll("foreignObject")
            .data([d])
            .enter()
            .append("foreignObject")
            .attr("x", nodeBCR.left - 190)
            .attr("y", nodeBCR.top - 300)
            .attr("height", 2 * useHW)
            .attr("width", 300)
            .append("xhtml:p")
            .attr("id", consts.activeEditId)
            .attr("contentEditable", "true")
            .text(d.title)
            .on("mousedown", function (d) {
                d3.event.stopPropagation();
            })
            .on("keydown", function (d) {
                d3.event.stopPropagation();
                if (d3.event.keyCode == consts.ENTER_KEY && !d3.event.shiftKey) {
                    this.blur();
                }
            })
            .on("blur", function (d) {
                /** ACTION: NODE CHANGED **/
                // Set localName
                d.title = this.textContent;
                // Set namespace
                d.namespace = Global[current_metamodel_element.attr('id').toUpperCase()].iri;
                // Set full IRI
                d.iri = /*Global[current_metamodel_element.attr('id').toUpperCase()].iri + "/" +*/ this.textContent;

                d.isID = Global[current_metamodel_element.attr('id').toUpperCase()].isID;

                addTriple(d.iri, Namespaces.rdf + "type", d.namespace);
                if (d.isID) {
                    addTriple(d.iri, Namespaces.rdfs + "subClassOf", Namespaces.sc+"identifier");
                }

                d.color = Global[current_metamodel_element.attr('id').toUpperCase()].color;

                thisGraph.insertTitleLinebreaks(d3node, d.title == null ? d.name : d.title);
                d3.select(this.parentElement).remove();
            });

        return d3txt;
    };

    // mouseup on nodes
    GraphCreator.prototype.circleMouseUp = function (d3node, d) {
        var thisGraph = this,
            state = thisGraph.state,
            consts = thisGraph.consts;
        // reset the states
        state.shiftNodeDrag = false;
        d3node.classed(consts.connectClass, false);

        var mouseDownNode = state.mouseDownNode;

        if (!mouseDownNode) return;

        thisGraph.dragLine.classed("hidden", true);

        if (mouseDownNode !== d) {
            // we're in a different node: create new edge for mousedown edge and add to graph
            var newEdge = {source: mouseDownNode, target: d, title: currentGlobalGraph.defaultNamespace};

            if (newEdge.source.namespace == Global.CONCEPT.iri &&
                (newEdge.target.namespace == Global.FEATURE.iri || newEdge.target.namespace == Global.FEATURE_ID.iri)) {
                newEdge.title = Global.HAS_FEATURE.iri;
            }
            /*else if (newEdge.source.namespace == Global.MEMBERCONCEPT.iri && newEdge.target.namespace == Global.FEATURE_ID.iri) {
                newEdge.title = Global.HAS_FEATURE.iri;
            }
            else if (newEdge.source.namespace == Global.MEMBERCONCEPT.iri && newEdge.target.namespace == Global.FEATURE.iri) {
                newEdge.title = Global.HAS_FEATURE.iri;
            }*/
            /*else if (newEdge.source.namespace == Global.MEMBERCONCEPT.iri && newEdge.target.namespace == Global.CONCEPT.iri) {
                newEdge.title = Global.PART_OF.iri;
            }*/
            /*else if (newEdge.source.namespace == Global.MEMBERCONCEPT.iri && newEdge.target.namespace == Global.MEMBERCONCEPT.iri) {
                newEdge.title = Global.PART_OF.iri;
            }*/
            /*else if (newEdge.source.namespace == Global.FEATURE.iri && newEdge.target.namespace == Global.AGGREGATIONFUNCTION.iri) {
                newEdge.title = Global.HAS_AGGREGATIONFUNCTION.iri;
            }*/
            /*else if (newEdge.source.namespace == Global.FEATURE_ID.iri && newEdge.target.namespace == Global.AGGREGATIONFUNCTION.iri) {
                newEdge.title = Global.HAS_AGGREGATIONFUNCTION.iri;
            }*/
            else if (newEdge.source.namespace == Global.CONCEPT.iri && newEdge.target.namespace == Global.CONCEPT.iri
            /*||  newEdge.source.namespace == Global.MEMBERCONCEPT.iri  &&  newEdge.target.namespace == Global.CONCEPT.iri */) {
                //TODO aresta nova
                // shift-clicked node: edit text content
                var thisGraph = this;

                // shift-clicked node: edit text content
                var d3txt = thisGraph.changeTextOfEdge(d3node, newEdge);

                var txtNode = d3txt.node();
                thisGraph.selectElementContents(txtNode);
                txtNode.focus();

            }

            var filtRes = thisGraph.paths.filter(function (d) {
                if (d.source === newEdge.target && d.target === newEdge.source) {
                    thisGraph.edges.splice(thisGraph.edges.indexOf(d), 1);
                }
                return d.source === newEdge.source && d.target === newEdge.target;
            });

            if (!filtRes[0].length) {
                thisGraph.edges.push(newEdge);
                thisGraph.updateGraph();
            }
            /** ACTION: EDGE CHANGED **/

            var edgeType = getGlobalEdge(newEdge.source.namespace, newEdge.target.namespace);

            if (!(newEdge.source.namespace == Global.CONCEPT.iri && newEdge.target.namespace == Global.CONCEPT.iri)) {
                if (edgeType != null) {
                    addTriple(newEdge.source.iri, edgeType, newEdge.target.iri);
                }
                else {
                    errorNotification("You can't create edges from " + newEdge.source.namespace + " to " + newEdge.target.namespace);
                    thisGraph.edges.splice(thisGraph.edges.indexOf(newEdge), 1);
                    state.selectedEdge = null;
                    thisGraph.updateGraph();
                }
            }

        } else {
            // we're in the same node
            if (state.justDragged) {
                // dragged, not clicked
                state.justDragged = false;
            } else {
                // clicked, not dragged
                /*if (d3.event.shiftKey) {
                    // shift-clicked node: edit text content
                    var d3txt = thisGraph.changeTextOfNode(d3node, d);
                    var txtNode = d3txt.node();
                    thisGraph.selectElementContents(txtNode);
                    txtNode.focus();
                } else {*/
                if (state.selectedEdge) {
                    thisGraph.removeSelectFromEdge();
                }
                var prevNode = state.selectedNode;

                if (!prevNode || prevNode.id !== d.id) {
                    thisGraph.replaceSelectNode(d3node, d);
                } else {
                    thisGraph.removeSelectFromNode();
                }
                //}
            }
        }
        state.mouseDownNode = null;
        return;

    }; // end of circles mouseup


    GraphCreator.prototype.changeTextOfEdge = function (d3node, d) {
        var thisGraph = this,
            consts = thisGraph.consts,
            htmlEl = d3node.node();

        d3node.selectAll("text").remove();
        var nodeBCR = htmlEl.getBoundingClientRect(),
            curScale = nodeBCR.width / consts.nodeRadius,
            useHW = curScale > 1 ? nodeBCR.width * 0.71 : consts.nodeRadius * 1.42;
        // replace with editable content text
        var d3txt = thisGraph.svg.selectAll("foreignObject")
            .data([d])
            .enter()
            .append("foreignObject")
            .attr("x", nodeBCR.left/2)
            .attr("y", nodeBCR.bottom/2)
            .attr("height", 2 * useHW)
            .attr("width", 300)
            .append("xhtml:p")
            .attr("id", consts.activeEditId)
            .attr("contentEditable", "true")
            .text(d.title)
            .on("mousedown", function (d) {
                d3.event.stopPropagation();
            })
            .on("keydown", function (d) {
                d3.event.stopPropagation();
                if (d3.event.keyCode == consts.ENTER_KEY && !d3.event.shiftKey) {
                    this.blur();
                }
            })
            .on("blur", function (d) {
                /** ACTION: EDGE CHANGED **/
                // Set localName
                //console.log(this.tsextContent);
                d.title = this.textContent;
                addTriple(d.source.iri, "http://www.BDIOntology.com/Global/" + d.title, d.target.iri);
                d3.select(this.parentElement).remove();
                thisGraph.updateGraph();
                d3node.append("text")
                    .classed("edgelabel", true)
                    .attr("text-anchor", "middle")
                    .attr("style", "pointer-events: none")
                    .attr('transform',function(d,i){
                        return "translate(" + (d.source.x+d.target.x)/2 + "," + (d.source.y+d.target.y)/2 + ")";
                    })
                    .attr("style", "pointer-events: none")
                    .attr("xlink:href" , function(d,i){return 'edgepath'+i})
                    .text(function(d) {
                        return d.title;
                    });
            });

        return d3txt;
    };

    // paths mouse up
    GraphCreator.prototype.pathMouseUp = function (d3node, d) {
        console.log("hola:")
        console.log(d)
        if (d3.event.shiftKey && d.source.namespace == Global.CONCEPT.iri && d.target.namespace == Global.CONCEPT.iri) {
            // shift-clicked node: edit text content
            var thisGraph = this;
            var d3txt = thisGraph.changeTextOfEdge(d3node, d);
            var txtNode = d3txt.node();
            thisGraph.selectElementContents(txtNode);
            txtNode.focus();
        }
    }


    // mousedown on main svg
    GraphCreator.prototype.svgMouseDown = function () {
        this.state.graphMouseDown = true;
    };

    // mouseup on main svg
    GraphCreator.prototype.svgMouseUp = function () {
        var thisGraph = this,
            state = thisGraph.state;
        if (state.justScaleTransGraph) {
            // dragged not clicked
            state.justScaleTransGraph = false;
        }
        else if (state.graphMouseDown && d3.event.shiftKey) {
            // clicked not dragged from svg
            var xycoords = d3.mouse(thisGraph.svgG.node()),
                d = {id: thisGraph.idct++, title: consts.defaultTitle, x: xycoords[0], y: xycoords[1]};
            thisGraph.nodes.push(d);
            thisGraph.updateGraph();
            // make title of text immediently editable
            var d3txt = thisGraph.changeTextOfNode(thisGraph.circles.filter(function (dval) {
                    return dval.id === d.id;
                }), d),
                txtNode = d3txt.node();
            thisGraph.selectElementContents(txtNode);
            txtNode.focus();
        }
        else if (state.shiftNodeDrag) {
            // dragged from node
            state.shiftNodeDrag = false;
            thisGraph.dragLine.classed("hidden", true);
        }
        state.graphMouseDown = false;
    };

    // keydown on main svg
    GraphCreator.prototype.svgKeyDown = function () {
        var thisGraph = this,
            state = thisGraph.state,
            consts = thisGraph.consts;
        // make sure repeated key presses don't register for each keydown
        if (state.lastKeyDown !== -1) return;

        state.lastKeyDown = d3.event.keyCode;
        var selectedNode = state.selectedNode,
            selectedEdge = state.selectedEdge;

        switch (d3.event.keyCode) {
            case consts.BACKSPACE_KEY:
            case consts.DELETE_KEY:
                d3.event.preventDefault();
                if (selectedNode) {
                    thisGraph.nodes.splice(thisGraph.nodes.indexOf(selectedNode), 1);
                    thisGraph.spliceLinksForNode(selectedNode);
                    state.selectedNode = null;
                    thisGraph.updateGraph();
                } else if (selectedEdge) {
                    thisGraph.edges.splice(thisGraph.edges.indexOf(selectedEdge), 1);
                    state.selectedEdge = null;
                    thisGraph.updateGraph();
                }
                break;
        }
    };

    GraphCreator.prototype.svgKeyUp = function () {
        this.state.lastKeyDown = -1;
    };

    // call to propagate changes to graph
    /** Graph creation **/
    GraphCreator.prototype.updateGraph = function () {
        var thisGraph = this,
            consts = thisGraph.consts,
            state = thisGraph.state;

        /*
                paths = d3.selectAll(".link")
                    .data(thisGraph.paths)
                    .enter().append('svg:path')
                    .attr('id', function(d,i) { return 'edgepath'+i; })
                    .attr('stroke',function(d) {
                        return d.color
                    })
                    .attr('marker-start', function(d,i){ return 'url(#marker_' + d.name + ')' })
                    .attr('marker-end', function(d,i){ return 'url(#marker_' + d.name  + ')' })
                    .attr('class', 'link');
                var edgelabels = d3.selectAll(".edgelabel")
                    .data(thisGraph.paths);
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
                    .text(function(d,i){ return d.name});*/

        // update existing nodes
        thisGraph.circles = thisGraph.circles.data(thisGraph.nodes, function (d) {
            return d.id;
        });

        thisGraph.circles
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });

        // add new nodes
        var newGs = thisGraph.circles.enter()
            .append("g");

        newGs.classed(consts.circleGClass, true)
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            })
            .on("mouseover", function (d) {
                if (state.shiftNodeDrag) {
                    d3.select(this).classed(consts.connectClass, true);
                }
            })
            .on("mouseout", function (d) {
                d3.select(this).classed(consts.connectClass, false);
            })
            .on("mousedown", function (d) {
                thisGraph.circleMouseDown.call(thisGraph, d3.select(this), d);
            })
            .on("mouseup", function (d) {
                thisGraph.circleMouseUp.call(thisGraph, d3.select(this), d);
            })
            .call(thisGraph.drag);

        newGs.each(function (d) {
            thisGraph.insertTitleLinebreaks(d3.select(this), d.title);
        });

        /*
        If its a saved element, set the color that was stored
        Otherwise, use the current selected metamodel element
         */
        newGs.append("circle")
            .attr("r", String(consts.nodeRadius))
            .style("fill", function(d) {
                var color = "red";
                thisGraph.circles.each(function(d2) {
                    if (d.id == d2.id) {
                        color = "color" in d ? d.color : $(current_metamodel_element).css('backgroundColor')
                    } else {
                        console.log("No");
                    }
                });
                return color;
            });

        // remove old nodes
        thisGraph.circles.exit().remove();

        thisGraph.paths = thisGraph.paths.data(thisGraph.edges, function (d) {
            return String(d.source.id) + "+" + String(d.target.id) /*+ d.title*/;
        });

        // update existing paths
        thisGraph.paths.select("g path").style('marker-end', 'url(#end-arrow)')
            .classed(consts.selectedClass, function (d) {
                return d === state.selectedEdge;
            })
            .attr("d", function (d) {
                return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
            })

        thisGraph.paths.select("g text").attr("transform", function (d) {
            return "translate(" + (d.source.x+d.target.x)/2 + "," + (d.source.y+d.target.y)/2 + ")";
        })
            .text(function(d) {
                return d.title;
            });

        // add new paths
        var newPaths = thisGraph.paths.enter().append("g");

        newPaths
            .append("path")
            .style('marker-end', 'url(#end-arrow)')
            .classed("link", true)
            .attr("d", function (d) {
                return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
            })
            .on("mousedown", function (d) {
                    thisGraph.pathMouseDown.call(thisGraph, d3.select(this), d);
                }
            ).on("mouseup", function (d) {
                //click event is in path, we select g with parentNode
                thisGraph.pathMouseUp.call(thisGraph, d3.select(this.parentNode), d);
            });

        newPaths
            .append("text")
            .classed("edgelabel", true)
            .attr("text-anchor", "middle")
            .attr("style", "pointer-events: none")
            .attr('transform',function(d,i){
                return "translate(" + (d.source.x+d.target.x)/2 + "," + (d.source.y+d.target.y)/2 + ")";
            })
            .attr("style", "pointer-events: none")
            .attr("xlink:href" , function(d,i){return 'edgepath'+i})
            .text(function(d) {
                return d.title;
            })

        // console.log(thisGraph.paths);

        // remove old links
        thisGraph.paths.exit().remove();
    };

    GraphCreator.prototype.zoomed = function () {
        this.state.justScaleTransGraph = true;
        d3.select("." + this.consts.graphClass)
            .attr("transform", "translate(" + d3.event.translate + ") scale(" + d3.event.scale + ")");
    };

    GraphCreator.prototype.updateWindow = function (svg) {
        var docEl = document.documentElement,
            bodyEl = document.getElementsByTagName('body')[0];
        var x = window.innerWidth || docEl.clientWidth || bodyEl.clientWidth;
        var y = window.innerHeight || docEl.clientHeight || bodyEl.clientHeight;
        svg.attr("width", x).attr("height", y);
    };

    /**** MAIN ****/
    /*
        Sergi 31/03/2019
        Store the d3 json of the graph. This is done by artificially clicking the download-input link.
        This has an event associated to store the "graphicalGraph"
     */
    window.addEventListener('beforeunload', function (e) {
        // Cancel the event
        e.preventDefault();
        // Chrome requires returnValue to be set
        e.returnValue = '';
        $("#download-input").click();
        // return null;
        return undefined;
    });

    var docEl = document.documentElement,
        bodyEl = document.getElementsByTagName('body')[0];

    var width = window.innerWidth || docEl.clientWidth || bodyEl.clientWidth,
        height = window.innerHeight || docEl.clientHeight || bodyEl.clientHeight;

    var xLoc = width / 2 - 25,
        yLoc = 100;

    // initial node data
    var nodes = [];
    var edges = [];


    /** MAIN SVG **/

    var svg = d3.select(settings.appendElSpec).append("svg")
        .attr("width", width)
        .attr("height", height);
    var graph = new GraphCreator(svg, nodes, edges);
    graph.setIdCt(2);
    graph.updateGraph();
})(window.d3, window.saveAs, window.Blob);