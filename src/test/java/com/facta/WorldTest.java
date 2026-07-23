package com.facta;

import com.facta.Node.Belief;
import com.facta.Node.Sequence;
import com.facta.Tree.Ticked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.facta.Status.FAILURE;

public class WorldTest {
    @Test
    public void shouldSetStateForBelief() {
        DummyBoard board = new DummyBoard();
        Map<Integer, Function<DummyBoard, Boolean>> believes = new HashMap<>();
        believes.put(1, (b) -> true);
        believes.put(2, (b) -> false);
        Map<Integer, Function<DummyBoard, Boolean>> actions = new HashMap<>();
        Node root = new Sequence(0, new Belief(1), new Belief(2));

        Ticked ticked = new World<>(board, believes, actions, root).run(5);

        Assertions.assertEquals(FAILURE, ticked.last().state());
    }

    class DummyBoard {}

}
