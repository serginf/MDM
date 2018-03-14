package eu.supersede.mdm.storage.model.omq.relational_operators;

import eu.supersede.mdm.storage.util.RDFUtil;

public class Wrapper extends RelationalOperator {

    private String wrapper;

    public Wrapper(String w) {
        this.wrapper = w;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wrapper wrapper1 = (Wrapper) o;

        return getWrapper().equals(wrapper1.getWrapper());
    }

    @Override
    public int hashCode() {
        return getWrapper().hashCode();
    }

    @Override
    public String toString() {
        return "("+RDFUtil.nn(wrapper)+")";
    }

    public String preview() throws Exception {
        throw new Exception("Can't preview a generic wrapper, need to call an implementation subclass");
    };
}
