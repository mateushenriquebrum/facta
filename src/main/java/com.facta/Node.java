package com.facta;


import java.util.function.Supplier;

import static com.facta.Node.Status.*;

public sealed interface Node permits Node.Action, Node.Belief, Node.Fallback, Node.Inverse, Node.Sequence {
    enum Status {
        SUCCESS, FAILURE, RUNNING
    }

    enum Verification {
        SUCCESS, FAILURE
    }

    Status tick(Context context);

    record Sequence(Node... children) implements Node {
        @Override
        public Status tick(Context context) {
            for (Node node : children) {
                Status status = node.tick(context);
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

    record Fallback(Node... children) implements Node {
        @Override
        public Status tick(Context context) {
            for (Node node : children) {
                Status status = node.tick(context);
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

    record Belief(Supplier<Verification> condition) implements Node {
        @Override
        public Status tick(Context context) {
            try {
                return condition.get() == Verification.SUCCESS ? Status.SUCCESS : Status.FAILURE;
            }
            catch (Exception e) {
                // Good messages here about intentions
                return FAILURE;
            }
        }
    }

    record Inverse(Belief belief) implements Node {
        @Override
        public Status tick(Context context) {
            return belief.tick(context) == SUCCESS ? FAILURE : SUCCESS;
        }
    }

    record Action(Supplier<Status> perform) implements Node {
        @Override
        public Status tick(Context context) {
            try {
                return perform.get();
            } catch (Exception ex) {
                // Good messages here about intentions
                return FAILURE;
            }
        }
    }
}
