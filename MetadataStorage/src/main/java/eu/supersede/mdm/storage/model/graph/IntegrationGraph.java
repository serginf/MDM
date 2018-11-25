package eu.supersede.mdm.storage.model.graph;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple3;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.List;
import java.util.function.Supplier;

public class IntegrationGraph extends SimpleDirectedGraph<String, RelationshipEdge> {

    public IntegrationGraph() {
        super(RelationshipEdge.class);
    }

    public IntegrationGraph(Supplier vSupplier, Supplier eSupplier) {
        super(vSupplier,eSupplier,false);
    }

    public void registerRDFDataset(String namedGraph) {
        List<Tuple3<String,String,String>> triples = Lists.newArrayList();

        this.edgeSet().forEach(edge -> {
            String source = this.getEdgeSource(edge);
            String target = this.getEdgeTarget(edge);
            if (source.contains("Concept") && !source.contains("Feature_id"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val()));
            else if (target.contains("Concept") && !target.contains("Feature_id"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val()));
            else if (source.contains("Feature"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val()));
            else if (target.contains("Feature"))
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(source), Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val()));

            //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(source),RDFUtil.convertToURI(edge.getLabel()),RDFUtil.convertToURI(target));
            triples.add(new Tuple3<>(RDFUtil.convertToURI(source),RDFUtil.convertToURI(edge.getLabel()),RDFUtil.convertToURI(target)));
        });
        RDFUtil.addBatchOfTriples(namedGraph,triples);
    }

    public void printAsWebGraphViz() {
        System.out.print("digraph \"xx\" {");
        System.out.print("size=\"8,5\"");
        this.edgeSet().forEach(edge -> {
            System.out.print("\""+this.getEdgeSource(edge)+"\" -> \""+this.getEdgeTarget(edge)+"\" [label = \""+edge.getLabel()+"\" ];");
        });
        System.out.print("}");
        System.out.println("");
    }

}
