package com.facta;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ActionExecutorTest {

    static class AnyBoard {
        int count = 0;
    }

    @Test
    public void testNextIsConsumedCorrectly() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        CountDownLatch count = new CountDownLatch(1);
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> {
            b.count++;
            count.countDown();
        });
        action.start();
        count.await();
        action.stop();
        assertEquals(1, board.count);
    }

    @Test
    public void testExecuteSynchronous() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.start();
        for(int a = 0; a < 10; a++){
            action.next((b) -> {
                b.count++;
            });
            Thread.sleep(100);
        }
        action.stop();
        assertEquals(10, board.count);
    }

    @Test
    public void testDiscardAction() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.start();
        for(int a = 0; a < 10; a++){
            action.next((b) -> b.count++);
        }
        Thread.sleep(100);
        action.stop();
        assertEquals(1, board.count);
    }
    @Test
    public void testRunningStatus() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        action.start();
        Thread.sleep(500);
        action.stop();
        assertEquals("RUNNING", action.status());
    }

    @Test
    public void testSuccessStatus() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        action.start();
        Thread.sleep(500);
        action.stop();
        assertEquals("SUCCESS", action.status());
    }

    @Test
    public void testSuccessFail() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> {
            throw  new RuntimeException("SOMETHING WHEN WRONG");
        });
        action.start();
        Thread.sleep(500);
        action.stop();
        assertEquals("FAIL", action.status());
    }

    @Test
    public void testCleanStatus() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        assertNull(action.status());
        action.next((b) -> {
            throw  new RuntimeException("SOMETHING WHEN WRONG");
        });
        action.start();
        Thread.sleep(500);
        action.stop();
        assertEquals("FAIL", action.status());
        assertNull(action.status());
    }
}
