package eu.supersede.mdm.storage.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * A class to compute transitive reduction for a jgrapht DAG.
 * The basis for this implementation is streme (URL below), but I have made a variety of changes.
 * It assumed that each vertex of type V has a toString() method which uniquely identifies it.
 * @see <a href="https://code.google.com/p/streme/source/browse/streme/src/streme/lang/ast/analysis/ipda/DependencyGraphParallelizer.java">streme</a>
 * @see <a href="http://en.wikipedia.org/wiki/Transitive_reduction">Transitive Reduction</a>
 * @see <a href="http://en.wikipedia.org/wiki/Dijkstra's_algorithm">Dijkstra's Algorithm</a>
 * @see <a href="http://en.wikipedia.org/wiki/Breadth-first_search">Breadth-First Search</a>
 */
public class TransitiveReduction {

    /**
     * Compute transitive reduction for a DAG.
     * Each vertex is assumed to have a toString() method which uniquely identifies it.
     * @param graph   Graph to compute transitive reduction for
     */
    public static <V, E> void prune(DirectedAcyclicGraph<V, E> graph) {
        ConnectionCache<V, E> cache = new ConnectionCache<V, E>(graph);
        Deque<V> deque = new ArrayDeque<V>(graph.vertexSet());
        while (!deque.isEmpty()) {
            V vertex = deque.pop();
            prune(graph, vertex, cache);
        }
    }

    /** Prune a particular vertex in a DAG, using the passed-in cache. */
    private static <V, E> void prune(DirectedAcyclicGraph<V, E> graph, V vertex, ConnectionCache<V, E> cache) {
        List<V> targets = Graphs.successorListOf(graph, vertex);
        for (int i = 0; i < targets.size(); i++) {
            for (int j = i + 1; j < targets.size(); j++) {
                V child1 = targets.get(i);
                V child2 = targets.get(j);
                if (cache.isConnected(child1, child2)) {
                    E edge = graph.getEdge(vertex, child2);
                    graph.removeEdge(edge);
                }
            }
        }
    }

    /** A cache that stores previously-computed connections between vertices. */
    private static class ConnectionCache<V, E> {
        private DirectedAcyclicGraph<V, E> graph;
        private Map<String, Boolean> map;

        public ConnectionCache(DirectedAcyclicGraph<V, E> graph) {
            this.graph = graph;
            this.map = new HashMap<String, Boolean>(graph.edgeSet().size());
        }

        public boolean isConnected(V startVertex, V endVertex) {
            String key = startVertex.toString() + "-" + endVertex.toString();

            if (!this.map.containsKey(key)) {
                boolean connected = isConnected(this.graph, startVertex, endVertex);
                this.map.put(key, connected);
            }

            return this.map.get(key);
        }

        private static <V, E> boolean isConnected(DirectedAcyclicGraph<V, E> graph, V startVertex, V endVertex) {
            BreadthFirstIterator<V, E> iter = new BreadthFirstIterator<V, E>(graph, startVertex);

            while (iter.hasNext()) {
                V vertex = iter.next();
                if (vertex.equals(endVertex)) {
                    return true;
                }
            }

            return false;
        }
    }

}
