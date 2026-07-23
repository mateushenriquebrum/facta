package com.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.tree.Status.*;

public class World<B> {
    private final Map<Integer, Function<B, Boolean>> belief;
    private final Map<Integer, Function<B, Boolean>> action;
    private final Node root;
    private final Cache cache;

    public World(
            Map<Integer, Function<B, Boolean>> belief,
            Map<Integer, Function<B, Boolean>> action,
            Node root) {
        this.belief = belief;
        this.action = action;
        this.root = root;
        this.cache = new Cache(new HashMap<>());
    }

    public void loop() {
        int count = 9;
        while (count-- > 0) {
            Tree.Ticked ticked = Tree.tick(root, cache);

            ticked
                    .statuses()
                    .stream()
                    .filter(tk -> RUNNING.equals(tk.state()))
                    .findFirst()
                    .ifPresent(tk -> {
                        cache.cached().put(tk.id(), SUCCESS);
                    });

            ticked
                    .statuses()
                    .stream()
                    .filter(tk -> List.of(BELIEF, ACTION).contains(tk.state()))
                    .findFirst()
                    .ifPresent(tk -> {
                        if (tk.state() == BELIEF) {
                            cache.cached().put(tk.id(), SUCCESS);
                        } else {
                            cache.cached().put(tk.id(), RUNNING);
                        }
                    });

            System.out.println(ticked);
        }
    }
}
