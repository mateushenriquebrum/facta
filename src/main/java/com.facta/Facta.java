package com.facta;

import java.util.function.Function;

import static java.util.List.of;

public class Facta<B> {

    private Facta() {}
    private static int index = 0;

    @SafeVarargs
    public static <B> Node.Sequence<B> Sequence(Node<B> ... children) {
        return new Node.Sequence<>(of(children));
    }


    @SafeVarargs
    public static <B> Node.Fallback<B> Fallback(Node<B> ... children) {
        return new Node.Fallback<>(of(children));
    }

    public static <B> Node.Belief<B> Belief(Function<B, Boolean> condition) {
        return new Node.Belief<>(condition);
    }

    public static <B> Node.Action<B> Action(Function<B, Node.Status> perform) {
        return new Node.Action<>(index++, perform);
    }

    public static <B> Node.Inverse<B> Inverse(Node.Belief<B> belief) {
        return new Node.Inverse<>(belief);
    }

}
