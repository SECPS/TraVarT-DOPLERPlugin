package at.jku.cps.travart.dopler.transformation.util;

public class Pair<K> {

    private final K first;
    private final K second;

    public Pair(K first, K second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public K getSecond() {
        return second;
    }
}
