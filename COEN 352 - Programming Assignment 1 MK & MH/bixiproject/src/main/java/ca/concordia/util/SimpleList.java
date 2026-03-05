package ca.concordia.util;

public class SimpleList<T> implements Iterable<T> {

    private Object[] data;
    private int      size;

    public SimpleList() {
        this.data = new Object[16];
        this.size = 0;
    }

    public SimpleList(int initialCapacity) {
        this.data = new Object[initialCapacity];
        this.size = 0;
    }

    /** adds an element to the end **/
    public void add(T item) {
        if (size == data.length) grow();
        data[size++] = item;
    }

    /** get element at index location **/
    public T get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        return (T) data[index];
    }

    /** set element at index location **/
    public void set(int index, T item) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        data[index] = item;
    }

    /** give array size **/
    public int size() { return size; }

    /** checks if array is empty **/
    public boolean isEmpty() { return size == 0; }

    /** wipes the array **/
    public void clear() { size = 0; }

    /** double array capacity **/
    private void grow() {
        Object[] bigger = new Object[data.length * 2];
        for (int i = 0; i < data.length; i++) bigger[i] = data[i];
        data = bigger;
    }

    /** for-each support **/
    @Override
    public java.util.Iterator<T> iterator() {
        return new java.util.Iterator<T>() {
            int cursor = 0;
            public boolean hasNext() { return cursor < size; }
            public T next() { return (T) data[cursor++]; }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}