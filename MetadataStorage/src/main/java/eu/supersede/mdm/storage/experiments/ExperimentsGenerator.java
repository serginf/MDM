package eu.supersede.mdm.storage.experiments;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.graph.*;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.util.GraphUtil;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple3;
import org.apache.commons.lang3.RandomStringUtils;
import org.jgrapht.generate.CompleteGraphGenerator;

import java.util.*;
import java.util.function.Supplier;

public class ExperimentsGenerator {

    public static IntegrationGraph generateCliqueGraphOfConcepts(int nConcepts) {
        Supplier<CQVertex> vSupplier = new Supplier<CQVertex>() {
            private int id = 0;
            @Override
            public CQVertex get() {
                return new CQVertex("Concept_"+id++);
            }
        };
        Supplier<IntegrationEdge> eSupplier = new Supplier<IntegrationEdge>() {
            private int id = 0;
            @Override
            public IntegrationEdge get() {
                return new IntegrationEdge("Edge_"+id++);
            }
        };


        IntegrationGraph G = new IntegrationGraph(vSupplier, eSupplier);
        CompleteGraphGenerator<CQVertex,IntegrationEdge> generator = new CompleteGraphGenerator<>(nConcepts);
        generator.generateGraph(G);
        return G;
    }

    public static IntegrationGraph addFeatures(IntegrationGraph G, int upperBound, float probabilityOfBeingAdded) {
        IntegrationGraph newG = GraphUtil.newGraphFromAnotherGraph(G);

        Set<CQVertex> concepts = Sets.newHashSet(newG.vertexSet());
        //Everyone gets an ID
        newG.addVertex(new CQVertex(/*Namespaces.sc.val() + "identifier"*/"ID"));
        concepts.forEach(c -> {
            newG.addVertex(new CQVertex(c.getLabel()+"_Feature_id"));
            newG.addEdge(c,new CQVertex(c.getLabel()+"_Feature_id"),new IntegrationEdge(/*GlobalGraph.HAS_FEATURE.val()*/"hasFeature"));
            newG.addEdge(new CQVertex(c.getLabel()+"_Feature_id"),new CQVertex(/*Namespaces.sc.val() + "identifier"*/"ID"),
                    new IntegrationEdge(/*Namespaces.rdfs.val() + "subClassOf"*/"subClassOf"));
        });

        Random random = new Random(System.currentTimeMillis());
        //Add random features up to the upperBound
        concepts.forEach(c -> {
            for (int f = 1; f <= /*random.nextInt(*/upperBound/*)*/; ++f) {
                if (random.nextFloat()<probabilityOfBeingAdded) {
                    newG.addVertex(new CQVertex(c.getLabel() + "_Feature_" + f));
                    newG.addEdge(c, new CQVertex(c.getLabel() + "_Feature_" + f), new IntegrationEdge(/*GlobalGraph.HAS_FEATURE.val()*/"hasFeature"));
                }
            }
        });
        return newG;
    }

    public static IntegrationGraph getConnectedRandomSubgraph(IntegrationGraph G, int coveredEdges, boolean allowCycles) throws Exception {
        if (coveredEdges>G.edgeSet().size()) throw new Exception("coveredEdges cannot be larger than the graphs number of edges");

        CQVertex aVertex = GraphUtil.getRandomVertexFromGraph(G);
        IntegrationGraph res = new IntegrationGraph();
        res.addVertex(aVertex);

        Set<IntegrationEdge> seenEdges = Sets.newHashSet();
        for (int i = 0; i < coveredEdges; ++i) {
            Set<IntegrationEdge> candidateNonSeenEdges = Sets.difference(G.outgoingEdgesOf(aVertex),seenEdges);
            Set<IntegrationEdge> nonSeenEdges = Sets.newHashSet();
            candidateNonSeenEdges.forEach(e ->  {
                if (allowCycles || (!allowCycles && !res.vertexSet().contains(G.getEdgeTarget(e))))
                    nonSeenEdges.add(e);
            });
            IntegrationEdge nonSeenRandomEdge = GraphUtil.getRandomEdge(nonSeenEdges);
            CQVertex target = G.getEdgeTarget(nonSeenRandomEdge);
            res.addVertex(target);
            res.addEdge(aVertex,target,nonSeenRandomEdge);

            seenEdges.add(nonSeenRandomEdge);
            aVertex=target;
        }
        return res;
    }

