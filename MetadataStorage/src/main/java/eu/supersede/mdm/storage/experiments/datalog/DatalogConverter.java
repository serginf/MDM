package eu.supersede.mdm.storage.experiments.datalog;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.experiments.datalog.DatalogQuery;
import eu.supersede.mdm.storage.model.graph.CQVertex;
import eu.supersede.mdm.storage.model.graph.IntegrationGraph;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatalogConverter {

    /**
     * To generate the datalog version of a global query we need to reference all features.
     * This is a constraint posed by the MiniCon algorithm, which requires that subgoals for the same
     * relation have the same size.
     */
    public static DatalogQuery convert(String headName, IntegrationGraph I, IntegrationGraph clique) {
        DatalogQuery Q = new DatalogQuery();

        //Find IDs of concepts
        HashMap<String,String> IDsPerConcept = Maps.newHashMap();
        clique.edgeSet().stream().filter(e -> !e.getLabel().equals("subClassOf")).forEach(e -> {
            CQVertex source = I.getEdgeSource(e);
            CQVertex target = I.getEdgeTarget(e);
            if (e.getLabel().equals("hasFeature") && target.getLabel().contains("Feature_id")) {
                IDsPerConcept.putIfAbsent(source.getLabel(),target.getLabel());
            }
        });

        HashMap<String, Set<String>> featuresPerConcept = Maps.newHashMap();
        I.edgeSet().stream().filter(e -> !e.getLabel().equals("subClassOf")).forEach(e -> {
            CQVertex source = I.getEdgeSource(e);
            CQVertex target = I.getEdgeTarget(e);

            featuresPerConcept.putIfAbsent(source.getLabel(), Sets.newHashSet());
            if (e.getLabel().equals("hasFeature")) {
                featuresPerConcept.compute(source.getLabel(),(k,v) -> {
                    v.add(target.getLabel()); return v;
                });
            } else {
                //Add the ID of the target as a "foreign key"
                featuresPerConcept.compute(source.getLabel(),(k,v) -> {
                    v.add(IDsPerConcept.get(target.getLabel())); return v;
                });
            }
        });
        //Add missing FK's (for concepts not in the query)
        clique.edgeSet().stream()
                .filter(e -> !e.getLabel().equals("hasFeature") && !e.getLabel().equals("subClassOf"))
                .filter(e -> IDsPerConcept.keySet().contains(clique.getEdgeSource(e).getLabel()))
                .forEach(e -> {
            CQVertex source = clique.getEdgeSource(e);
            CQVertex target = clique.getEdgeTarget(e);
            if (featuresPerConcept.keySet().contains(source.getLabel())) {
                featuresPerConcept.compute(source.getLabel(),(k,v) -> {
                    v.add(IDsPerConcept.get(target.getLabel())); return v;
                });
            }
        });

        //Generate head and body of the query
        HashMap<String, Set<String>> head = Maps.newHashMap();
        Set<String> headAtts = featuresPerConcept.values().stream().flatMap(atts->atts.stream()).collect(Collectors.toSet());
        head.put(headName,headAtts);
        Q.setHead(head);

        HashMap<String, Set<String>> body = Maps.newHashMap();
        featuresPerConcept.keySet().forEach(c -> {
            body.putIfAbsent(c,Sets.newHashSet());
            featuresPerConcept.get(c).forEach(f -> {
                body.compute(c,(k,v) -> {
                   v.add(f);return v;
                });
            });
        });
        Q.setBody(body);

        return Q;
//        return toDatalog(headName,featuresPerConcept);
    }

    // Given a set of datalog queries of the form q(x,y,z) :- r1(x,y), r2(y,z), ...
    // minimizes those attributes (FK) that do not have their corresponding subgoal
    // q(x,y,z):-r1(r1_id,x,y,z,r2_id,r3_id), r2(r2_id,t,r3_id)
    //      --> q(x,y,z):-r1(r1_id,x,y,z,r2_id), r2(r2_id,t)
    public static Set<DatalogQuery> minimizeDatalog(Set<DatalogQuery> datalogQueries) {
        Set<String> concepts = datalogQueries.stream().flatMap(dlQ->dlQ.getBody().values().stream().flatMap(atts->atts.stream()))
                .filter(a->a.contains("_id")).map(a->a.replace("_Feature_id","")).collect(Collectors.toSet());

        Set<String> conceptsInQuery = concepts.stream().filter(c -> {
            return datalogQueries.stream().flatMap(dq->dq.getBody().keySet().stream()).collect(Collectors.toSet()).contains(c);
        }).collect(Collectors.toSet());

        Set<DatalogQuery> minimizedDatalogQueries = Sets.newHashSet();
        datalogQueries.forEach(dlQ -> {
            DatalogQuery newQ = new DatalogQuery();

            dlQ.getHead().keySet().forEach(h -> {
                dlQ.getHead().get(h).forEach(a -> {
                    if (!a.contains("id") || conceptsInQuery.contains(a.replace("_Feature_id",""))) {
                        newQ.getHead().putIfAbsent(h,Sets.newHashSet());
                        newQ.getHead().compute(h,(k,v)->{
                            v.add(a);return v;
                        });
                    }
                });
            });
            dlQ.getBody().keySet().forEach(h -> {
                dlQ.getBody().get(h).forEach(a -> {
                    if (!a.contains("id") || conceptsInQuery.contains(a.replace("_Feature_id",""))) {
                        newQ.getBody().putIfAbsent(h,Sets.newHashSet());
                        newQ.getBody().compute(h,(k,v)->{
                            v.add(a);return v;
                        });
                    }
                });
            });
            minimizedDatalogQueries.add(newQ);

        });

        return minimizedDatalogQueries;
    }

}
