package eu.supersede.mdm.storage.model.omq.relational_operators;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

public class EquiJoin extends RelationalOperator {

    private String left_attribute;
    private String right_attribute;

    public EquiJoin() {}

    public EquiJoin (String left, String right) {
        this.left_attribute = left;
        this.right_attribute = right;
    }

    public String getLeft_attribute() {
        return left_attribute;
    }

    public void setLeft_attribute(String left_attribute) {
        this.left_attribute = left_attribute;
    }

    public String getRight_attribute() {
        return right_attribute;
    }

    public void setRight_attribute(String right_attribute) {
        this.right_attribute = right_attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EquiJoin) {
            final EquiJoin other = (EquiJoin)o;
            return Objects.equals(left_attribute,other.left_attribute) &&
                    Objects.equals(right_attribute, other.right_attribute);
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(left_attribute,right_attribute);
    }

    @Override
    public String toString() {
        return left_attribute + " = " + right_attribute+" ";
    }
}
