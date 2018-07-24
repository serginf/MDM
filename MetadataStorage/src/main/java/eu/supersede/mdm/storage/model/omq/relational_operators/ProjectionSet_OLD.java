package eu.supersede.mdm.storage.model.omq.relational_operators;

import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.util.RDFUtil;

import java.util.Objects;
import java.util.Set;

public class ProjectionSet_OLD extends RelationalOperator {

    public ProjectionSet_OLD() {
        this.projectedAttributes = Sets.newHashSet();
    }

    public ProjectionSet_OLD(String s) {
        this.projectedAttributes = Sets.newHashSet(s);
    }

    public ProjectionSet_OLD(Set<String> s) {
        this.projectedAttributes = s;
    }

    private Set<String> projectedAttributes;

    public Set<String> getProjectedAttributes() {
        return projectedAttributes;
    }

    public void setProjectedAttributes(Set<String> projectedAttributes) {
        this.projectedAttributes = projectedAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ProjectionSet_OLD) {
            final ProjectionSet_OLD other = (ProjectionSet_OLD)o;
            return Objects.equals(projectedAttributes,other.projectedAttributes);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedAttributes);
    }

    @Override
    public String toString() {
        String out = "π(";
        for (String p : projectedAttributes) {
            out += RDFUtil.nn(p) + ",";
        }
        out = out.substring(0,out.length()-1);
        out += ")";
        return out;
    }
}
