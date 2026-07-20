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
        count.await();
        assertEquals(1, board.count);
    }

    @Test
    public void testExecuteSynchronous() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        CountDownLatch count = new CountDownLatch(10);
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        for(int a = 0; a < 10; a++){
            action.next((b) -> {
                b.count++;
                count.countDown();
            });
        }
        count.await();
        assertEquals(10, board.count);
    }

    @Test
    public void testRunningStatus() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> sleep(2000));
        Thread.sleep(500);
        action.stop();
        assertEquals("RUNNING", action.status());
    }

    @Test
    public void testSuccessStatus() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> sleep(100));
        sleep(500);
        assertEquals("SUCCESS", action.status());
    }

    @Test
    public void testSuccessFail() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> {
            throw new RuntimeException("SOMETHING WHEN WRONG");
        });
        sleep(500);
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
        sleep(500);
        assertEquals("FAIL", action.status());
        assertNull(action.status());
    }

    @Test
    public void testSelfHealingAfterFailingOnOutOfMemory() throws InterruptedException {
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(null);
        CountDownLatch count = new CountDownLatch(1);

        action.next((board) -> {
            throw new OutOfMemoryError();
        });
        sleep(500);
        assertEquals("FAIL", action.status());
        action.next((board) -> {});
        sleep(500);
        assertEquals("SUCCESS", action.status());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
