package com.facta;

import com.facta.Tree.Ticked;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.facta.Status.*;
import static java.util.Objects.requireNonNull;

/**
 * Unsafe class that deals with States and IO.
 * @param <B>
 */
public class World<B> {
    private final B board;
    private final Map<Integer, Function<B, Boolean>> belief;
    private final Map<Integer, Function<B, Boolean>> action;
    private final Node root;
    private final State state;

    public World(
            B board,
            Map<Integer, Function<B, Boolean>> belief,
            Map<Integer, Function<B, Boolean>> action,
            Node root) {
        requireNonNull(board);
        requireNonNull(belief);
        requireNonNull(action);
        requireNonNull(root);
        this.board = board;
        this.belief = belief;
        this.action = action;
        this.root = root;
        this.state = new State(new HashMap<>());
    }

    public Ticked once() {
        Ticked ticked = Tree.tick(this.root, this.state);
        StateOf last = ticked.last();
        if (last.state() == BELIEF) {
            var result = safelyDoBelief(ticked);
            this.state.state().put(last.id(), result);
        } else if (last.state() == ACTION) {
            //sandbox start
        } else if (last.state() == RUNNING) {
            //sandbox status
        }
        return ticked;
    }

    private Status safelyDoBelief(Ticked ticked) {
        try {
            return belief.get(ticked.last().id()).apply(this.board) ? SUCCESS : FAILURE;
        } catch (Exception e) {
            return FAILURE;
        }
    }

    public Ticked run(Integer times) {
        int counter = 0;
        Ticked ticked = null;
        while (counter++ < times) {
            ticked = once();
        }
        return ticked;
    }
}
