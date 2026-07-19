package com.facta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActionExecutorTest {

    static class AnyBoard {
        int count = 0;
    }

    @Test
    public void testNextIsConsumedCorrectly() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        action.next((b) -> b.count++);
        action.start();
        Thread.sleep(500);
        action.stop();
        Assertions.assertEquals(1, board.count);
    }

    @Test
    public void testDiscardSpareAction() throws InterruptedException {
        AnyBoard board = new AnyBoard();
        ActionExecutor<AnyBoard> action = new ActionExecutor<>(board);
        for(int a = 0; a < 10; a++){
            action.next((b) -> b.count++);
        }
        action.start();
        Thread.sleep(500);
        action.stop();
        Assertions.assertEquals(1, board.count);
    }
}
