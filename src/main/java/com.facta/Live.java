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
    final Clock.World<B> world;

    public Live(Node<B> node, Clock.World<B> world) {
        this.node = node;
        this.world = world;
    }

    public Node.Status tick() {
        Node.Status status = Clock.tick(node, world);
        world.ticked();
        return status;
    }

    @Override
    public String toString() {
        return "Live{" +
                "node=" + node +
                ", world=" + world +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Live<?> live = (Live<?>) o;
        return Objects.equals(node, live.node) && Objects.equals(world, live.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, world);
    }
}
