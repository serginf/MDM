package eu.supersede.mdm.storage.model.graph;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;

import java.util.Set;

public class CQVertex {

    private String label;
    private Set<ConjunctiveQuery> CQs;

    public CQVertex(String label)
    {
        this.label = label;
        this.CQs = Sets.newHashSet();
    }

    public CQVertex(String label, Set<ConjunctiveQuery> CQs)
    {
        this.label = label;
        this.CQs = CQs;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<ConjunctiveQuery> getCQs() {
        return CQs;
    }

    public void setCQs(Set<ConjunctiveQuery> CQs) {
        this.CQs = CQs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass().equals(String.class)) {
            return Objects.equal((String)o,label);
        }
        if (o == null || getClass() != o.getClass()) return false;
        CQVertex cqVertex = (CQVertex) o;
        return Objects.equal(label, cqVertex.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(label);
    }

    @Override
    public String toString()
    {
        return label/* + " - " + CQs*/;
    }

}
