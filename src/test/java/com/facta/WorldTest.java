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
                new StateOf(1, BELIEF)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(1, SUCCESS),
                new StateOf(2, BELIEF)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(1, SUCCESS),
                new StateOf(2, FAILURE)
        )), world.run(1));

        assertEquals(new Ticked(List.of(
                new StateOf(1, SUCCESS),
                new StateOf(2, FAILURE)
        )), world.run(1));
    }

    @Test
    public void shouldBeliefFailSafely() {
        DummyBoard board = new DummyBoard();
        Map<Integer, Function<DummyBoard, Boolean>> believes = new HashMap<>();
        believes.put(1, (b) -> {
            throw new RuntimeException();
        });
        Map<Integer, Function<DummyBoard, Boolean>> actions = new HashMap<>();
        Node root = new Sequence(0,
                new Belief(1));

        World<?> world = new World<>(board, believes, actions, root);

        assertEquals(new Ticked(List.of(
                new StateOf(1, FAILURE)
        )), world.run(2));
    }

    static class DummyBoard {}

}
