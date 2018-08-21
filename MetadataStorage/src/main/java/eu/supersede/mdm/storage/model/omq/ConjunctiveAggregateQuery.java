package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;

public class ConjunctiveAggregateQuery {

    private ConjunctiveQuery conjunctiveQuery;
    private Set<String> groupBy;

    public ConjunctiveAggregateQuery() {
        this.conjunctiveQuery = null;
        this.groupBy = Sets.newHashSet();
    }

    public ConjunctiveAggregateQuery(ConjunctiveQuery CQ) {
        this.conjunctiveQuery = CQ;
        this.groupBy = Sets.newHashSet();
    }

    public ConjunctiveQuery getConjunctiveQuery() {
        return conjunctiveQuery;
    }

    public void setConjunctiveQuery(ConjunctiveQuery conjunctiveQuery) {
        this.conjunctiveQuery = conjunctiveQuery;
    }

    public Set<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(Set<String> groupBy) {
        this.groupBy = groupBy;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConjunctiveAggregateQuery) {
            final ConjunctiveAggregateQuery other = (ConjunctiveAggregateQuery)o;
            return Objects.equals(conjunctiveQuery,other.conjunctiveQuery) &&
                    Objects.equals(groupBy,other.groupBy);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(conjunctiveQuery,groupBy);
    }

    @Override
    public String toString() {
        return "ConjunctiveAggregateQuery{" +
                "conjunctiveQuery=" + conjunctiveQuery +
                ", groupBy=" + groupBy +
                '}';
    }
}
