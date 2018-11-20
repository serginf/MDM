package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConjunctiveQuery {

    private Set<String> projections;
    private Set<EquiJoin> joinConditions;
    private Set<Wrapper> wrappers;

    public ConjunctiveQuery() {
        this.projections = Sets.newHashSet();
        this.joinConditions = Sets.newHashSet();
        this.wrappers = Sets.newHashSet();
    }

    public ConjunctiveQuery(Set<String> projections, Set<EquiJoin> joinConditions, Set<Wrapper> wrappers) {
        this.projections = projections;
        this.joinConditions = joinConditions;
        this.wrappers = wrappers;
    }

    public Set<String> getProjections() {
        return projections;
    }

    public void setProjections(Set<String> projections) {
        this.projections = projections;
    }

    public Set<EquiJoin> getJoinConditions() {
        return joinConditions;
    }

    public void setJoinConditions(Set<EquiJoin> joinConditions) {
        this.joinConditions = joinConditions;
    }

    public Set<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(Set<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConjunctiveQuery) {
            final ConjunctiveQuery other = (ConjunctiveQuery)o;
            return Objects.equals(projections,other.projections) &&
                    Objects.equals(joinConditions,other.joinConditions) &&
                    Objects.equals(wrappers, other.wrappers);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(projections,joinConditions,wrappers);
    }

    @Override
    public String toString() {
        List<String> sortedProjections = Lists.newArrayList(projections);
        Collections.sort(sortedProjections);

        Map<String,String> namespaces = Maps.newHashMap();
        EnumSet.allOf(Namespaces.class).forEach(e -> namespaces.put(e.val(),e.name()));

        return "ConjunctiveQuery{" +
                "projections=" + projections/*sortedProjections.stream().map(s-> {
                    String uri = namespaces.keySet().stream().filter(n -> s.contains(n)).findFirst().get();
                    return s.replace(uri,namespaces.get(uri)+":");
                }).collect(Collectors.joining(", ")) */+
                ", joinConditions=" + joinConditions +
                ", wrappers=" + wrappers +
                '}';
    }
}
