package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.omq.relational_operators.RelationalOperator;

import java.util.List;

public class Walk {

    public Walk() {
        this.operators = Lists.newArrayList();
    }

    private List<RelationalOperator> operators;

    public List<RelationalOperator> getOperators() {
        return operators;
    }

    public void setOperators(List<RelationalOperator> operators) {
        this.operators = operators;
    }

    public Walk(Walk w) {
        this.operators = Lists.newArrayList(w.getOperators());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Walk walk = (Walk) o;

        return getOperators().equals(walk.getOperators());
    }

    @Override
    public int hashCode() {
        return getOperators().hashCode();
    }

    @Override
    public String toString() {
        String out = "";
        for (RelationalOperator op : operators) {
            out += op.toString();
        }
        return out;
    }
}
