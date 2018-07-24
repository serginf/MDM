package eu.supersede.mdm.storage.util;

import java.util.Objects;

public class KeyedTuple2<X, Y> extends Tuple2<X, Y> {

    public KeyedTuple2(X _1, Y _2) {
        super(_1,_2);
    }
    @Override

    public int hashCode() {
        return Objects.hash(_1);
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof KeyedTuple2) {
            final KeyedTuple2 other = (KeyedTuple2)o;
            return Objects.equals(_1,other._1);
        }
        else if (o instanceof String) {
            final String other = (String)o;
            return Objects.equals(_1, other);
        }
        else {
            return false;
        }
    }

}