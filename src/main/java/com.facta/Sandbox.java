package com.facta;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Sandbox<B> implements AutoCloseable {

    private final CompletableFuture<Boolean> future;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public Sandbox(B board, Function<B, Boolean> action) {
        this.future = CompletableFuture.supplyAsync(() -> action.apply(board), executor);
    }

    public Node.Status status() {
        return switch (future.state()) {
            case RUNNING -> Node.Status.RUNNING;
            case SUCCESS -> Boolean.TRUE.equals(future.getNow(false))
                    ? Node.Status.SUCCESS
                    : Node.Status.FAILURE;
            case FAILED, CANCELLED -> Node.Status.FAILURE;
        };
    }

    @Override
    public void close() {
        executor.close();
    }
}
