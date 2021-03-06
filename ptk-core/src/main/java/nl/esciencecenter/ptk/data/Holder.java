package nl.esciencecenter.ptk.data;

/**
 * Generic Object Holder Class or 'VAR' Parameter.
 */
public class Holder<T> implements VARHolder<T> {

    public T value;

    @Override
    public boolean isSet() {
        return (value != null);
    }

    @Override
    public void set(T newValue) {
        value = newValue;
    }

    @Override
    public T get() {
        return value;
    }

}
