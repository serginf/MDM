package eu.supersede.mdm.storage.model.graph;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.function.Supplier;

public class IntegrationGraph extends DefaultDirectedGraph<CQVertex, IntegrationEdge> {

    public IntegrationGraph() {
        super(IntegrationEdge.class);
    }

    public IntegrationGraph(Supplier vSupplier, Supplier eSupplier) {
        super(vSupplier,eSupplier,false);
    }


}
