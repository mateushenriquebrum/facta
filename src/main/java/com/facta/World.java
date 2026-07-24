package com.facta;

import com.facta.Tree.Ticked;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final Sandbox<B> sandbox;
    private final Node root;
    private final State state;

    public World(
            B board,
            Map<Integer, Function<B, Boolean>> belief,
            Map<Integer, Function<B, Boolean>> action,
            Node root,
            Sandbox<B> sandbox) {
        requireNonNull(board);
        requireNonNull(belief);
        requireNonNull(action);
        requireNonNull(root);
        this.board = board;
        this.belief = belief;
        this.action = action;
        this.root = root;
        this.sandbox = sandbox;
        this.state = new State(new HashMap<>());
    }

    public Ticked once() {
        Ticked ticked = Tree.tick(this.root, this.state);
        StateOf last = ticked.last();
        Status status = switch (last.state()) {
            case BELIEF -> safelyDoBelief(ticked);
            case ACTION -> safelyDoAction(ticked);
            case RUNNING -> sandbox.status();
            default -> last.state();
        };
        this.state.state().put(last.id(), status);
        return ticked;
    }

    private Status safelyDoBelief(Ticked ticked) {
        try {
            Function<B, Boolean> run = belief.get(ticked.last().id());
            requireNonNull(run, "A function is required at this stage");
            return run.apply(this.board) ? SUCCESS : FAILURE;
        } catch (Exception e) {
            return FAILURE;
        }
    }

    private Status safelyDoAction(Ticked ticked) {
        Function<B, Boolean> run = action.get(ticked.last().id());
        requireNonNull(run, "A function is required at this stage");
        sandbox.spin(run);
        return sandbox.status();
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
