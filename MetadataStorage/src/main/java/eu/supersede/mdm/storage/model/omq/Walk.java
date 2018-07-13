package eu.supersede.mdm.storage.model.omq;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.omq.relational_operators.EquiJoin;
import eu.supersede.mdm.storage.model.omq.relational_operators.RelationalOperator;

import java.util.List;
import java.util.Objects;

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
        if (o instanceof Walk) {
            final Walk other = (Walk)o;
            return Objects.equals(operators,other.operators) ;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(operators);
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
