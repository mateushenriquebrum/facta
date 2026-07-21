package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.*;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("nodes")
public class NodeTest {

    Verifiable<Verification>        BS_OK;
    Verifiable<Verification>        BS_NK;
    Verifiable<Verification>        BR_OK_OK_NK;
    Verifiable<Status>              AS_OK;
    Verifiable<Status>              AS_NK;
    Verifiable<Status>              AS_RN;
    Verifiable<Status>              AS_RN_NK;
    Verifiable<Status>              AS_RN_NK_NK;
    Verifiable<Status>              AS_RN_OK;

    Context context;

    @BeforeEach
    public void setUp() {
        reset();
    }

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, new Inverse(new Belief(BS_OK)).tick(context));
        assertEquals(Status.SUCCESS, new Inverse(new Belief(BS_NK)).tick(context));
    }

    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Belief(BS_OK), new Belief(BS_OK)).tick(context));
        assertEquals(2, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Sequence(new Belief(BS_OK), new Belief(BS_NK)).tick(context));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Sequence(new Belief(BS_NK), new Belief(BS_OK)).tick(context));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Sequence(new Belief(BS_NK), new Belief(BS_NK)).tick(context));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();
    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(BS_OK), new Belief(BS_OK)).tick(context));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, new Fallback(new Belief(BS_OK), new Belief(BS_NK)).tick(context));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, new Fallback(new Belief(BS_NK), new Belief(BS_OK)).tick(context));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Fallback(new Belief(BS_NK), new Belief(BS_NK)).tick(context));
        assertEquals(0, BS_OK.invoked);
        assertEquals(2, BS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Action(0, AS_OK), new Action(1, AS_OK)).tick(context));
        assertEquals(2, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Sequence(new Action(2, AS_NK), new Action(3, AS_OK)).tick(context));
        assertEquals(0, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, new Sequence(new Action(4, AS_OK), new Action(5, AS_NK)).tick(context));
        assertEquals(1, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, new Sequence(new Action(0, AS_RN), new Action(1, AS_OK)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, new Sequence(new Action(2, AS_RN), new Action(3, AS_NK)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, new Sequence(new Action(4, AS_RN), new Action(5, AS_RN)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, new Fallback(new Action(6, AS_RN), new Action(7, AS_OK)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, new Fallback(new Action(8, AS_RN), new Action(9, AS_NK)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, new Fallback(new Action(10, AS_RN), new Action(11, AS_RN)).tick(context));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionAndBeliefBePanicSafe() {
        assertEquals(Status.FAILURE, new Action(0, () -> {
            throw new RuntimeException();
        }).tick(context));

        assertEquals(Status.FAILURE, new Belief(() -> {
            throw new RuntimeException();
        }).tick(context));
    }

    @Test
    void shouldShortCircuitWhenFindRunning() {
        Node root = new Fallback(new Action(0, AS_RN_NK));
        assertEquals(Status.RUNNING, root.tick(context));
        assertEquals(Status.FAILURE, root.tick(context));
    }

    @Test
    void shouldCacheSequenceActionResult() {
        Node sequence = new Sequence(
                new Action(0, AS_RN_NK),
                new Action(1, AS_OK));
        assertEquals(Status.RUNNING, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(0, AS_OK.invoked);
    }

    @Test
    void shouldCacheFallbackActionResult() {
        Node fallback = new Fallback(
                new Action(0, AS_RN_NK),
                new Action(1, AS_OK));
        assertEquals(Status.RUNNING, fallback.tick(context));
        assertEquals(Status.SUCCESS, fallback.tick(context));
        assertEquals(Status.SUCCESS, fallback.tick(context));
        assertEquals(Status.SUCCESS, fallback.tick(context));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(1, AS_OK.invoked);
    }

    @Test
    void shouldRemoveCacheWhenActionIsUnreachable() {
        Node root = new Sequence(new Belief(BR_OK_OK_NK), new Action(0, AS_RN_OK));

        context.removeInactive();
        context.prepareCollectActive();
        assertEquals(Status.RUNNING, root.tick(context));
        context.removeInactive();
        context.prepareCollectActive();
        assertEquals(Status.SUCCESS, root.tick(context));
        context.removeInactive();
        context.prepareCollectActive();
        assertEquals(Status.FAILURE, root.tick(context));
        context.removeInactive();
        context.prepareCollectActive();

        assertEquals(context.cached.size(), 0);
        assertEquals(context.active.size(), 0);

    }

    static class Verifiable<T> implements Supplier<T> {
        int invoked = 0;
        private final Supplier<T> decorate;
        public  Verifiable (Supplier<T> decorate) {
            this.decorate = decorate;
        }

        @Override
        public T get() {
            invoked++;
            return this.decorate.get();
        }
    }

    static class Sequential<T> implements Supplier<T> {

        int invoked = 0;
        private final T[] sequence;
        private int index = 0;

        @SafeVarargs
        public Sequential(T ... sequence) {
            this.sequence = sequence;
        }

        @Override
        public T get() {
            invoked++;
            if(index == sequence.length - 1) {
                return sequence[index];
            }
            return sequence[index++];
        }
    }

    static class Rotational<T> implements Supplier<T> {
        private final T[] rotation;
        int index = 0;

        @SafeVarargs
        Rotational(T ... rotation) {
            this.rotation = rotation;
        }

        @Override
        public T get() {
            if(index == rotation.length - 1) {
                var tmp = rotation[index];
                index = 0;
                return tmp;
            }
            return rotation[index++];
        }
    }

    private void reset() {
        BS_OK = new Verifiable<>(new Sequential<>(Verification.SUCCESS));
        BS_NK = new Verifiable<>(new Sequential<>(Verification.FAILURE));
        AS_OK = new Verifiable<>(new Sequential<>(Status.SUCCESS));
        AS_NK = new Verifiable<>(new Sequential<>(Status.FAILURE));
        AS_RN = new Verifiable<>(new Sequential<>(Status.RUNNING));
        AS_RN_NK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.FAILURE));
        AS_RN_NK_NK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.FAILURE, Status.FAILURE));
        AS_RN_OK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.SUCCESS));
        BR_OK_OK_NK = new Verifiable<>(new Rotational<>(Verification.SUCCESS, Verification.SUCCESS, Verification.FAILURE));
        context = new Context();
    }
}
