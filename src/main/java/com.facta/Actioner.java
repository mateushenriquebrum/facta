package com.facta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Actioner<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Actioner.class);
    private final T board;
    private final SynchronousQueue<Consumer<T>> exchange  = new SynchronousQueue<>();
    private final AtomicReference<String> status = new AtomicReference<>(null);
    private final AtomicReference<Thread> watcher = new AtomicReference<>(null);
    private final ReentrantLock lock = new ReentrantLock();

    public Actioner(T board) {
        this.board = board;
    }

    public void stop(){
        Thread current = this.watcher.get();
        if (current != null && current.isAlive()) {
            LOG.debug("Stopping active queue_watcher thread");
            current.interrupt();
        }
    }

    public void next(Consumer<T> action) throws InterruptedException {
        LOG.info("Execute received {}", action);
        if(action == null) {
            LOG.warn("Execute must not be null, returning from next");
            return;
        }
        lock.lock();
        try{
            startWorkerIfNeeded();
        } finally {
            lock.unlock();
        }
        exchange.put(action);
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
                    status.set("FAILURE");
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

    private void startWorkerIfNeeded() {
        Thread current = watcher.get();
        if(current == null || !current.isAlive()) {
            Thread newly = Thread
                    .ofVirtual()
                    .uncaughtExceptionHandler((t, e) -> {
                        LOG.error("Something went wrong with queue_watcher, it will start again (self-healing)", e);
                        status.set("FAILURE");
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
