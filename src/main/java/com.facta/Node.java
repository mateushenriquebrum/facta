package com.facta;


import java.util.function.Supplier;

import static com.facta.Node.Status.*;

public sealed interface Node permits Node.Belief, Node.Fallback, Node.Inverse, Node.Sequence {
    enum Status {
        SUCCESS, FAILURE, RUNNING;
    }

    enum Verification {
        SUCCESS, FAILURE;
    }

    Status tick();

    record Sequence(Node ... children) implements Node {
        @Override
        public Status tick() {
            for (Node node : children) {
                Status status = node.tick();
                switch (status) {
                    case FAILURE, RUNNING -> {
                        return status;
                    }
                    case SUCCESS -> {
                    }
                }
            }
            return SUCCESS;
        }
    }

    record Fallback(Node ... children) implements Node {
        @Override
        public Status tick() {
            for (Node node : children) {
                Status status = node.tick();
                switch (status) {
                    case SUCCESS, RUNNING -> {
                        return status;
                    }
                    case FAILURE -> {
                    }
                }
            }
            return FAILURE;
        }
    }

    record Belief(Supplier<Verification> action) implements Node {
        @Override
        public Status tick() {
            return action.get() == Verification.SUCCESS ? Status.SUCCESS : Status.FAILURE;
        }
    }

    record Inverse(Belief belief) implements Node {

        @Override
        public Status tick() {
            return belief.tick() == SUCCESS ? Status.FAILURE : Status.SUCCESS;
        }
    }
}
