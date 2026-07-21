package com.facta;

import java.util.List;
import java.util.function.Function;


public sealed interface Node<B> permits Node.Action, Node.Belief, Node.Fallback, Node.Inverse, Node.Sequence {
    enum Status {SUCCESS, FAILURE, RUNNING}

    enum Verification {SUCCESS, FAILURE}

    record Sequence<B>(List<Node<B>> children) implements Node<B> {}

    record Fallback<B>(List<Node<B>> children) implements Node<B> {}

    record Belief<B>(Function<B, Verification> condition) implements Node<B> {}

    record Inverse<B>(Belief<B> belief) implements Node<B> {}

    record Action<B>(Integer id, Function<B, Status> perform) implements Node<B> {}
}
