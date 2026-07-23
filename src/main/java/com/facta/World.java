package com.facta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.facta.Status.*;

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
        this.board = board;
        this.belief = belief;
        this.action = action;
        this.root = root;
        this.state = new State(new HashMap<>());
    }

    public void loop() {
        int count = 9;
        while (count-- > 0) {
            Tree.Ticked ticked = Tree.tick(root, state);

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
