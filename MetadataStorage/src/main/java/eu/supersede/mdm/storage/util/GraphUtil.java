package eu.supersede.mdm.storage.util;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.graph.RelationshipEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.Random;
import java.util.Set;

public class GraphUtil {

    public static IntegrationGraph newGraphFromAnotherGraph(Graph<String, RelationshipEdge> sG) {
        IntegrationGraph G = new IntegrationGraph();
        sG.edgeSet().forEach(edge -> {
            G.addVertex(sG.getEdgeSource((RelationshipEdge) edge));
            G.addVertex(sG.getEdgeTarget((RelationshipEdge)edge));
            G.addEdge(sG.getEdgeSource((RelationshipEdge)edge),sG.getEdgeTarget((RelationshipEdge)edge),(RelationshipEdge)edge);
        });
        return G;
    }

    public static String getRandomVertexFromGraph(IntegrationGraph G) {
        Random r = new Random(System.currentTimeMillis());
        return Lists.newArrayList(G.vertexSet()).get(r.nextInt(Lists.newArrayList(G.vertexSet()).size()));
    }

    public static RelationshipEdge getRandomEdge(Set<RelationshipEdge> edges) {
        Random r = new Random(System.currentTimeMillis());
        return Lists.newArrayList(edges).get(r.nextInt(Lists.newArrayList(edges).size()));
    }

    public static IntegrationGraph getRandomSubgraphFromDijkstraPath(IntegrationGraph G) {
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

        IntegrationGraph W = new IntegrationGraph();
        //Build a new graph from the path
        path.getEdgeList().forEach(edge -> {
            W.addVertex(G.getEdgeSource(edge));
            W.addVertex(G.getEdgeTarget(edge));
            W.addEdge(G.getEdgeSource(edge),G.getEdgeTarget(edge),edge);
        });
        return W;
    }

}
