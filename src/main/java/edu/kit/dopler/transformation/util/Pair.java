package edu.kit.dopler.transformation.util;

public class Pair<K, L> {

    private final K first;
    private final L second;

    public Pair(K first, L second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public L getSecond() {
        return second;
    }
}
