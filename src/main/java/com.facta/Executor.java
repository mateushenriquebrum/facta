package com.facta;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Executor<B> {

    private final Queue<Consumer<B>> queue = new LinkedList<>();
    private final B board;
    private final CountDownLatch latch;

    public  Executor(B board, CountDownLatch latch) {
        this.board = board;
        this.latch = latch;
        watchingForQueueChanges();
    }

    public void enqueue(Consumer<B> execute) {
        queue.add(execute);
    }

    private void tryExecuteNext() {
        Consumer<B> consume = queue.poll();
        if(consume != null) {
            consume.accept(board);
        }
    }

    private void watchingForQueueChanges() {
        try {
            ReentrantLock lock = new ReentrantLock();
            Thread
                    .ofVirtual()
                    .name("watcher")
                    .uncaughtExceptionHandler((t, e) -> {
                        System.out.println(e.getMessage());
                    })
                    .unstarted(() -> {
                        while (true) {
                            try {
                                lock.lock();
                                /// copy board here, and rollback when failure
                                tryExecuteNext();
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } finally {
                                latch.countDown();
                                lock.unlock();
                            }
                        }
                    }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
