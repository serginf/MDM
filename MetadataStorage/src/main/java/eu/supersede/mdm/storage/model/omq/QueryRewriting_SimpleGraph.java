package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.ProjectionSet_OLD;
import eu.supersede.mdm.storage.model.omq.relational_operators.RelationalOperator;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.KeyedTuple2;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.BasicPattern;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class QueryRewriting_SimpleGraph {

    //Used for adding triples in-memory
    private void addTriple(Model model, String s, String p, String o) {
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }
    private ResultSet runAQuery(String sparqlQuery, Dataset ds) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private ResultSet runAQuery(String sparqlQuery, InfModel o) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), o)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Dataset T;
    private Set<String> PI;
    // We keep two representations of Q_G.\varphi to simplify its manipulation
    //  1) As a pattern to easily access its triples as a list
    //  2) As an ontology so it can be queried via SPARQL
    private BasicPattern PHI_p; //PHI_pattern
    private InfModel PHI_o; // PHI_ontology

    public QueryRewriting_SimpleGraph(String SPARQL) {
        T = Utils.getTDBDataset(); T.begin(ReadWrite.READ);

        // Compile the SPARQL using ARQ and generate its <pi,phi> representation
        Query q = QueryFactory.create(SPARQL);
        Op ARQ = Algebra.compile(q);

        PI = Sets.newHashSet();
        ((OpTable)((OpJoin)((OpProject)ARQ).getSubOp()).getLeft()).getTable().rows().forEachRemaining(r -> {
            r.vars().forEachRemaining(v -> PI.add(r.get(v).getURI()));
        });

        this.PHI_p = ((OpBGP)((OpJoin)((OpProject)ARQ).getSubOp()).getRight()).getPattern();
        OntModel PHI_o_ontmodel = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            this.addTriple(PHI_o_ontmodel, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );
        Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner(); //RDFS entailment subclass+superclass
        PHI_o = ModelFactory.createInfModel(reasoner,PHI_o_ontmodel);
    }

    public Set<Walk> rewriteAggregations() {
        //Define G_virtual as a copy of G
        InfModel G_virtual = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            this.addTriple(G_virtual, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );

        //Retrieve aggregable features (i.e., those that have an aggregation function)
        Set<String> aggregableFeatures = Sets.newHashSet();
        PI.forEach(f -> {
            this.runAQuery("SELECT ?f WHERE { GRAPH ?g {" +
                "?f <"+ GlobalGraph.HAS_AGGREGATION_FUNCTION.val()+"> ?t } }", T).forEachRemaining(s -> {
                aggregableFeatures.add(s.get("f").toString());
            });
        });
        aggregableFeatures.forEach(f -> {
            //Get parent concept of f
            String parentConcept = this.runAQuery("SELECT ?c WHERE { GRAPH ?g { " +
                    "?c <"+GlobalGraph.HAS_FEATURE.val()+"> <"+f+"> } }",T).next().toString();
            System.out.println(parentConcept + " is parent of "+f);

            //identify the member concepts related to each parent concept

        });

        return null;
        /*
        Set<Walk> out = this.rewrite();
        return out;*/
    }

    @SuppressWarnings("Duplicates")
    public Set<ConjunctiveQuery> rewrite() {
        // ***************************************
        // Phase 1 : Query expansion
        // ***************************************
        // 1 Identify query-related concepts
        // First, create a graph of the pattern in order to obtain its topological sort
        Graph<String,String> conceptsGraph = new SimpleGraph<>(String.class);
        PHI_p.getList().forEach(t -> {
            // Add only concepts so its easier to populate later the list of concepts
            if (!t.getPredicate().getURI().equals(GlobalGraph.HAS_FEATURE.val())) {
                conceptsGraph.addVertex(t.getSubject().getURI());
                conceptsGraph.addVertex(t.getObject().getURI());
                conceptsGraph.addEdge(t.getSubject().getURI(), t.getObject().getURI(), t.getPredicate().getURI());
            }
        });
        // This is required when only one concept is queried, where all edges are hasFeature
        if (conceptsGraph.vertexSet().isEmpty()) {
            conceptsGraph.addVertex(PHI_p.getList().get(0).getSubject().getURI());
        }

        // 2 Expand Q_G with IDs
        conceptsGraph.vertexSet().forEach(c -> {
            //Give me the feature ID for concept c
            ResultSet IDs = this.runAQuery("SELECT ?t " +
                    "WHERE { GRAPH ?g {" +
                    "<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?t . " +
                    "?t <"+ Namespaces.rdfs.val()+"subClassOf> <"+ Namespaces.sc.val()+"identifier> " +
                    "} }", T);
            IDs.forEachRemaining(id -> {
                if (!PHI_p.getList().contains(Triple.create(NodeFactory.createURI(c),
                        NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()),id.get("t").asNode()))) {
                    PHI_p.add(Triple.create(NodeFactory.createURI(c),
                            NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()), id.get("t").asNode()));
                    this.addTriple(PHI_o, c, GlobalGraph.HAS_FEATURE.val(), id.get("t").asNode().getURI());
                }
            });
        });

        // ***************************************
        // Phase 2 : Intra-concept generation
        // ***************************************

        //This graph will hold "partialCQs", which are queries to retrieve the data for each concept
        Graph<KeyedTuple2<String,Set<ConjunctiveQuery>>,String> partialCQsGraph = new SimpleGraph<>(String.class);
        // 3 Identify queried features
        conceptsGraph.vertexSet().forEach(c -> {
            Map<Wrapper,Set<ConjunctiveQuery>> CQsPerWrapper = Maps.newHashMap();
            ResultSet resultSetFeatures = this.runAQuery("SELECT ?f " +
                    "WHERE {<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?f }",PHI_o);
        // 4 Unfold LAV mappings
            Set<String> features = Sets.newHashSet();
            resultSetFeatures.forEachRemaining(f -> features.add(f.get("f").asResource().getURI()));

            features.forEach(f -> {
                ResultSet wrappers = this.runAQuery("SELECT ?g " +
                        "WHERE { GRAPH ?g { <"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> <"+f+"> } }",T);
        // 5 Find attributes in S
                wrappers.forEachRemaining(wRes -> {
                    String w = wRes.get("g").asResource().getURI();
                    // Distinguish the ontology named graph
                    if (!w.equals(Namespaces.T.val()) && w.contains("Wrapper")/*last min bugfix*/) {
                        ResultSet rsAttr = this.runAQuery("SELECT ?a " +
                                "WHERE { GRAPH ?g { ?a <"+Namespaces.owl.val()+"sameAs> <"+f+"> . " +
                                "<"+w+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }", T);
                        String attribute = rsAttr.nextSolution().get("a").asResource().getURI();

                        if (!CQsPerWrapper.containsKey(new Wrapper(w))) {
                            CQsPerWrapper.put(new Wrapper(w), Sets.newHashSet());
                        }
                        ConjunctiveQuery CQ = new ConjunctiveQuery();
                        CQ.setProjections(Sets.newHashSet(attribute));
                        CQ.setWrappers(Sets.newHashSet(new Wrapper(w)));

                        Set<ConjunctiveQuery> currentSet = CQsPerWrapper.get(new Wrapper(w));
                        currentSet.add(CQ);
                        CQsPerWrapper.put(new Wrapper(w), currentSet);

                    }
                });
            });
        // 6 Prune output
            CQsPerWrapper.forEach((wrapper,CQs) -> {
                ConjunctiveQuery mergedCQ = new ConjunctiveQuery();
                mergedCQ.setWrappers(Sets.newHashSet(wrapper));
                mergedCQ.setProjections(Sets.newHashSet());
                CQs.forEach(CQ -> mergedCQ.getProjections().addAll(CQ.getProjections()));

                Set<String> featuresInCQ = Sets.newHashSet();
                CQs.forEach(w -> {
                    w.getProjections().forEach(projection -> {
                        this.runAQuery("SELECT ?f " +
                                "WHERE { GRAPH ?g " +
                                "{<"+projection+"> <"+Namespaces.owl.val()+"sameAs> ?f } }",T).forEachRemaining(featureInWalk -> {
                            featuresInCQ.add(featureInWalk.get("f").asResource().getURI());
                        });
                    });
                });
                if (features.equals(featuresInCQ)) {
                    boolean found = false;
                    for (Tuple2<String,Set<ConjunctiveQuery>> cq : partialCQsGraph.vertexSet()) {
                        if (cq._1.equals(c)) {
                            found = true;
                            cq._2().add(mergedCQ);
                        }
                    }
                    if (!found) partialCQsGraph.addVertex(new KeyedTuple2<>(c,Sets.newHashSet(mergedCQ)));
                }
            });
        });

        //Add edges to the graph of partialCQs
        conceptsGraph.edgeSet().forEach(edge -> {
            KeyedTuple2<String,Set<ConjunctiveQuery>> source = partialCQsGraph.vertexSet().stream().filter(v -> v.equals(conceptsGraph.getEdgeSource(edge))).findFirst().get();
            KeyedTuple2<String,Set<ConjunctiveQuery>> target = partialCQsGraph.vertexSet().stream().filter(v -> v.equals(conceptsGraph.getEdgeTarget(edge))).findFirst().get();
            partialCQsGraph.addEdge(source,target,UUID.randomUUID().toString());
        });

        // ***************************************
        // Phase 3 : Inter-concept generation
        // ***************************************
        /**Assumption here, acyclic graph*/
        while (!partialCQsGraph.edgeSet().isEmpty()) {
            String edge = partialCQsGraph.edgeSet().stream().findAny().get();
            KeyedTuple2<String,Set<ConjunctiveQuery>> source = partialCQsGraph.getEdgeSource(edge);
            KeyedTuple2<String,Set<ConjunctiveQuery>> target = partialCQsGraph.getEdgeTarget(edge);

            KeyedTuple2<String,Set<ConjunctiveQuery>> joinedVertex = new KeyedTuple2<>(source._1+"-"+target._1, Sets.newHashSet());
            for (List<ConjunctiveQuery> CP : Sets.cartesianProduct(source._2,target._2)) {
                // 8 Merge CQs

                if (Collections.disjoint(CP.get(0).getWrappers(),CP.get(1).getWrappers())) {
                    //The partial CQs do not share any wrapper, must discover how to join them
                    //This will add new equijoins

                    //Find ID features for the current set of wrappers in both ends
                    Map<String,Tuple2<Set<Wrapper>,Set<Wrapper>>> IDs_and_their_wrappers = Maps.newHashMap();
                    for (Wrapper w : CP.get(0).getWrappers()) {
                        ResultSet rs = this.runAQuery("SELECT ?f WHERE { " +
                                "GRAPH <"+w.getWrapper()+"> { ?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> } }",T);
                        while (rs.hasNext()) {
                            String ID = rs.next().get("f").toString();
                            if (!IDs_and_their_wrappers.containsKey(ID)) IDs_and_their_wrappers.put(ID,new Tuple2<>(Sets.newHashSet(),Sets.newHashSet()));
                            Set<Wrapper> wrappersForID = IDs_and_their_wrappers.get(ID)._1;
                            wrappersForID.add(w);
                            IDs_and_their_wrappers.put(ID,new Tuple2<>(wrappersForID,IDs_and_their_wrappers.get(ID)._2));
                        }
                    }
                    for (Wrapper w : CP.get(1).getWrappers()) {
                        ResultSet rs = this.runAQuery("SELECT ?f WHERE { " +
                                "GRAPH <"+w.getWrapper()+"> { ?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> } }",T);
                        while (rs.hasNext()) {
                            String ID = rs.next().get("f").toString();
                            if (!IDs_and_their_wrappers.containsKey(ID)) IDs_and_their_wrappers.put(ID,new Tuple2<>(Sets.newHashSet(),Sets.newHashSet()));
                            Set<Wrapper> wrappersForID = IDs_and_their_wrappers.get(ID)._2;
                            wrappersForID.add(w);
                            IDs_and_their_wrappers.put(ID,new Tuple2<>(IDs_and_their_wrappers.get(ID)._1,wrappersForID));
                        }
                    }
                    IDs_and_their_wrappers.entrySet().forEach(entry -> {
                        String feature = entry.getKey();
                        Sets.cartesianProduct(entry.getValue()._1, entry.getValue()._2).forEach(wrapper_combination -> {
                            Wrapper wrapperA = wrapper_combination.get(0);
                            Wrapper wrapperB = wrapper_combination.get(1);

                            String attA = this.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                    "?a <"+Namespaces.owl.val()+"sameAs> <"+feature+"> . " +
                                    "<"+wrapperA.getWrapper()+"> <"+SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                    .nextSolution().get("a").asResource().getURI();
                            String attB = this.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                    "?a <"+Namespaces.owl.val()+"sameAs> <"+feature+"> . " +
                                    "<"+wrapperB.getWrapper()+"> <"+SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                    .nextSolution().get("a").asResource().getURI();

                            ConjunctiveQuery mergedCQ = new ConjunctiveQuery();
                            mergedCQ.getProjections().addAll(CP.get(0).getProjections());
                            mergedCQ.getProjections().addAll(CP.get(1).getProjections());
                            mergedCQ.getJoinConditions().addAll(CP.get(0).getJoinConditions());
                            mergedCQ.getJoinConditions().addAll(CP.get(1).getJoinConditions());
                            mergedCQ.getWrappers().addAll(CP.get(0).getWrappers());
                            mergedCQ.getWrappers().addAll(CP.get(1).getWrappers());
                            mergedCQ.getJoinConditions().add(new EquiJoin(attA,attB));

                            joinedVertex._2.add(mergedCQ);
                        });
                    });
                } else {
                    ConjunctiveQuery mergedCQ = new ConjunctiveQuery();
                    mergedCQ.getProjections().addAll(CP.get(0).getProjections());
                    mergedCQ.getProjections().addAll(CP.get(1).getProjections());
                    mergedCQ.getJoinConditions().addAll(CP.get(0).getJoinConditions());
                    mergedCQ.getJoinConditions().addAll(CP.get(1).getJoinConditions());
                    mergedCQ.getWrappers().addAll(CP.get(0).getWrappers());
                    mergedCQ.getWrappers().addAll(CP.get(1).getWrappers());

                    joinedVertex._2.add(mergedCQ);
                }

            }

            //Remove the processed edge
            partialCQsGraph.removeEdge(edge);
            //Add the new vertex to the graph
            partialCQsGraph.addVertex(joinedVertex);
            //Create edges to the new vertex from those neighbors of source and target
            Graphs.neighborListOf(partialCQsGraph,source).forEach(neighbor -> {
                if (!source.equals(neighbor)) {
                    partialCQsGraph.addEdge(joinedVertex,neighbor,UUID.randomUUID().toString());
                }
            });
            Graphs.neighborListOf(partialCQsGraph,target).forEach(neighbor -> {
                if (!target.equals(neighbor)) {
                    partialCQsGraph.addEdge(joinedVertex,neighbor,UUID.randomUUID().toString());
                }
            });
            partialCQsGraph.removeVertex(source);
            partialCQsGraph.removeVertex(target);

        }
        return partialCQsGraph.vertexSet().stream().findFirst().get()._2;
    }


}
