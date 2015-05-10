package nl.esciencecenter.ptk.data;

import java.util.List;

public class ListHolder<T> implements VARListHolder<T> {

    public List<T> values;

    @Override
    public boolean isSet() {
        return (values != null);
    }

    @Override
    public void set(List<T> newValues) {
        values = newValues;
    }

    @Override
    public void set(int index, T newValue) {
        if (values == null) {
            throw new NullPointerException("List has not been initialized (list is null)!");
        }
        values.set(index, newValue);
    }

    @Override
    public List<T> get() {
        return values;
    }

    @Override
    public T get(int index) {
        if (values == null)
            return null;
        return values.get(index);
    }

    @Override
    public void add(T newValue) {
        if (values == null) {
            throw new NullPointerException("List has not been initialized (list is null)!");
        }
        values.add(newValue);
    }

    @Override
    public boolean isEmpty() {
        if (values == null)
            return true;
        return (values.size() < 0);
    }

}
