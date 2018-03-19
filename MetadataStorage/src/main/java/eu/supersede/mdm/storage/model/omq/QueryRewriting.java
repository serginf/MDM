package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.metamodel.SourceGraph;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.Projection;
import eu.supersede.mdm.storage.model.omq.relational_operators.RelationalOperator;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.BasicPattern;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryRewriting {

    private Dataset T;

    private Set<String> PI;

    // We keep two representations of Q_G.\varphi to simplify its manipulation
    //  1) As a pattern to easily access its triples as a list
    //  2) As an ontology so it can be queried via SPARQL
    private BasicPattern PHI_p; //PHI_pattern
    private OntModel PHI_o; // PHI_ontology

    public QueryRewriting(String SPARQL, Dataset d) {
        T = d;

        // Compile the SPARQL using ARQ and generate its <pi,phi> representation
        Query q = QueryFactory.create(SPARQL);
        Op ARQ = Algebra.compile(q);

        PI = Sets.newHashSet();
        ((OpTable)((OpJoin)((OpProject)ARQ).getSubOp()).getLeft()).getTable().rows().forEachRemaining(r -> {
            r.vars().forEachRemaining(v -> PI.add(r.get(v).getURI()));
        });

        this.PHI_p = ((OpBGP)((OpJoin)((OpProject)ARQ).getSubOp()).getRight()).getPattern();
        PHI_o = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            RDFUtil.addTriple(PHI_o, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );
    }

    public Set<Walk> rewrite() {

        /**
         * Query expansion
         */
        /** 1 Identify query-related concepts **/
        List<String> concepts = Lists.newArrayList();
        // First, create a graph of the pattern in order to obtain its topological sort
        DirectedAcyclicGraph<String,String> conceptsGraph = new DirectedAcyclicGraph<String, String>(String.class);
        PHI_p.getList().forEach(t -> {
            // Add only concepts so its easier to populate later the list of concepts
            if (!t.getPredicate().getURI().equals(GlobalGraph.HAS_FEATURE.val())) {
                try {
                    conceptsGraph.addVertex(t.getSubject().getURI());
                    conceptsGraph.addVertex(t.getObject().getURI());
                    conceptsGraph.addDagEdge(t.getSubject().getURI(), t.getObject().getURI(), t.getPredicate().getURI());
                } catch (DirectedAcyclicGraph.CycleFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        // Now, iterate using a topological sort adding the concepts to the list of concepts
        conceptsGraph.iterator().forEachRemaining(vertex -> concepts.add(vertex));

        /** 2 Expand Q_G with IDs **/
        concepts.forEach(c -> {
            ResultSet IDs = RDFUtil.runAQuery("SELECT ?t " +
                    "WHERE { GRAPH ?g {" +
                    "<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?t . " +
                    "?t <"+ Namespaces.rdfs.val()+"subClassOf> <"+ Namespaces.sc.val()+"identifier> " +
                    "} }", T);
            IDs.forEachRemaining(id -> {

                if (!PHI_p.getList().contains(Triple.create(NodeFactory.createURI(c),
                        NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()),id.get("t").asNode()))) {

                    PHI_p.add(Triple.create(NodeFactory.createURI(c),
                            NodeFactory.createURI(GlobalGraph.HAS_FEATURE.val()), id.get("t").asNode()));
                    RDFUtil.addTriple(PHI_o, c, GlobalGraph.HAS_FEATURE.val(), id.get("t").asResource().getURI());
                }
            });
        });

        /**
         * Intra-concept generation
         */
        List<Tuple2<String,Set<Walk>>> partialWalks = Lists.newArrayList();
        /** 3 Identify queried features **/
        concepts.forEach(c -> {
            Map<Wrapper,Set<Walk>> PartialWalksPerWrapper = Maps.newHashMap();
            ResultSet resultSetFeatures = RDFUtil.runAQuery("SELECT ?f " +
                    "WHERE {<"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?f }",PHI_o);
        /** 4 Unfold LAV mappings **/
            //Convert the resultset to set
            Set<String> features = Sets.newHashSet();
            resultSetFeatures.forEachRemaining(f -> features.add(f.get("f").asResource().getURI()));

            features.forEach(f -> {
                ResultSet wrappers = RDFUtil.runAQuery("SELECT ?g " +
                        "WHERE { GRAPH ?g { <"+c+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> <"+f+"> } }",T);
        /** 5 Find attributes in S **/
                wrappers.forEachRemaining(wRes -> {
                    String w = wRes.get("g").asResource().getURI();
                    // Distinguish the ontology named graph
                    if (!w.equals(Namespaces.T.val())) {
                        ResultSet rsAttr = RDFUtil.runAQuery("SELECT ?a " +
                                "WHERE { GRAPH ?g { ?a <"+Namespaces.owl.val()+"sameAs> <"+f+"> . " +
                                "<"+w+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }", T);
                        String attribute = rsAttr.nextSolution().get("a").asResource().getURI();

                        if (!PartialWalksPerWrapper.containsKey(new Wrapper(w))) {
                            PartialWalksPerWrapper.put(new Wrapper(w), Sets.newHashSet());
                        }
                        Walk walk = new Walk();
                        walk.getOperators().add(new Projection(attribute));
                        walk.getOperators().add(new Wrapper(w));

                        Set<Walk> currentSet = PartialWalksPerWrapper.get(new Wrapper(w));
                        currentSet.add(walk);
                        PartialWalksPerWrapper.put(new Wrapper(w), currentSet);

                    }
                });
            });
        /** 6 Prune output **/
            PartialWalksPerWrapper.forEach((wrapper,walk) -> {
                Walk mergedWalk = new Walk();
                mergedWalk.getOperators().add(0, new Projection());
                mergedWalk.getOperators().add(1, wrapper);
                walk.forEach(w -> {
                    w.getOperators().forEach(op -> {
                        if (op instanceof Projection) {
                            ((Projection)op).getProjectedAttributes().forEach(att -> {
                                ((Projection)mergedWalk.getOperators().get(0)).getProjectedAttributes().add(att);
                            });
                        }
                    });
                });

                Set<String> featuresInWalk = Sets.newHashSet();
                walk.forEach(w -> {
                    w.getOperators().forEach(op -> {
                        if (op instanceof Projection) {
                            ((Projection)op).getProjectedAttributes().forEach(a -> {
                                RDFUtil.runAQuery("SELECT ?f " +
                                        "WHERE { GRAPH ?g " +
                                        "{<"+a+"> <"+Namespaces.owl.val()+"sameAs> ?f } }",T).forEachRemaining(featureInWalk -> {
                                    featuresInWalk.add(featureInWalk.get("f").asResource().getURI());
                                });
                            });
                        }
                    });
                });
                if (featuresInWalk.equals(features)) {
                    boolean found = false;
                    for (Tuple2<String,Set<Walk>> pw : partialWalks) {
                        if (pw._1.equals(c)) {
                            found = true;
                            pw._2().add(mergedWalk);
                        }
                    }
                    if (!found) partialWalks.add(new Tuple2<>(c,Sets.newHashSet(mergedWalk)));
                }
            });

        });

        System.out.println("Output of phase #2");
        partialWalks.forEach(pw -> System.out.println(pw));

        /**
         * Inter-concept generation
         */
        Tuple2<String,Set<Walk>> current = partialWalks.get(0);
        for (int i = 1; i < partialWalks.size(); ++i) {
            if (i==2) {
                System.out.println("a");
            }
            Tuple2<String,Set<Walk>> next = partialWalks.get(i);
            Set<Walk> joined = Sets.newHashSet();

            System.out.println("iteration "+i);
            System.out.println("current = "+current);
            System.out.println("next = "+next);

        /** 7 Compute cartesian product **/
            for (List<Walk> CP : Sets.cartesianProduct(current._2,next._2)) {
                Walk CP_left = CP.get(0);
                Walk CP_right = CP.get(1);

                Set<Wrapper> wrappersLeft = Sets.newHashSet(
                    CP_left.getOperators().stream().filter(op -> op instanceof Wrapper).map(w -> (Wrapper)w).collect(Collectors.toList())
                );
                Set<Wrapper> wrappersRight = Sets.newHashSet(
                    CP_right.getOperators().stream().filter(op -> op instanceof Wrapper).map(w -> (Wrapper)w).collect(Collectors.toList())
                );

                Walk mergedWalk = new Walk(CP_left);

        /** 8 Merge walks **/
                if (!Sets.intersection(wrappersLeft,wrappersRight).isEmpty()) {
                    for (int j = 1; j < CP_right.getOperators().size(); ++j) {
                        RelationalOperator op_cpright = CP_right.getOperators().get(j);
                        if (op_cpright instanceof Wrapper) {
                            for (int k = 1; k < mergedWalk.getOperators().size(); ++k) {
                                RelationalOperator op_mw = mergedWalk.getOperators().get(k);
                                if (op_mw instanceof Wrapper && op_cpright.equals(op_mw) ) {
                                    ((Projection)mergedWalk.getOperators().get(k-1)).getProjectedAttributes().addAll(
                                            ((Projection)CP_right.getOperators().get(j-1)).getProjectedAttributes()
                                    );
                                }

                            }
                        }
                    }
                } else {
                    CP_right.getOperators().forEach(op -> mergedWalk.getOperators().add(op));
                }

        /** 9 Discover join wrappers **/

                if (Sets.intersection(wrappersLeft,wrappersRight).isEmpty()) {
                    Set<Wrapper> wrappersFromLtoR = Sets.newHashSet();
                    RDFUtil.runAQuery("SELECT ?g " +
                        "WHERE { GRAPH ?g { <"+current._1+"> ?x <"+next._1+">}}", T).
                            forEachRemaining(w -> {
                                if (!w.get("g").asResource().getURI().equals(Namespaces.T.val())) {
                                    wrappersFromLtoR.add(new Wrapper(w.get("g").asResource().getURI()));
                                }
                            });
                    Set<Wrapper> wrappersFromRtoL = Sets.newHashSet();
                    RDFUtil.runAQuery("SELECT ?g " +
                            "WHERE { GRAPH ?g { <"+next._1+"> ?x <"+current._1+">}}", T).
                            forEachRemaining(w -> {
                                if (!w.get("g").asResource().getURI().equals(Namespaces.T.val()))
                                    wrappersFromRtoL.add(new Wrapper(w.get("g").asResource().getURI()));
                            });

        /** 10 Discover join attribute **/
                    if (!wrappersFromLtoR.isEmpty()) {
                        String f_ID = RDFUtil.runAQuery("SELECT ?t WHERE { " +
                                "GRAPH ?g { <"+next._1+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?t . " +
                                "?t <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> } }",T)
                                .nextSolution().get("t").asResource().getURI();

                        // find wrapper with ID
                        Wrapper wrapperWithIDright = (Wrapper)CP_right.getOperators().get(1);

                        String att_right = RDFUtil.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                "?a <"+Namespaces.owl.val()+"sameAs> <"+f_ID+"> . " +
                                "<"+wrapperWithIDright.getWrapper()+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                .nextSolution().get("a").asResource().getURI();
                        wrappersFromLtoR.forEach(w -> {
                            String att_left = RDFUtil.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                    "?a <"+Namespaces.owl.val()+"sameAs> <"+f_ID+"> . " +
                                    "<"+w.getWrapper()+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                    .nextSolution().get("a").asResource().getURI();

                            EquiJoin join = new EquiJoin(att_left,att_right);

                            boolean found = false;
                            for (int j = 0; j < mergedWalk.getOperators().size() && !found; ++j) {
                                for (int k = j+1; k < mergedWalk.getOperators().size() && !found; ++k) {
                                    if (mergedWalk.getOperators().get(j) instanceof Wrapper &&
                                        mergedWalk.getOperators().get(j).equals(w) &&
                                        mergedWalk.getOperators().get(k) instanceof Wrapper &&
                                        mergedWalk.getOperators().get(k).equals(wrapperWithIDright)) {
                                        found = true;
                                        mergedWalk.getOperators().add(k-1,join);
                                    }
                                    else if (mergedWalk.getOperators().get(j) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(j).equals(wrapperWithIDright) &&
                                            mergedWalk.getOperators().get(k) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(k).equals(w)) {
                                        found = true;
                                        mergedWalk.getOperators().add(k-1,join);
                                    }
                                }
                            }
                        });
                    }
                    else if (!wrappersFromRtoL.isEmpty()) {
                        String f_ID = RDFUtil.runAQuery("SELECT ?t WHERE { " +
                                "GRAPH ?g { <"+current._1+"> <"+ GlobalGraph.HAS_FEATURE.val()+"> ?t . " +
                                "?t <"+Namespaces.rdfs.val()+"subClassOf> <"+Namespaces.sc.val()+"identifier> } }",T)
                                .nextSolution().get("t").asResource().getURI();

                        // find wrapper with ID
                        Wrapper wrapperWithIDleft = (Wrapper)CP_left.getOperators().get(1);

                        String att_left = RDFUtil.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                "?a <"+Namespaces.owl.val()+"sameAs> <"+f_ID+"> . " +
                                "<"+wrapperWithIDleft.getWrapper()+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                .nextSolution().get("a").asResource().getURI();

                        wrappersFromRtoL.forEach(w -> {
                            String att_right = RDFUtil.runAQuery("SELECT ?a WHERE { GRAPH ?g {" +
                                    "?a <"+Namespaces.owl.val()+"sameAs> <"+f_ID+"> . " +
                                    "<"+w.getWrapper()+"> <"+ SourceGraph.HAS_ATTRIBUTE.val()+"> ?a } }",T)
                                    .nextSolution().get("a").asResource().getURI();

                            EquiJoin join = new EquiJoin(att_left,att_right);

                            boolean found = false;
                            for (int j = 0; j < mergedWalk.getOperators().size() && !found; ++j) {
                                for (int k = j+1; k < mergedWalk.getOperators().size() && !found; ++k) {
                                    if (mergedWalk.getOperators().get(j) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(j).equals(w) &&
                                            mergedWalk.getOperators().get(k) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(k).equals(wrapperWithIDleft)) {
                                        found = true;
                                        mergedWalk.getOperators().add(k-1,join);
                                    }
                                    else if (mergedWalk.getOperators().get(j) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(j).equals(wrapperWithIDleft) &&
                                            mergedWalk.getOperators().get(k) instanceof Wrapper &&
                                            mergedWalk.getOperators().get(k).equals(w)) {
                                        found = true;
                                        mergedWalk.getOperators().add(k-1,join);
                                    }
                                }
                            }


                        });


                        System.out.println("a");
                    }
                }
                joined.add(mergedWalk);
            }
            current = new Tuple2<>(next._1, joined);
        }

        return current._2;
    }

}
