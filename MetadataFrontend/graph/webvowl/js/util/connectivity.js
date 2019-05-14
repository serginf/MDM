module.exports = (function () {

    var graph = {},
        componentCount;

    graph.isConnected = function(vertex,egdes){

        if(vertex.length==0){
            componentCount = 0;
            return false;
        }

        componentCount = 1;
        var nodes = [];
        //construct adjacency list of graph
        var adjList = {};
        vertex.forEach(function(v){
            var n = new Object();
            n.id = v.id();
            n.visited = false;
            nodes[n.id]=n;
            adjList[v.id()]=[];
        });
        // console.log("nodes");
        // console.log(nodes);
        // console.log("adjList");
        // console.log(adjList);

        egdes.forEach(function(e){
            adjList[e.link().domain().id()].push(nodes[e.link().range().id()]);
            adjList[e.link().range().id()].push(nodes[e.link().domain().id()]);
        });

        //perform DFS on nodes
        var q = [];
        q.push(nodes[Object.keys(nodes)[0]]);

        while(q.length>0){

            var v1 = q.shift();
            var adj = adjList[v1.id];

            for(var i=0; i<adj.length; i++){
                var v2 = adj[i];
                if(v2.visited)
                    continue;
                q.push(v2);
            }

            v1.visited = true;
            //check for unvisited nodes
            if(q.length==0){
                for(var key in nodes){
                    if(!nodes[key].visited){
                        q.push(nodes[key]);
                        componentCount++;
                        break;
                    }
                }
            }
        }
        console.log("components "+componentCount )
        if (componentCount == 1)
            return true
        return false;
    };


    return function () {
        /* Use a function here to keep a consistent style like webvowl.path.to.module()
         * despite having just a single math object. */
        return graph;
    };
})();