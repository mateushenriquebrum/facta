package com.facta;

public sealed interface Node permits Node.Action, Node.Sequence, Node.Fallback, Node.Belief {
    Integer id();
    record Sequence(Integer id, Node ... children) implements Node { }
    record Fallback(Integer id, Node ... children) implements Node { }
    record Action(Integer id) implements Node { }
    record Belief(Integer id) implements Node { }
}

