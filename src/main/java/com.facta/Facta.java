package com.facta;

public class Facta<T> {
    T board;

    public Facta(T board) {
        this.board = board;
    }

    public Root sequence(Node ... nodes) {
        if (nodes.length == 0) throw new IllegalArgumentException("Nodes cannot be empty");
        return new Root();
    }

    public Node belief() {
        return new Node.Belief((board) -> null);
    }
}
