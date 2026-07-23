package com.facta;

import com.facta.Node.Belief;
import com.facta.Node.Sequence;
import com.facta.Tree.Ticked;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.facta.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldTest {
    @Test
    public void shouldSetStateForBelief() {
        DummyBoard board = new DummyBoard();
        Map<Integer, Function<DummyBoard, Boolean>> believes = new HashMap<>();
        believes.put(1, (b) -> true);
        believes.put(2, (b) -> false);
        Map<Integer, Function<DummyBoard, Boolean>> actions = new HashMap<>();
        Node root = new Sequence(0,
                new Belief(1),
                new Belief(2),
                new Belief(3));

        World<?> world = new World<>(board, believes, actions, root);

        assertEquals(new Ticked(List.of(
                new StateOf(BELIEF, 1)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(SUCCESS, 1),
                new StateOf(BELIEF, 2)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(SUCCESS, 1),
                new StateOf(FAILURE, 2)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(SUCCESS, 1),
                new StateOf(FAILURE, 2)
        )), world.run(1));
    }

    static class DummyBoard {}

}
