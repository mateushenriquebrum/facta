package com.facta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ActionExecutor<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionExecutor.class);
    private final T board;
    private final SynchronousQueue<Consumer<T>> exchange  = new SynchronousQueue<>();
    private final Thread watcher;
    private final AtomicReference<String> status = new AtomicReference<>(null);

    public ActionExecutor(T board) {
        this.board = board;
        this.watcher = Thread
                .ofVirtual()
                .uncaughtExceptionHandler((t, e) -> {
                    LOG.error("Something went wrong with queue_watcher, it will start again", e);
                    status.set("FAIL");
                })
                .name("queue_watcher")
                .unstarted(this::processQueue);
        LOG.info("queue_watcher registered");
    }

    public void stop(){
        LOG.debug("interrupting queue_watcher, shouldn't happen in production environment");
        this.watcher.interrupt();
    }

    public void next(Consumer<T> execute) throws InterruptedException {
        LOG.info("execute received {}", execute);
        if(execute == null) {
            LOG.warn("execute must not be null, returning from next");
            return;
        }
        if(!this.watcher.isAlive()) {
            LOG.info("queue_watcher is not started, starting it and blocking until finishing");
            this.watcher.start();
        }
        exchange.put(execute);
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Consumer<T> local = exchange.take();
                try{
                    LOG.info("got a function to execute {}, setting status to RUNNING", local);
                    status.set("RUNNING");
                    local.accept(board);
                    LOG.info("finished the function {}, setting status to SUCCESS", local);
                    status.set("SUCCESS");
                } catch (Exception e) {
                    LOG.debug("failed due exception", e);
                    LOG.info("function failed {}, setting status to FAIL", local);
                    status.set("FAIL");
                }
            }catch (InterruptedException e) {
                LOG.debug("interrupted due exception, if it is production it should not be caused by stop", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public String status() {
        LOG.info("getting status from queue_watcher");
        return  status.getAndSet(null);
    }
}
