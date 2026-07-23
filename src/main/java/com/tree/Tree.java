package com.tree;

import java.util.ArrayList;
import java.util.List;

import static com.tree.Status.*;

public class Tree {

    public record Ticked(List<StateOf> statuses) {

        public StateOf last() {
            return statuses.getLast();
        }
    }

    public static Ticked tick(Node node, Cache cache) {
        return switch (node) {
            case Node.Action action ->
                    new Ticked(List.of(
                            new StateOf(cache.cached().getOrDefault(action.id(), ACTION), action.id())));

            case Node.Belief belief ->
                    new Ticked(List.of(
                            new StateOf(cache.cached().getOrDefault(belief.id(), BELIEF), belief.id())));

            case Node.Sequence sequence -> {
                var statuses = new ArrayList<StateOf>();

                for (var child : sequence.children()) {
                    var tick = tick(child, cache);
                    statuses.addAll(tick.statuses());

                    var state = tick.last().state();

                    if (state != SUCCESS) {
                        statuses.add(new StateOf(state, sequence.id()));
                        yield new Ticked(List.copyOf(statuses));
                    }
                }

                statuses.add(new StateOf(SUCCESS, sequence.id()));
                yield new Ticked(List.copyOf(statuses));
            }

            case Node.Fallback fallback -> {
                var statuses = new ArrayList<StateOf>();

                for (var child : fallback.children()) {
                    var tick = tick(child, cache);
                    statuses.addAll(tick.statuses());

                    var state = tick.last().state();

                    if (state != FAILURE) {
                        statuses.add(new StateOf(state, fallback.id()));
                        yield new Ticked(List.copyOf(statuses));
                    }
                }

                statuses.add(new StateOf(FAILURE, fallback.id()));
                yield new Ticked(List.copyOf(statuses));
            }
        };
    }
}
