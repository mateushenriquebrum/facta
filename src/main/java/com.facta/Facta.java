package com.facta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Facta<B> {
    public static class Seq<B> {
        public Live<B> With(B board) {
            Root.Context<B> context = new Root.Context<>(board, new HashMap<>(), new HashSet<>());
            Node<B> sequence = new Node.Sequence<>(List.of());
            return new Live<B>(sequence, context);
        }
    }

    public static <B> Seq<B> Sequence() {
        return new Seq<>();
    }
}
