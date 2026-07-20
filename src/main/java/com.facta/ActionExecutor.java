package com.facta;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private final T board;
    private final SynchronousQueue<Consumer<T>> exchange  = new SynchronousQueue<>();
    private final Thread watcher;
    private final AtomicReference<String> status = new AtomicReference<>(null);

    public ActionExecutor(T board) {
        this.board = board;
        this.watcher = Thread
                .ofVirtual()
                .name("queue_watcher")
                .unstarted(this::processQueue);
    }

    public void stop(){
        this.watcher.interrupt();
    }

    public void next(Consumer<T> execute) throws InterruptedException {
        if(execute == null) return;
        if(!this.watcher.isAlive()) this.watcher.start();
        exchange.put(execute);
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Consumer<T> local = exchange.take();
                try{
                    status.set("RUNNING");
                    local.accept(board);
                    status.set("SUCCESS");
                } catch (Exception e) {
                    status.set("FAIL");
                }
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public String status() {
        return  status.getAndSet(null);
    }
}
