package com.facta;

import com.facta.Tree.Ticked;

import java.util.HashMap;
import java.util.List;
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

    public Ticked run(Integer times) {
        int counter = 0;
        Ticked ticked = null;
        while (counter++ < times) {
            ticked = Tree.tick(this.root, this.state);
            if (ticked.last().state() == BELIEF) {
                var result = belief.get(ticked.last().id()).apply(this.board) ? SUCCESS :FAILURE;
                this.state.state().put(ticked.last().id(), result);
            }
        }
        return ticked;
    }

    public void loop() {
        int count = 9;
        while (count-- > 0) {
            Ticked ticked = Tree.tick(root, state);

            ticked
                    .states()
                    .stream()
                    .filter(tk -> RUNNING.equals(tk.state()))
                    .findFirst()
                    .ifPresent(tk -> state.state().put(tk.id(), SUCCESS));

            ticked
                    .states()
                    .stream()
                    .filter(tk -> List.of(BELIEF, ACTION).contains(tk.state()))
                    .findFirst()
                    .ifPresent(tk -> {
                        if (tk.state() == BELIEF) {
                            if (belief.get(tk.id()).apply(this.board)){
                                state.state().put(tk.id(), SUCCESS);
                            } else {
                                state.state().put(tk.id(), FAILURE);
                            }
                        } else {
                            state.state().put(tk.id(), RUNNING);
                        }
                    });

            System.out.println(ticked);
        }
    }
}
