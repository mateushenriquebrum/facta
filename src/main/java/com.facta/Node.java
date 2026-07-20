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

    Status tick();

    record Sequence(Node... children) implements Node {
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

    record Fallback(Node... children) implements Node {
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

    record Belief(Supplier<Verification> condition) implements Node {
        @Override
        public Status tick() {
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
        public Status tick() {
            return belief.tick() == SUCCESS ? FAILURE : SUCCESS;
        }
    }

    record Memoizer(Node child, AtomicReference<Status> cache) implements Node {

        public Memoizer(Node child) {
            this(child, new AtomicReference<>(null));
        }

        @Override
        public Status tick() {
            Status cached = cache.get();
            if (cached != null) {
                return cached;
            }
            Status result = child.tick();
            if(result != RUNNING) {
                cache.set(result);
            }
            return result;
        }
    }

    record Action(Supplier<Status> perform) implements Node {
        @Override
        public Status tick() {
            try {
                return perform.get();
            } catch (Exception ex) {
                // Good messages here about intentions
                return FAILURE;
            }
        }
    }
}
