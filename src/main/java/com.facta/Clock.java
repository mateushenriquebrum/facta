package com.facta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.facta.Node.Status.FAILURE;
import static com.facta.Node.Status.SUCCESS;

final public class Clock<B> {
    private static final Logger LOG = LoggerFactory.getLogger(Clock.class);

    public record World<B>(B board, Map<Integer, Node.Status> cached, Set<Integer> active) {
        public void ticked() {
            this.cached.keySet().retainAll(active);
            this.active.clear();
        }
    }

    public static <B> Node.Status tick(Node<B> node, World<B> world) {
        return switch (node) {
            case Node.Sequence<B> sequence -> {
                LOG.debug("Sequence, world {}, children {}", world, sequence.children().size());
                for (Node<B> child : sequence.children()) {
                    Node.Status status = tick(child, world);
                    if(status != SUCCESS) {
                        LOG.debug("Sequence left prematurely after {}", status);
                        yield status;
                    }
                }
                LOG.debug("Sequence left after {}", SUCCESS);
                yield SUCCESS;
            }
            case Node.Fallback<B> fall -> {
                LOG.debug("Fallback world {}, children {}", world, fall.children().size());
                for (Node<B> child : fall.children()) {
                    Node.Status status = tick(child, world);
                    if(status != FAILURE) {
                        LOG.debug("Fallback left prematurely after {}", status);
                        yield status;
                    }
                }
                LOG.debug("Fallback left after {}", FAILURE);
                yield FAILURE;
            }
            case Node.Belief<B> belief -> {
                try {
                    yield belief.condition().apply(world.board)
                            ? Node.Status.SUCCESS
                            : Node.Status.FAILURE;
                } catch (Exception e) {
                    LOG.error("Failed executing belief logic", e);
                    yield Node.Status.FAILURE;
                }
            }
            case Node.Inverse<B> inverse ->
                    tick(inverse.belief(), world) == Node.Status.SUCCESS
                            ? Node.Status.FAILURE
                            : Node.Status.SUCCESS;

            case Node.Action<B> action -> {
                Integer id = action.id();

                // Cache hit check
                if (world.cached().containsKey(id)) {
                    world.active().add(id);
                    yield world.cached().get(id);
                }

                // Computation
                Node.Status result;
                try {
                    result = action.perform().apply(world.board);
                } catch (Exception ex) {
                    LOG.error("Action execution exception at ID: {}", id, ex);
                    result = Node.Status.FAILURE;
                }

                // Cache persistence criteria
                if (result != Node.Status.RUNNING) {
                    world.cached().put(id, result);
                    world.active().add(id);
                }
                yield result;
            }
        };
    }
}
