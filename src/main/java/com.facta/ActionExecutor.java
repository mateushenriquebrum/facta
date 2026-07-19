package com.facta;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private final T board;
    private final BlockingQueue<Consumer<T>> queue = new ArrayBlockingQueue<>(1);
    private final Thread watcher;

    public ActionExecutor(T board) {
        this.board = board;
        this.watcher = Thread
                .ofVirtual()
                .name("queue_watcher")
                .unstarted(this::processQueue);
    }

    public void start(){
        this.watcher.start();
    }

    public void stop(){
        this.watcher.interrupt();
    }

    public void next(Consumer<T> execute) {
        if(execute != null){
            queue.offer(execute);
        }
    }

    private void processQueue() {
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
