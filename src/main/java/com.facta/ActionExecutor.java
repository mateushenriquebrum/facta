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
    private final AtomicReference<String> status = new AtomicReference<>(null);
    private final AtomicReference<Thread> watcher = new AtomicReference<>(null);

    public ActionExecutor(T board) {
        this.board = board;
    }

    public void stop(){
        Thread current = this.watcher.get();
        if (current != null && current.isAlive()) {
            LOG.debug("Stopping active queue_watcher thread");
            current.interrupt();
        }
    }

    public void next(Consumer<T> execute) throws InterruptedException {
        LOG.info("Execute received {}", execute);
        if(execute == null) {
            LOG.warn("Execute must not be null, returning from next");
            return;
        }
        startWorkerIfNeeded();
        exchange.put(execute);
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Consumer<T> local = exchange.take();
                try{
                    LOG.info("Got a function to execute {}, setting status to RUNNING", local);
                    status.set("RUNNING");
                    local.accept(board);
                    LOG.info("Finished the function {}, setting status to SUCCESS", local);
                    status.set("SUCCESS");
                } catch (Exception e) {
                    LOG.debug("Failed due exception", e);
                    LOG.info("Function failed {}, setting status to FAIL", local);
                    status.set("FAIL");
                }
            }catch (InterruptedException e) {
                LOG.debug("Interrupted due exception, if it is production it should not be caused by stop", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public String status() {
        LOG.info("Getting status from queue_watcher");
        return  status.getAndSet(null);
    }

    private synchronized void startWorkerIfNeeded() {
        Thread current = watcher.get();
        if(current == null || !current.isAlive()) {
            Thread newly = Thread
                    .ofVirtual()
                    .uncaughtExceptionHandler((t, e) -> {
                        LOG.error("Something went wrong with queue_watcher, it will start again (self-healing)", e);
                        status.set("FAIL");
                        this.watcher.set(null);
                    })
                    .name("queue_watcher")
                    .unstarted(this::processQueue);
            watcher.set(newly);
            newly.start();
            LOG.info("New queue_watcher started successfully");
        }
    }
}
