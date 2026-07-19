package com.facta;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private final T board;
    private final BlockingQueue<Consumer<T>> queue = new ArrayBlockingQueue<>(1);
    private final Thread watcher;
    private String status;

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
            status = "RUNNING";
            queue.offer(execute);
        }
    }

    private void processQueue() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Consumer<T> local = queue.take();
                try{
                    local.accept(board);
                    status = "SUCCESS";
                } catch (Exception e) {
                    status = "FAIL";
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String status() {
        String temp = status;
        status = null;
        return temp;
    }
}
