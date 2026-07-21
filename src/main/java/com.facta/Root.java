package com.facta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.facta.Node.Status.FAILURE;
import static com.facta.Node.Status.SUCCESS;

final public class Root<B> {
    private static final Logger LOG = LoggerFactory.getLogger(Root.class);

    public record Context<B>(B board, Map<Integer, Node.Status> cached, Set<Integer> active) {
        public void ticked() {
            this.cached.keySet().retainAll(active);
            this.active.clear();
        }
    }

    public static <B> Node.Status tick(Node<B> node, Context<B> context) {
        return switch (node) {
            case Node.Sequence<B> sequence -> {
                LOG.debug("Sequence, context {}, children {}", context, sequence.children().size());
                for (Node<B> child : sequence.children()) {
                    Node.Status status = tick(child, context);
                    if(status != SUCCESS) {
                        LOG.debug("Sequence left prematurely after {}", status);
                        yield status;
                    }
                }
                LOG.debug("Sequence left after {}", SUCCESS);
                yield SUCCESS;
            }
            case Node.Fallback<B> fall -> {
                LOG.debug("Fallback context {}, children {}", context, fall.children().size());
                for (Node<B> child : fall.children()) {
                    Node.Status status = tick(child, context);
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
                    yield belief.condition().apply(context.board) == Node.Verification.SUCCESS
                            ? Node.Status.SUCCESS
                            : Node.Status.FAILURE;
                } catch (Exception e) {
                    LOG.error("Failed executing belief logic", e);
                    yield Node.Status.FAILURE;
                }
            }
            case Node.Inverse<B> inverse ->
                    tick(inverse.belief(), context) == Node.Status.SUCCESS
                            ? Node.Status.FAILURE
                            : Node.Status.SUCCESS;

            case Node.Action<B> action -> {
                Integer id = action.id();

                // Cache hit check
                if (context.cached().containsKey(id)) {
                    context.active().add(id);
                    yield context.cached().get(id);
                }

                // Computation
                Node.Status result;
                try {
                    result = action.perform().apply(context.board);
                } catch (Exception ex) {
                    LOG.error("Action execution exception at ID: {}", id, ex);
                    result = Node.Status.FAILURE;
                }

                // Cache persistence criteria
                if (result != Node.Status.RUNNING) {
                    context.cached().put(id, result);
                    context.active().add(id);
                }
                yield result;
            }
        };
    }
}
