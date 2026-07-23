package com.facta;

import com.facta.Node.Action;
import com.facta.Node.Belief;
import com.facta.Node.Fallback;
import com.facta.Node.Sequence;
import com.facta.Tree.Ticked;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.facta.Status.*;
import static com.facta.Tree.tick;
import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeTest {
    @Test
    public void sequenceShouldResultInSuccess() {
        Map<Integer, Status> states = of(
                1, SUCCESS,
                2, SUCCESS
        );
        Node root = new Sequence(0,
                new Action(1),
                new Belief(2)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(SUCCESS, tree.last().state());
    }

    @Test
    public void sequenceShouldResultInRunning() {
        Map<Integer, Status> states = of(
                1, SUCCESS,
                2, RUNNING
        );
        Node root = new Sequence(0,
                new Action(1),
                new Action(2),
                new Belief(3)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(RUNNING, tree.last().state());
    }

    @Test
    public void sequenceShouldResultInFailure() {
        Map<Integer, Status> states = of(
                1, SUCCESS,
                2, FAILURE
        );
        Node root = new Sequence(0,
                new Action(1),
                new Action(2),
                new Belief(3)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(FAILURE, tree.last().state());
    }
    @Test
    public void fallbackShouldResultInSuccess() {
        Map<Integer, Status> states = of(
                1, FAILURE,
                2, SUCCESS
        );
        Node root = new Fallback(0,
                new Action(1),
                new Belief(2)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(SUCCESS, tree.last().state());
    }

    @Test
    public void fallbackShouldResultInRunning() {
        Map<Integer, Status> states = of(
                1, FAILURE,
                2, RUNNING
        );
        Node root = new Fallback(0,
                new Action(1),
                new Action(2),
                new Belief(3)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(RUNNING, tree.last().state());
    }

    @Test
    public void fallbackShouldResultInFailure() {
        Map<Integer, Status> states = of(
                1, FAILURE,
                2, FAILURE
        );
        Node root = new Fallback(0,
                new Action(1),
                new Action(2)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(FAILURE, tree.last().state());
    }

    @Test
    public void unknownStateShouldResultInAction() {
        Map<Integer, Status> states = of(
                1, SUCCESS
        );
        Node root = new Sequence(0,
                new Action(1),
                new Action(2)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(ACTION, tree.last().state());
    }

    @Test
    public void unknownStateShouldResultInBelief() {
        Map<Integer, Status> states = of(
                1, SUCCESS
        );
        Node root = new Sequence(0,
                new Belief(1),
                new Belief(2)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(BELIEF, tree.last().state());
    }

    @Test
    public void shouldReturnAllLeavesStatesWithIds() {
        Map<Integer, Status> states = of(
                1, SUCCESS,
                2, SUCCESS,
                3, SUCCESS,
                4, RUNNING
        );
        Node root = new Sequence(0,
                new Belief(1),
                new Belief(2),
                new Action(3),
                new Action(4)
        );
        Ticked tree = tick(root, new State(states));
        assertEquals(List.of(
                new StateOf(SUCCESS, 1),
                new StateOf(SUCCESS, 2),
                new StateOf(SUCCESS, 3),
                new StateOf(RUNNING, 4)
        ),tree.states());
    }
}
