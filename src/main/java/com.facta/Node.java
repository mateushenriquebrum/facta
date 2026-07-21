package com.facta;

import java.util.List;
import java.util.function.Supplier;


public sealed interface Node permits Node.Action, Node.Belief, Node.Fallback, Node.Inverse, Node.Sequence {
    enum Status {SUCCESS, FAILURE, RUNNING}

    enum Verification {SUCCESS, FAILURE}

    record Sequence(List<Node> children) implements Node {}

    record Fallback(List<Node> children) implements Node {}

    record Belief(Supplier<Verification> condition) implements Node {}

    record Inverse(Belief belief) implements Node {}

    record Action(Integer id, Supplier<Status> perform) implements Node {}
}
