package eu.supersede.mdm.storage.model.omq.relational_operators;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.util.RDFUtil;

import java.util.List;
import java.util.Set;

public class Projection extends RelationalOperator {

    public Projection() {
        this.projectedAttributes = Sets.newHashSet();
    }

    public Projection(String s) {
        this.projectedAttributes = Sets.newHashSet(s);
    }

    public Projection(Set<String> s) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Projection that = (Projection) o;

        return getProjectedAttributes().equals(that.getProjectedAttributes());
    }

    @Override
    public int hashCode() {
        return getProjectedAttributes().hashCode();
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
