package com.facta;


import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.facta.Node.Status.*;

public sealed interface Node permits Node.Action, Node.Belief, Node.Fallback, Node.Inverse, Node.Memoizer, Node.Sequence {
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

    record Memoizer(Node child, AtomicReference<Status> cache) implements Node {

        public Memoizer(Node child) {
            this(child, new AtomicReference<>(null));
        }

        @Override
        public Status tick(Context context) {
            Status cached = cache.get();
            if (cached != null) {
                return cached;
            }
            Status result = child.tick(context);
            if(result != RUNNING) {
                cache.set(result);
            }
            return result;
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
