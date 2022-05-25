package nl.esciencecenter.ptk.data;

public class Pair<T1, T2> {

    public T1 one;

    public T2 two;

    public Pair() {
        one = null;
        two = null;
    }

    public Pair(T1 value1, T2 value2) {
        one = value1;
        two = value2;
    }

    public T1 one() {
        return one;
    }

    public T2 two() {
        return two;
    }

    public T1 left() {
        return one;
    }

    public T2 right() {
        return two;
    }

    public T1 setOne(T1 value) {
        T1 prev = one;
        one = value;
        return prev;
    }

    public T2 setTwo(T2 value) {
        T2 prev = two;
        two = value;
        return prev;
    }

}
