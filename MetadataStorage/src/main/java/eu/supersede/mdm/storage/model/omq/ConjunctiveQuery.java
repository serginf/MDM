package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;

import java.util.Objects;
import java.util.Set;

public class ConjunctiveQuery {

    private Set<String> projections;
    private Set<EquiJoin> joinConditions;
    private Set<Wrapper> wrappers;

    public ConjunctiveQuery() {
        this.projections = Sets.newHashSet();
        this.joinConditions = Sets.newHashSet();
        this.wrappers = Sets.newHashSet();
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
        return "ConjunctiveQuery{" +
                "projections=" + projections +
                ", joinConditions=" + joinConditions +
                ", wrappers=" + wrappers +
                '}';
    }
}
