package com.facta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static java.util.List.of;

public class Facta<B> {

    private Facta() {}
    public static int index = 0;

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

    public static <B> Of<B> Of(Node<B> node) {
        return new Of<>(node);
    }

    public static <B> Node.Inverse<B> Inverse(Node.Belief<B> belief) {
        return new Node.Inverse<>(belief);
    }

    public static class Of<B> {
        private final Node<B> node;
        public Of(Node<B> node) {
            this.node = node;
        }

        public Live<B> With(B board) {
            Root.Context<B> context = new Root.Context<>(board, new HashMap<>(), new HashSet<>());
            return new Live<>(node, context);
        }
    }

}
