package eu.supersede.mdm.storage.model.omq.relational_operators;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EquiJoin equiJoin = (EquiJoin) o;

        if (!getLeft_attribute().equals(equiJoin.getLeft_attribute())) return false;
        return getRight_attribute().equals(equiJoin.getRight_attribute());
    }

    @Override
    public int hashCode() {
        int result = getLeft_attribute().hashCode();
        result = 31 * result + getRight_attribute().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return left_attribute + " ⋈ " + right_attribute;
    }
}
