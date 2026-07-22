package com.facta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static com.facta.Facta.*;
import static java.lang.Boolean.TRUE;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactaTest {
    
    static class Board {
        boolean home = false;
        int x, y = 0;
    }

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        index = 0; //TODO: solve static member
    }
    
    @Test
    public void shouldExpressEmptySequence() {
        assertEquals(
                new Node.Sequence<Board>(of()),
                Sequence());
    }

    @Test
    public void shouldExpressEmptyFallback() {
        assertEquals(
                new Node.Fallback<Board>(of()),
                Fallback());
    }

    @Test
    public void shouldExpressBelief() {
        Function<Board, Boolean> lambda = (board) -> TRUE;
        assertEquals(
                new Node.Fallback<>(of(new Node.Inverse<>(new Node.Belief<>(lambda)))),
                Fallback(Inverse(Belief(lambda))));
    }

    @Test
    public void shouldExpressActionWithProperIds() {
        Function<Board, Node.Status> lambda = (board) -> Node.Status.SUCCESS;
        assertEquals(
                new Node.Fallback<>(of(
                        new Node.Action<>(0, lambda),
                        new Node.Action<>(1, lambda)
                )),
                Fallback(
                        Action(lambda),
                        Action(lambda)));
    }

    @Test
    public void shouldBoardContextToTree() {
        Function<Board, Node.Status> lambda = (board) -> Node.Status.SUCCESS;
        assertEquals(
                new Live<>(new Node.Fallback<>(
                        of(
                                new Node.Action<>(0, lambda),
                                new Node.Action<>(1, lambda),
                                new Node.Action<>(2, lambda))),
                        new Clock.World<>(board, new HashMap<>(), new HashSet<>())),
                Of(
                        Fallback(
                                Action(lambda),
                                Action(lambda),
                                Action(lambda)))
                        .With(board));
    }
}
