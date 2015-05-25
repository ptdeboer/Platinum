package nl.esciencecenter.ptk.data;

public class Triple<T1, T2, T3> {

    public T1 one;

    public T2 two;

    public T3 three;

    public Triple() {
        one = null;
        two = null;
        three = null;
    }

    public Triple(T1 value1, T2 value2, T3 value3) {
        one = value1;
        two = value2;
        three = value3;
    }

    public T1 one() {
        return one;
    }

    public T2 two() {
        return two;
    }

    public T3 three() {
        return three;
    }

    public T1 left() {
        return one;
    }

    public T2 middle() {
        return two;
    }

    public T3 right() {
        return three;
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

    public T3 setTree(T3 value) {
        T3 prev = three;
        three = value;
        return prev;
    }

}
