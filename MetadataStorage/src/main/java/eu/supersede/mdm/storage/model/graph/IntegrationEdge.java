package eu.supersede.mdm.storage.model.graph;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.omq.ConjunctiveQuery;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;

public class IntegrationEdge extends DefaultEdge {

    private String label;
    private Set<Wrapper> wrappers;

    public IntegrationEdge(String label)
    {
        this.label = label;
        this.wrappers = Sets.newHashSet();
    }

    public IntegrationEdge(String label, Set<Wrapper> wrappers)
    {
        this.label = label;
        this.wrappers = wrappers;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(Set<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrationEdge cqVertex = (IntegrationEdge) o;
        return Objects.equal(label, cqVertex.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(label);
    }

    @Override
    public String toString()
    {
        return label + " - " + wrappers;
    }

}
