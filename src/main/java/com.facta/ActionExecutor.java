package com.facta;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private final T board;
    private final ExecutorService executor;
    private final BlockingQueue<Consumer<T>> queue = new LinkedBlockingQueue<>(1);

    public ActionExecutor(T board, ExecutorService executor) {
        this.board = board;
        this.executor = executor;
    }

    public void start(){
        executor.submit(this::watchLoop);
    }

    public void stop(){
        Thread.currentThread().interrupt();
    }

    public void next(Consumer<T> execute) {
        if(execute != null){
            queue.offer(execute);
        }
    }

    private void watchLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Consumer<T> local = queue.take();
                try{
                    local.accept(board);
                } catch (Exception e) {
                    e.printStackTrace();
                    //
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
