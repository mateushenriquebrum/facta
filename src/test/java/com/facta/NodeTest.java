package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.function.Supplier;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("nodes")
public class NodeTest {

    Verifiable<Verification>        BS_OK;
    Verifiable<Verification>        BS_NK;
    Verifiable<Verification>        BS_OK_OK_NK;
    Verifiable<Verification>        BR_OK_OK_NK;
    Verifiable<Status>              AS_OK;
    Verifiable<Status>              AS_NK;
    Verifiable<Status>              AS_RN;
    Verifiable<Status>              AS_RN_NK;
    Verifiable<Status>              AS_RN_NK_NK;
    Verifiable<Status>              AR_RN_OK;
    Verifiable<Status>              AS_RN_OK;
    Supplier<Status>                A_EXCEPTION = () -> {throw new RuntimeException();};
    Supplier<Verification>          B_EXCEPTION = () -> {throw new RuntimeException();};
    Context context;

    @BeforeEach
    public void setUp() {
        reset();
    }

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, context.prepare(new Inverse(new Belief(BS_OK))::tick));
        assertEquals(Status.SUCCESS, context.prepare(new Inverse(new Belief(BS_NK))::tick));
    }

    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, context.prepare(new Sequence(of(new Belief(BS_OK), new Belief(BS_OK)))::tick));
        assertEquals(2, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(new Sequence(of(new Belief(BS_OK), new Belief(BS_NK)))::tick));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(new Sequence(of(new Belief(BS_NK), new Belief(BS_OK)))::tick));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(new Sequence(of(new Belief(BS_NK), new Belief(BS_NK)))::tick));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();
    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, context.prepare(
                new Fallback(of(new Belief(BS_OK), new Belief(BS_OK)))::tick));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, context.prepare(
                new Fallback(of(new Belief(BS_OK), new Belief(BS_NK)))::tick));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, context.prepare(
                new Fallback(of(new Belief(BS_NK), new Belief(BS_OK)))::tick));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(
                new Fallback(of(new Belief(BS_NK), new Belief(BS_NK)))::tick));
        assertEquals(0, BS_OK.invoked);
        assertEquals(2, BS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, context.prepare(
                new Sequence(of(new Action(0, AS_OK), new Action(1, AS_OK)))::tick));
        assertEquals(2, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(
                new Sequence(of(new Action(2, AS_NK), new Action(3, AS_OK)))::tick));
        assertEquals(0, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, context.prepare(
                new Sequence(of(new Action(4, AS_OK), new Action(5, AS_NK)))::tick));
        assertEquals(1, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, context.prepare(
                new Sequence(of(new Action(0, AS_RN), new Action(1, AS_OK)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, context.prepare(
                new Sequence(of(new Action(2, AS_RN), new Action(3, AS_NK)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, context.prepare(
                new Sequence(of(new Action(4, AS_RN), new Action(5, AS_RN)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, context.prepare(
                new Fallback(of(new Action(6, AS_RN), new Action(7, AS_OK)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, context.prepare(
                new Fallback(of(new Action(8, AS_RN), new Action(9, AS_NK)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, context.prepare(
                new Fallback(of(new Action(10, AS_RN), new Action(11, AS_RN)))::tick));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionAndBeliefBePanicSafe() {
        assertEquals(Status.FAILURE, context.prepare(new Action(0, A_EXCEPTION)::tick));
        assertEquals(Status.FAILURE, context.prepare(new Belief(B_EXCEPTION)::tick));
    }

    @Test
    void shouldShortCircuitWhenFindRunning() {
        Node root = new Fallback(of(new Action(0, AS_RN_NK)));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.FAILURE, context.prepare(root::tick));
    }

    @Test
    void shouldCacheSequenceActionResult() {
        Node root = new Sequence(of(new Action(0, AS_RN_NK), new Action(1, AS_OK)));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.FAILURE, context.prepare(root::tick));
        assertEquals(Status.FAILURE, context.prepare(root::tick));
        assertEquals(Status.FAILURE, context.prepare(root::tick));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(0, AS_OK.invoked);
    }

    @Test
    void shouldCacheFallbackActionResult() {
        Node root = new Fallback(of(new Action(0, AS_RN_NK), new Action(1, AS_OK)));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(1, AS_OK.invoked);
    }

    @Test
    void shouldRemoveCacheWhenActionIsUnreachable() {
        Node root = new Sequence(of(new Belief(BS_OK_OK_NK), new Action(0, AS_RN_OK)));

        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));
        assertEquals(Status.FAILURE, context.prepare(root::tick));

        assertEquals(0, context.cached.size());
        assertEquals(0, context.active.size());

    }

    @Test
    void shouldKeepAllTheSequenceCached() {
        Node root = new Sequence(of(new Belief(BS_OK),
                new Action(0, AR_RN_OK),
                new Action(1, AR_RN_OK),
                new Action(2, AR_RN_OK)));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.RUNNING, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));
        assertEquals(Status.SUCCESS, context.prepare(root::tick));

        assertEquals(6, AR_RN_OK.invoked);
        assertEquals(3, context.cached.size());
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
        BS_OK_OK_NK = new Verifiable<>(new Sequential<>(Verification.SUCCESS, Verification.SUCCESS, Verification.FAILURE));
        AR_RN_OK = new Verifiable<>(new Rotational<>(Status.RUNNING, Status.SUCCESS));
        context = new Context();
    }
}
