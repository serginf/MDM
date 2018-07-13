package eu.supersede.mdm.storage.util;

public class Tuple2<X, Y> {
    public final X _1;
    public final Y _2;
    public Tuple2(X _1, Y _2) {
        this._1 = _1;
        this._2 = _2;
    }
    public X _1() {
        return _1;
    }
    public Y _2() {
        return _2;
    }
    @Override
    public String toString() {
        return "Tuple2{" +
                "_1=" + _1 +
                ", _2=" + _2 +
                '}';
    }
}