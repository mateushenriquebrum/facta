package com.facta;

import java.util.ArrayList;
import java.util.List;

import static com.facta.Status.*;

public class Tree {

    public record Ticked(List<StateOf> states) {
        public StateOf last() {
            return states.getLast();
        }
    }

    public static Ticked tick(Node node, State cache) {
        return switch (node) {
            case Node.Action action ->
                    new Ticked(List.of(
                            new StateOf(action.id(), cache.state().getOrDefault(action.id(), ACTION))));

            case Node.Belief belief ->
                    new Ticked(List.of(
                            new StateOf(belief.id(), cache.state().getOrDefault(belief.id(), BELIEF))));

            case Node.Sequence sequence -> {
                var statuses = new ArrayList<StateOf>();

                for (var child : sequence.children()) {
                    var tick = tick(child, cache);
                    statuses.addAll(tick.states());

                    var state = tick.last().state();

                    if (state != SUCCESS) {
                        yield new Ticked(List.copyOf(statuses));
                    }
                }

                statuses.add(new StateOf(sequence.id(), SUCCESS));
                yield new Ticked(List.copyOf(statuses));
            }

            case Node.Fallback fallback -> {
                var statuses = new ArrayList<StateOf>();

                for (var child : fallback.children()) {
                    var tick = tick(child, cache);
                    statuses.addAll(tick.states());

                    var state = tick.last().state();

                    if (state != FAILURE) {
                        yield new Ticked(List.copyOf(statuses));
                    }
                }

                statuses.add(new StateOf(fallback.id(), FAILURE));
                yield new Ticked(List.copyOf(statuses));
            }
        };
    }
}
