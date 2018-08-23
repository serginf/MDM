package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.omq.relational_operators.AggregatedAttribute;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConjunctiveAggregateQuery {

    private ConjunctiveQuery conjunctiveQuery;
    private Set<String> groupBy;
    private Set<AggregatedAttribute> aggregatedAttributes;

    public ConjunctiveAggregateQuery() {
        this.conjunctiveQuery = null;
        this.groupBy = Sets.newHashSet();
        this.aggregatedAttributes = Sets.newHashSet();
    }

    public ConjunctiveAggregateQuery(ConjunctiveQuery CQ) {
        this.conjunctiveQuery = CQ;
        this.groupBy = Sets.newHashSet();
        this.aggregatedAttributes = Sets.newHashSet();
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

    public Set<AggregatedAttribute> getAggregatedAttributes() {
        return aggregatedAttributes;
    }

    public void setAggregatedAttributes(Set<AggregatedAttribute> aggregatedAttributes) {
        this.aggregatedAttributes = aggregatedAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConjunctiveAggregateQuery) {
            final ConjunctiveAggregateQuery other = (ConjunctiveAggregateQuery) o;
            return Objects.equals(conjunctiveQuery, other.conjunctiveQuery) &&
                    Objects.equals(groupBy, other.groupBy) &&
                    Objects.equals(aggregatedAttributes, other.aggregatedAttributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conjunctiveQuery,groupBy,aggregatedAttributes);
    }

    @Override
    public String toString() {

        Map<String,String> namespaces = Maps.newHashMap();
        EnumSet.allOf(Namespaces.class).forEach(e -> namespaces.put(e.val(),e.name()));

        return "ConjunctiveAggregateQuery{" +
                "aggregatedAttributes=" + aggregatedAttributes +
                ", groupBy=[" + groupBy.stream().map(s-> {
                    String uri = namespaces.keySet().stream().filter(n -> s.contains(n)).findFirst().get();
                    return s.replace(uri,namespaces.get(uri)+":");
                }).collect(Collectors.joining(", ")) +
                "], conjunctiveQuery=" + conjunctiveQuery +
                '}';
    }
}
