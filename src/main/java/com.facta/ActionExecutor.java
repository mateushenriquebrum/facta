package com.facta;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private final T board;
    private final ExecutorService executor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile Consumer<T> next = null;

    public ActionExecutor(T board, ExecutorService executor) {
        this.board = board;
        this.executor = executor;
    }

    public void start(){
        if (isRunning.compareAndSet(false, true)){
            executor.submit(this::watchLoop);
        }
    }

    public void stop(){
        isRunning.compareAndSet(true, false);
    }

    public void next(Consumer<T> execute) {
        this.next = execute;
    }

    private void tryExecuteNext() {
        Consumer<T> local = next;
        if(local != null) {
            this.next = null;
            local.accept(board);
        }
    }

    private void watchLoop() {
        try {
            while (isRunning.get()) {
                tryExecuteNext();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
        }
    }
}
