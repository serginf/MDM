package eu.supersede.mdm.storage.util;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class GraphUtil {

    public static IntegrationGraph newGraphFromAnotherGraph(Graph<CQVertex, IntegrationEdge> sG) {
        IntegrationGraph G = new IntegrationGraph();
        sG.edgeSet().forEach(edge -> {
            G.addVertex(sG.getEdgeSource(edge));
            G.addVertex(sG.getEdgeTarget(edge));
            G.addEdge(sG.getEdgeSource(edge),sG.getEdgeTarget(edge),edge);
        });
        return G;
    }

    public static CQVertex getRandomVertexFromGraph(IntegrationGraph G) {
        return G.vertexSet().stream().skip(new Random().nextInt(G.vertexSet().size())).findFirst().orElse(null);
        /*return new CQVertex(Arrays.copyOf(G.vertexSet().toArray(), G.vertexSet().size(), String[].class)
                [new Random().nextInt(G.vertexSet().size())]);*/
    }
/*
    public static IntegrationGraph_old getRandomSubgraphFromDijkstraPath(IntegrationGraph_old G) {
        Random random = new Random(System.currentTimeMillis());
        DijkstraShortestPath<String,RelationshipEdge> dijkstra = new DijkstraShortestPath<>(G);
        String randomSource = Lists.newArrayList(G.vertexSet()).get(random.nextInt(Lists.newArrayList(G.vertexSet()).size()));
        String randomTarget = Lists.newArrayList(G.vertexSet()).get(random.nextInt(Lists.newArrayList(G.vertexSet()).size()));
        //To ensure we do not select the same vertex twice
        while (randomSource.equals(randomTarget)) {
            randomTarget = Lists.newArrayList(G.vertexSet()).get(random.nextInt(Lists.newArrayList(G.vertexSet()).size()));
        }

        GraphPath<String,RelationshipEdge> path = dijkstra.getPath(randomSource,randomTarget) != null ?
                dijkstra.getPath(randomSource,randomTarget) :
                dijkstra.getPath(randomTarget,randomSource);

        IntegrationGraph_old W = new IntegrationGraph_old();
        //Build a new graph from the path
        path.getEdgeList().forEach(edge -> {
            W.addVertex(G.getEdgeSource(edge));
            W.addVertex(G.getEdgeTarget(edge));
            W.addEdge(G.getEdgeSource(edge),G.getEdgeTarget(edge),edge);
        });
        return W;
    }
*/

    public static IntegrationEdge getRandomEdge(Set<IntegrationEdge> edges) {
        Random r = new Random(System.currentTimeMillis());
        return Lists.newArrayList(edges).get(r.nextInt(Lists.newArrayList(edges).size()));
    }

}
