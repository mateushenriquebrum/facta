package com.facta;

import java.util.Objects;

/**
 * Deal with all immutability and side effect,
 * including change contex, remove unreached leaves, manage action threads
 * @param <B> the shared stateful object used for the node to communicate between them,
 *           It's recommended to use a simple Java structure like:
 *           class Board {
 *              int bar;
 *              boolean fuzz;
 *              String foo;
 *           }
 */
public class Live<B> {
    final Node<B> node;
    final Root.Context<B> context;

    public Live(Node<B> node, Root.Context<B> context) {
        this.node = node;
        this.context = context;
    }

    public Node.Status tick() {
        Node.Status status = Root.tick(node, context);
        context.ticked();
        return status;
    }

    @Override
    public String toString() {
        return "Live{" +
                "node=" + node +
                ", context=" + context +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Live<?> live = (Live<?>) o;
        return Objects.equals(node, live.node) && Objects.equals(context, live.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, context);
    }
}
