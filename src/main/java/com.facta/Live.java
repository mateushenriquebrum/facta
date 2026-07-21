package com.facta;

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
}
