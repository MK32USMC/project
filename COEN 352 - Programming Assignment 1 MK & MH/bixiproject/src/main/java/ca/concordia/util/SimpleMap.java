package ca.concordia.util;

public class SimpleMap {

    private static final int DEFAULT_CAPACITY = 256;
    private static final double LOAD_FACTOR   = 0.75;

    /** allows for chaining the operation **/
    private static class Node {
        String key;
        int    value;
        Node   next;

        Node(String key, int value) {
            this.key   = key;
            this.value = value;
        }
    }

    private Node[] buckets;
    private int    size;
    private int    capacity;

    public SimpleMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets  = new Node[capacity];
        this.size     = 0;
    }

    /** hash **/
    private int hash(String key) {
        if (key == null) return 0;
        int h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = 31 * h + key.charAt(i);
        }
        return Math.abs(h) % capacity;
    }

    /** sets up key-value pair **/
    public void put(String key, int value) {
        int idx  = hash(key);
        Node cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) { cur.value = value; return; }
            cur = cur.next;
        }
        Node node = new Node(key, value);
        node.next    = buckets[idx];
        buckets[idx] = node;
        size++;
        if (size > capacity * LOAD_FACTOR) resize();
    }

    /** gets value for key, or returns -1 if not found. **/
    public int get(String key) {
        int idx  = hash(key);
        Node cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return -1;
    }

    /** returns true if the key exists. */
    public boolean containsKey(String key) {
        return get(key) != -1;
    }

    /** increments the count for a key by 1 or sets to 1 */
    public void increment(String key) {
        int idx  = hash(key);
        Node cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) { cur.value++; return; }
            cur = cur.next;
        }
        // Not found — insert with value 1
        Node node = new Node(key, 1);
        node.next    = buckets[idx];
        buckets[idx] = node;
        size++;
        if (size > capacity * LOAD_FACTOR) resize();
    }

    /** returns array size **/
    public int size() { return size; }

    /** returns keys for Simplelist, helps with chaining **/
    public SimpleList<String> keys() {
        SimpleList<String> result = new SimpleList<>(size);
        for (int i = 0; i < capacity; i++) {
            Node cur = buckets[i];
            while (cur != null) {
                result.add(cur.key);
                cur = cur.next;
            }
        }
        return result;
    }

    /** double array size and rehash all entries **/
    private void resize() {
        int      oldCapacity = capacity;
        Node[]   oldBuckets  = buckets;
        capacity = capacity * 2;
        buckets  = new Node[capacity];
        size     = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Node cur = oldBuckets[i];
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
    }
}