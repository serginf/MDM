package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.KeyedTuple2;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Tuple3;
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
import org.jgrapht.graph.SimpleGraph;
import java.util.*;

public class QueryRewriting_SimpleGraph {

    private static void addTriple(BasicPattern pattern, String s, String p, String o) {
        pattern.add(new Triple(new ResourceImpl(s).asNode(), new PropertyImpl(p).asNode(), new ResourceImpl(o).asNode()));
    }
    //Used for adding triples in-memory
    private static void addTriple(Model model, String s, String p, String o) {
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }
    private static ResultSet runAQuery(String sparqlQuery, Dataset ds) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static ResultSet runAQuery(String sparqlQuery, InfModel o) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), o)) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static OntModel ontologyFromPattern(BasicPattern PHI_p) {
        OntModel o = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
                addTriple(o, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );
        return o;
    }

    private static boolean covering(Set<Wrapper> W, BasicPattern PHI_p, Dataset T) {
        BasicPattern coveredPattern = new BasicPattern();
        W.forEach(w -> {
            runAQuery("SELECT ?s ?p ?o WHERE { GRAPH <"+w.getWrapper()+"> { ?s ?p ?o } }",T).forEachRemaining(res -> {
                coveredPattern.add(new Triple(new ResourceImpl(res.get("s").toString()).asNode(),
                        new PropertyImpl(res.get("p").toString()).asNode(), new ResourceImpl(res.get("o").toString()).asNode()));
            });
        });
        return coveredPattern.getList().containsAll(PHI_p.getList());
    }

    private static boolean minimal(Set<Wrapper> W, BasicPattern PHI_p, Dataset T) {
        for (Wrapper w : W) {
            if (covering(Sets.difference(W,Sets.newHashSet(w)),PHI_p,T)) return false;
        }
        return true;
    }

    public static Tuple3<Set<String>, BasicPattern, InfModel> parseSPARQL(String SPARQL, Dataset T) {
        // Compile the SPARQL using ARQ and generate its <pi,phi> representation
        Query q = QueryFactory.create(SPARQL);
        Op ARQ = Algebra.compile(q);

        Set<String> PI = Sets.newHashSet();
        ((OpTable)((OpJoin)((OpProject)ARQ).getSubOp()).getLeft()).getTable().rows().forEachRemaining(r -> {
            r.vars().forEachRemaining(v -> PI.add(r.get(v).getURI()));
        });

        BasicPattern PHI_p = ((OpBGP)((OpJoin)((OpProject)ARQ).getSubOp()).getRight()).getPattern();
        OntModel PHI_o_ontmodel = ontologyFromPattern(PHI_p);
        Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner(); //RDFS entailment subclass+superclass
        InfModel PHI_o = ModelFactory.createInfModel(reasoner,PHI_o_ontmodel);

        return new Tuple3<>(PI,PHI_p,PHI_o);
    }

    public static Set<Walk> rewriteToUnionOfConjunctiveAggregateQueries(Tuple3<Set<String>, BasicPattern, InfModel> queryStructure, Dataset T) {
        Set<String> PI = queryStructure._1;
        BasicPattern PHI_p = queryStructure._2;
        InfModel PHI_o = queryStructure._3;

        //Define G_virtual as a copy of G
        InfModel G_virtual = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            addTriple(G_virtual, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );

        //Retrieve aggregable features (i.e., those that have an aggregation function)
        Set<String> aggregableFeatures = Sets.newHashSet();
        runAQuery("SELECT ?f WHERE { GRAPH ?g {" +
            "?f <"+ GlobalGraph.HAS_AGGREGATION_FUNCTION.val()+"> ?t } }", T).forEachRemaining(f -> {
                String feature = f.get("f").toString();
                    if (PI.contains(feature)) aggregableFeatures.add(feature);
        });

        aggregableFeatures.forEach(f -> {
            //Get parent concept of f
            String parentConcept = runAQuery("SELECT ?c WHERE { GRAPH ?g { " +
                    "?c <"+GlobalGraph.HAS_FEATURE.val()+"> <"+f+"> } }",T).next().get("c").toString();

            //identify the member concepts related to each parent concept (adjacentParents)
            Set<String> adjacentParents = Sets.newHashSet();
            runAQuery("SELECT ?n WHERE { GRAPH ?g { ?n <"+GlobalGraph.PART_OF.val()+">+ <"+parentConcept+"> } } ", T)
                .forEachRemaining(adjacentParent -> adjacentParents.add(adjacentParent.get("n").toString()));

            Set<String> aggregableNeighbours = Sets.newHashSet();
            runAQuery("SELECT ?n WHERE { GRAPH ?g { " +
                    "{ <"+parentConcept+"> ?p ?n . " +
                    "?n <"+GlobalGraph.PART_OF.val()+">+ ?top } UNION "+
                    "{ <"+parentConcept+"> ?p ?n . " +
                    "?bottom <"+ GlobalGraph.PART_OF.val()+">+ ?n } } }", T)
                .forEachRemaining(aggregableNeighbour -> aggregableNeighbours.add(aggregableNeighbour.get("n").toString()));

            //identify regular neighbours in the query
            Set<String> regularNeighbours = Sets.newHashSet();
            runAQuery("SELECT ?n WHERE { GRAPH ?g {" +
                    "<"+parentConcept+"> ?p ?n } } ",PHI_o)
                .forEachRemaining(regularNeighbour -> {
                    String URI_regularNeighbour = regularNeighbour.get("n").toString();
                    if (!aggregableFeatures.contains(URI_regularNeighbour)
                            && !aggregableNeighbours.contains(URI_regularNeighbour)
                            && !adjacentParents.contains(URI_regularNeighbour)
                            && /*tweak to avoid including rdf:type*/!URI_regularNeighbour.contains(GlobalGraph.CONCEPT.val())) {
                        regularNeighbours.add(URI_regularNeighbour);
                        //expand with subclasses from the query pattern
                        runAQuery("SELECT ?sc WHERE { " +
                            "?sc <"+Namespaces.rdfs.val()+"subClassOf>+ <"+URI_regularNeighbour+"> }",PHI_o)
                            .forEachRemaining(sc -> regularNeighbours.add(sc.get("sc").toString()));

                    }
                });

            adjacentParents.forEach(par -> {
                //Generate MatchQuery Qm
                BasicPattern matchQuery = new BasicPattern();
                Set<Tuple3<String,String,String>> patternTriples = Sets.newHashSet(); //used to avoid some duplicate triples that appear

                patternTriples.add(new Tuple3<>(par,GlobalGraph.HAS_FEATURE.val(),f));
                regularNeighbours.forEach(regularNeighbour -> {
                    runAQuery("SELECT ?p WHERE { GRAPH ?g { <"+par+"> ?p <"+regularNeighbour+"> } }",T).forEachRemaining(prop -> {
                        patternTriples.add(new Tuple3<>(par,prop.get("p").toString(),regularNeighbour));
                    });
                });
                aggregableNeighbours.forEach(aggregableNeighbour -> {
                    runAQuery("SELECT ?p ?y WHERE { GRAPH ?g { <"+par+"> ?p ?y . ?y <"+GlobalGraph.PART_OF.val()+">+ <"+aggregableNeighbour+"> } }",T)
                        .forEachRemaining(t -> {
                            patternTriples.add(new Tuple3<>(par,t.get("p").toString(),t.get("y").toString()));
                            //go up one at a time until in the top level (e.g., Hour)
                            //we rely on automatic expansion of IDs later
                            String currentLevel = t.get("y").toString();
                            while (!currentLevel.equals(aggregableNeighbour)) {
                                String top = runAQuery("SELECT DISTINCT ?t WHERE { GRAPH ?g { <"+currentLevel+"> <"+GlobalGraph.PART_OF.val()+"> ?t } }",T)
                                    .next().get("t").toString();
                                patternTriples.add(new Tuple3<>(currentLevel,GlobalGraph.PART_OF.val(),top));
                                currentLevel = top;
                            }
                        });
                });
                patternTriples.forEach(triple -> addTriple(matchQuery,triple._1,triple._2,triple._3));

                //Call rewriteConjunctiveQuery with the matchQuery generated
                Tuple3<Set<String>,BasicPattern,InfModel> CQqueryStructure = new Tuple3<>(PI,matchQuery,ontologyFromPattern(matchQuery));
                rewriteToUnionOfConjunctiveQueries(CQqueryStructure,T).forEach(System.out::println);
                System.out.println("###################################################");

            });


        });

        return null;
        /*
        Set<Walk> out = rewrite();
        return out;*/
    }

    @SuppressWarnings("Duplicates")
    public static Set<ConjunctiveQuery> rewriteToUnionOfConjunctiveQueries(Tuple3<Set<String>, BasicPattern, InfModel> queryStructure, Dataset T) {
        Set<String> PI = queryStructure._1;
        BasicPattern PHI_p = queryStructure._2;
        InfModel PHI_o = queryStructure._3;

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
                conceptsGraph.addEdge(t.getSubject().getURI(), t.getObject().getURI(),/*, t.getPredicate().getURI()*/UUID.randomUUID().toString());
            }
        });
        // This is required when only one concept is queried, where all edges are hasFeature
        if (conceptsGraph.vertexSet().isEmpty()) {
            conceptsGraph.addVertex(PHI_p.getList().get(0).getSubject().getURI());
        }

        // 2 Expand Q_G with IDs
        conceptsGraph.vertexSet().forEach(c -> {
            //Give me the feature ID for concept c
            ResultSet IDs = runAQuery("SELECT ?t " +
                    "WHERE { GRAPH ?g {" +
                    "<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?t . " +
                    "?t <"+ Namespaces.rdfs.val()+"subClassOf> <"+ Namespaces.sc.val()+"identifier> " +
                    "} }", T);
            IDs.forEachRemaining(id -> {
                if (!PHI_p.getList().contains(Triple.create(NodeFactory.createURI(c),
                        NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()),id.get("t").asNode()))) {
                    PHI_p.add(Triple.create(NodeFactory.createURI(c),
                            NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()), id.get("t").asNode()));
                    addTriple(PHI_o, c, GlobalGraph.HAS_FEATURE.val(), id.get("t").asNode().getURI());
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
            ResultSet resultSetFeatures = runAQuery("SELECT ?f " +
                    "WHERE {<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?f }",PHI_o);
        // 4 Unfold LAV mappings
            Set<String> features = Sets.newHashSet();
            resultSetFeatures.forEachRemaining(f -> features.add(f.get("f").asResource().getURI()));

            //Case when the Concept has no data, we need to identify the wrapper using the concept instead of the feature
            if (features.isEmpty()) {
                runAQuery("SELECT ?g WHERE { GRAPH ?g { <"+c+"> <"+Namespaces.rdf.val()+"type"+"> <"+ GlobalGraph.CONCEPT.val() +"> } }", T)
                    .forEachRemaining(wrapper -> {
                        String w = wrapper.get("g").toString();
                        if (!w.equals(Namespaces.T.val()) && w.contains("Wrapper")/*last min bugfix*/) {
                            if (!CQsPerWrapper.containsKey(new Wrapper(w))) {
                                CQsPerWrapper.put(new Wrapper(w), Sets.newHashSet());
                            }
                            ConjunctiveQuery CQ = new ConjunctiveQuery();
                            CQ.setProjections(Sets.newHashSet());
                            CQ.setWrappers(Sets.newHashSet(new Wrapper(w)));

                            Set<ConjunctiveQuery> currentSet = CQsPerWrapper.get(new Wrapper(w));
                            currentSet.add(CQ);
                            CQsPerWrapper.put(new Wrapper(w), currentSet);
                        }
                    });
            } else {
                features.forEach(f -> {
                    ResultSet wrappers = runAQuery("SELECT ?g " +
                            "WHERE { GRAPH ?g { <" + c + "> <" + GlobalGraph.HAS_FEATURE.val() + "> <" + f + "> } }", T);
                    // 5 Find attributes in S
                    wrappers.forEachRemaining(wRes -> {
                        String w = wRes.get("g").asResource().getURI();
                        // Distinguish the ontology named graph
                        if (!w.equals(Namespaces.T.val()) && w.contains("Wrapper")/*last min bugfix*/) {
                            ResultSet rsAttr = runAQuery("SELECT ?a " +
                                    "WHERE { GRAPH ?g { ?a <" + Namespaces.owl.val() + "sameAs> <" + f + "> . " +
                                    "<" + w + "> <" + SourceGraph.HAS_ATTRIBUTE.val() + "> ?a } }", T);
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
            }
        // 6 Prune output
            //partialCQsGraph.addVertex(new KeyedTuple2<>(c,Sets.newHashSet()));

            CQsPerWrapper.forEach((wrapper,CQs) -> {
                ConjunctiveQuery mergedCQ = new ConjunctiveQuery();
                mergedCQ.setWrappers(Sets.newHashSet(wrapper));
                mergedCQ.setProjections(Sets.newHashSet());
                CQs.forEach(CQ -> mergedCQ.getProjections().addAll(CQ.getProjections()));

                Set<String> featuresInCQ = Sets.newHashSet();
                CQs.forEach(w -> {
                    w.getProjections().forEach(projection -> {
                        runAQuery("SELECT ?f WHERE { GRAPH ?g " +
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
                    if (!found) {
                        //partialCQsGraph.removeVertex(new KeyedTuple2<>(c,null));
                        partialCQsGraph.addVertex(new KeyedTuple2<>(c,Sets.newHashSet(mergedCQ)));
                    }
                }
            });
        });

        //Add edges to the graph of partialCQs
        conceptsGraph.edgeSet().forEach(edge -> {
            KeyedTuple2<String,Set<ConjunctiveQuery>> source = partialCQsGraph.vertexSet().stream().filter(v -> v.equals(conceptsGraph.getEdgeSource(edge))).findFirst().get();
            KeyedTuple2<String,Set<ConjunctiveQuery>> target = partialCQsGraph.vertexSet().stream().filter(v -> v.equals(conceptsGraph.getEdgeTarget(edge))).findFirst().get();
            partialCQsGraph.addEdge(source,target,/*UUID.randomUUID().toString()*/conceptsGraph.getEdge(source._1,target._1));
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
                    //The partial CQs do not share any wrapper, must discover how to join them, this will add new equijoins

                    //First, let's check if the two sets of wrappers might generate non-minimal queries. If so, we can dismiss them.
                    if (!minimal(Sets.union(CP.get(0).getWrappers(),CP.get(1).getWrappers()),PHI_p,T)) continue;

                    //This conceptSource and conceptTarget are obtained from the original graph, we can only join
                    //the two partialCQs using the IDs of these two concepts -- see the queries next
                    String conceptSource = conceptsGraph.getEdgeSource(edge);
                    String conceptTarget = conceptsGraph.getEdgeTarget(edge);

                    //Find ID features for the current set of wrappers in both ends
                    Map<String,Tuple2<Set<Wrapper>,Set<Wrapper>>> IDs_and_their_wrappers = Maps.newHashMap();
                    for (Wrapper w : CP.get(0).getWrappers()) {
                        //Union should be OK, otherwise we would have not have entered the if that says that both collections of wrappers are disjoint
                        ResultSet rs = runAQuery("SELECT ?f WHERE { GRAPH <"+w.getWrapper()+"> {" +
                                "{ " +
                                    "?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> . " +
                                    "<"+conceptSource+"> <"+GlobalGraph.HAS_FEATURE.val()+"> ?f } " +
                                "UNION { " +
                                    "?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> . " +
                                    "<"+conceptTarget+"> <"+GlobalGraph.HAS_FEATURE.val()+"> ?f } " +
                                "} }",T);
                        while (rs.hasNext()) {
                            String ID = rs.next().get("f").toString();
                            if (!IDs_and_their_wrappers.containsKey(ID)) IDs_and_their_wrappers.put(ID,new Tuple2<>(Sets.newHashSet(),Sets.newHashSet()));
                            Set<Wrapper> wrappersForID = IDs_and_their_wrappers.get(ID)._1;
                            wrappersForID.add(w);
                            IDs_and_their_wrappers.put(ID,new Tuple2<>(wrappersForID,IDs_and_their_wrappers.get(ID)._2));
                        }
                    }
                    for (Wrapper w : CP.get(1).getWrappers()) {
                        ResultSet rs = runAQuery("SELECT ?f WHERE { GRAPH <"+w.getWrapper()+"> {" +
                                "{ " +
                                    "?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> . " +
                                    "<"+conceptSource+"> <"+GlobalGraph.HAS_FEATURE.val()+"> ?f } " +
                                "UNION { " +
                                    "?f <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> . " +
                                    "<"+conceptTarget+"> <"+GlobalGraph.HAS_FEATURE.val()+"> ?f } " +
                                "} }",T);
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
                        //Different ways of doing the join
                        Sets.cartesianProduct(entry.getValue()._1, entry.getValue()._2).forEach(wrapper_combination -> {

                            Wrapper wrapperA = wrapper_combination.get(0);
                            Wrapper wrapperB = wrapper_combination.get(1);

                            String attA = runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                    "?a <"+Namespaces.owl.val()+"sameAs> <"+feature+"> . " +
                                    "<"+wrapperA.getWrapper()+"> <"+SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                    .nextSolution().get("a").asResource().getURI();
                            String attB = runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
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
                    String edgeConnectingSourceToNeighbor = partialCQsGraph.getEdge(new KeyedTuple2<>(source._1,null),new KeyedTuple2<>(neighbor._1,null));
                    partialCQsGraph.removeEdge(edgeConnectingSourceToNeighbor);
                    partialCQsGraph.addEdge(joinedVertex,neighbor,edgeConnectingSourceToNeighbor);
                }
            });
            Graphs.neighborListOf(partialCQsGraph,target).forEach(neighbor -> {
                if (!target.equals(neighbor)) {
                    String edgeConnectingTargetToNeighbor = partialCQsGraph.getEdge(new KeyedTuple2<>(target._1,null),new KeyedTuple2<>(neighbor._1,null));
                    partialCQsGraph.removeEdge(edgeConnectingTargetToNeighbor);
                    partialCQsGraph.addEdge(joinedVertex,neighbor,edgeConnectingTargetToNeighbor);
                }
            });
            partialCQsGraph.removeVertex(source);
            partialCQsGraph.removeVertex(target);
        }
        return partialCQsGraph.vertexSet().stream().findFirst().get()._2;
    }


}