    public static IntegrationGraph getConnectedRandomSubgraphFromDAG(IntegrationGraph G, int coveredEdges) {
        Random rand = new Random(System.currentTimeMillis());

        IntegrationGraph res = new IntegrationGraph();
        Set<IntegrationEdge> usedEdges = Sets.newHashSet();
        Set<IntegrationEdge> candidateEdges = Sets.newHashSet(G.edgeSet());
        for (int i=0; i < coveredEdges; ++i) {
            IntegrationEdge randEdge = (IntegrationEdge)
                    Lists.newArrayList(candidateEdges.toArray()).get(rand.nextInt(candidateEdges.size()));
            //IntegrationEdge randEdge = candidateEdges.stream().findAny().get();

            res.addVertex(G.getEdgeSource(randEdge));
            res.addVertex(G.getEdgeTarget(randEdge));
            res.addEdge(G.getEdgeSource(randEdge),G.getEdgeTarget(randEdge),randEdge);

            usedEdges.add(randEdge);

            candidateEdges = Sets.newHashSet();
            for (IntegrationEdge usedEdge : usedEdges) {
                for (IntegrationEdge e : G.incomingEdgesOf(G.getEdgeSource(usedEdge))) candidateEdges.add(e);
                for (IntegrationEdge e : G.outgoingEdgesOf(G.getEdgeSource(usedEdge))) candidateEdges.add(e);
                for (IntegrationEdge e : G.incomingEdgesOf(G.getEdgeTarget(usedEdge))) candidateEdges.add(e);
                for (IntegrationEdge e : G.outgoingEdgesOf(G.getEdgeTarget(usedEdge))) candidateEdges.add(e);
            }
            candidateEdges = Sets.difference(candidateEdges,usedEdges);
        }
        return res;
    }

    public static void expandWithOneEdge(IntegrationGraph query, IntegrationGraph G) {
        CQVertex targetV = GraphUtil.getRandomVertexFromGraph(G);
        while (query.containsVertex(targetV)) targetV = GraphUtil.getRandomVertexFromGraph(G);
        CQVertex sourceV = GraphUtil.getRandomVertexFromGraph(query);

        query.addVertex(targetV);
        query.addEdge(sourceV,targetV,G.getEdge(sourceV,targetV));
    }

    public static void registerWrapper(IntegrationGraph W, String namedGraph) {
        String wrapperName = "Wrapper_"+ /*UUID.randomUUID()*/RandomStringUtils.randomAlphabetic(3);
        List<Tuple3<String,String,String>> triples = Lists.newArrayList();

        //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(wrapperName), Namespaces.rdf.val()+"type", SourceGraph.WRAPPER.val());
        triples.add(new Tuple3<>(RDFUtil.convertToURI(wrapperName), Namespaces.rdf.val()+"type", SourceGraph.WRAPPER.val()));
        //sameAs
        W.vertexSet().forEach(v -> {
            if (v.getLabel().contains("Feature")) {
                String attributeName = /*UUID.randomUUID().toString()*/RandomStringUtils.randomAlphabetic(2);
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(attributeName), Namespaces.rdf.val()+"type", SourceGraph.ATTRIBUTE.val());
                triples.add(new Tuple3<>(RDFUtil.convertToURI(attributeName), Namespaces.rdf.val()+"type", SourceGraph.ATTRIBUTE.val()));
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(wrapperName), SourceGraph.HAS_ATTRIBUTE.val(), RDFUtil.convertToURI(attributeName));
                triples.add(new Tuple3<>(RDFUtil.convertToURI(wrapperName), SourceGraph.HAS_ATTRIBUTE.val(), RDFUtil.convertToURI(attributeName)));
                //RDFUtil.addTriple(namedGraph,RDFUtil.convertToURI(attributeName), Namespaces.owl.val() + "sameAs", RDFUtil.convertToURI(v));
                triples.add(new Tuple3<>(RDFUtil.convertToURI(attributeName), Namespaces.owl.val() + "sameAs", RDFUtil.convertToURI(v.getLabel())));
                //System.out.println("    "+wrapperName+" - "+attributeName + " -- sameAs --> "+v);
            }
        });
        RDFUtil.addBatchOfTriples(namedGraph,triples);

        //LAV mapping
        W.registerRDFDataset(RDFUtil.convertToURI(wrapperName));
    }

    public static String convertToSPARQL(IntegrationGraph Q, Map<String, String> prefixes) {
        String SPARQL = prefixes.keySet().stream().map(p -> "PREFIX "+p+": <"+prefixes.get(p)+"> ").reduce(String::concat).get();
        SPARQL += "SELECT ?a" +
                " WHERE { " +
                " VALUES (?a) { (sup:test) } ";
        StringBuilder b = new StringBuilder();
        Q.edgeSet().forEach(e -> {
            CQVertex source = Q.getEdgeSource(e);
            CQVertex target = Q.getEdgeTarget(e);
            b.append("<"+RDFUtil.convertToURI(source.getLabel())+">" + " <" + RDFUtil.convertToURI(e.getLabel()) + "> <" + RDFUtil.convertToURI(target.getLabel()) + "> . ");
        });
        SPARQL+=b.toString()+"}";
        return SPARQL;
    }

}
