package com.facta;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.lang.Boolean.TRUE;

public class LightSandBox<B> implements AutoCloseable, Sandbox<B> {

    private CompletableFuture<Boolean> future;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final B board;

    public LightSandBox(B board) {
        this.board = board;
    }

    @Override
    public Status status() {
        return switch (future.state()) {
            case RUNNING -> Status.RUNNING;
            case SUCCESS -> TRUE.equals(future.getNow(false))
                    ? Status.SUCCESS
                    : Status.FAILURE;
            case FAILED, CANCELLED -> Status.FAILURE;
        };
    }

    @Override
    public void spin(Function<B, Boolean> action) {
        this.future = CompletableFuture.supplyAsync(() -> action.apply(this.board), executor);
    }

    @Override
    public void close() {
        executor.close();
    }
}
