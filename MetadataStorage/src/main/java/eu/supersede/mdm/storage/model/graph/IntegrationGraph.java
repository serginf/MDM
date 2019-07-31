package eu.supersede.mdm.storage.model.graph;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple3;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.List;
import java.util.function.Supplier;

public class IntegrationGraph extends DefaultDirectedGraph<CQVertex, IntegrationEdge> {

    public IntegrationGraph() {
        super(IntegrationEdge.class);
    }

    public IntegrationGraph(Supplier vSupplier, Supplier eSupplier) {
        super(vSupplier,eSupplier,false);
    }

    public void registerRDFDataset(String namedGraph) {
        List<Tuple3<String,String,String>> triples = Lists.newArrayList();

        this.edgeSet().forEach(edge -> {
            CQVertex source = this.getEdgeSource(edge);
            CQVertex target = this.getEdgeTarget(edge);
            if (source.getLabel().contains("Concept") && !source.getLabel().contains("Feature_id"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source.getLabel()), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val()));
            else if (target.getLabel().contains("Concept") && !target.getLabel().contains("Feature_id"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source.getLabel()), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val()));
            else if (source.getLabel().contains("Feature"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source.getLabel()), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val()));
            else if (target.getLabel().contains("Feature"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source.getLabel()), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val()));

            //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source),RDFUtil.convertToURI(edge.getLabel()),RDFUtil.convertToURI(target));
            triples.add(new Tuple3<>(RDFUtil.convertToURI(source.getLabel()),RDFUtil.convertToURI(edge.getLabel()),RDFUtil.convertToURI(target.getLabel())));
        });
        RDFUtil.addBatchOfTriples(namedGraph,triples);
    }

    public void printAsWebGraphViz() {
        System.out.print("digraph \"xx\" {");
        System.out.print("size=\"8,5\"");
        this.edgeSet().forEach(edge -> {
            String source = this.getEdgeSource(edge).getLabel().replace("Concept","C").replace("Feature","F");
            String target = this.getEdgeTarget(edge).getLabel().replace("Concept","C").replace("Feature","F");
            String label = edge.getLabel().replace("hasFeature","hasF");

            System.out.print("\""+source+"\" -> \""+target+"\" [label = \""+label+"\" ];");
        });
        System.out.print("}");
        System.out.println("");
    }
}
