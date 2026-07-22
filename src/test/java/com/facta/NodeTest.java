package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("nodes")
public class NodeTest {

    Verifiable<Board, Boolean>              BS_OK;
    Verifiable<Board, Boolean>              BS_NK;
    Verifiable<Board, Boolean>              BS_OK_OK_NK;
    Verifiable<Board, Status>               AS_OK;
    Verifiable<Board, Status>               AS_NK;
    Verifiable<Board, Status>               AS_RN;
    Verifiable<Board, Status>               AS_RN_NK;
    Verifiable<Board, Status>               AS_RN_NK_NK;
    Verifiable<Board, Status>               AR_RN_OK;
    Verifiable<Board, Status>               AS_RN_OK;
    Function<Board, Status>                 A_EXCEPTION = (board) -> {throw new RuntimeException();};
    Function<Board, Boolean>                B_EXCEPTION = (board) -> {throw new RuntimeException();};
    Clock.World<Board> world;

    @BeforeEach
    public void setUp() {
        reset();
    }

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, tick(new Inverse<>(new Belief<>(BS_OK))));
        assertEquals(Status.SUCCESS, tick(new Inverse<>(new Belief<>(BS_NK))));
    }

    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, tick(new Sequence<>(of(new Belief<>(BS_OK), new Belief<>(BS_OK)))));
        assertEquals(2, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Sequence<>(of(new Belief<>(BS_OK), new Belief<>(BS_NK)))));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Sequence<>(of(new Belief<>(BS_NK), new Belief<>(BS_OK)))));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Sequence<>(of(new Belief<>(BS_NK), new Belief<>(BS_NK)))));
        assertEquals(0, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();
    }

    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, tick(new Fallback<>(of(new Belief<>(BS_OK), new Belief<>(BS_OK)))));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, tick(new Fallback<>(of(new Belief<>(BS_OK), new Belief<>(BS_NK)))));
        assertEquals(1, BS_OK.invoked);
        assertEquals(0, BS_NK.invoked);
        reset();

        assertEquals(Status.SUCCESS, tick(new Fallback<>(of(new Belief<>(BS_NK), new Belief<>(BS_OK)))));
        assertEquals(1, BS_OK.invoked);
        assertEquals(1, BS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Fallback<>(of(new Belief<>(BS_NK), new Belief<>(BS_NK)))));
        assertEquals(0, BS_OK.invoked);
        assertEquals(2, BS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, tick(new Sequence<>(of(new Action<>(0, AS_OK), new Action<>(1, AS_OK)))));
        assertEquals(2, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Sequence<>(of(new Action<>(2, AS_NK), new Action<>(3, AS_OK)))));
        assertEquals(0, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();

        assertEquals(Status.FAILURE, tick(new Sequence<>(of(new Action<>(4, AS_OK), new Action<>(5, AS_NK)))));
        assertEquals(1, AS_OK.invoked);
        assertEquals(1, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, tick(new Sequence<>(of(new Action<>(0, AS_RN), new Action<>(1, AS_OK)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, tick(new Sequence<>(of(new Action<>(2, AS_RN), new Action<>(3, AS_NK)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, tick(new Sequence<>(of(new Action<>(4, AS_RN), new Action<>(5, AS_RN)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, tick(new Fallback<>(of(new Action<>(6, AS_RN), new Action<>(7, AS_OK)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, tick(new Fallback<>(of(new Action<>(8, AS_RN), new Action<>(9, AS_NK)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();

        assertEquals(Status.RUNNING, tick(new Fallback<>(of(new Action<>(10, AS_RN), new Action<>(11, AS_RN)))));
        assertEquals(1, AS_RN.invoked);
        assertEquals(0, AS_OK.invoked);
        assertEquals(0, AS_NK.invoked);
        reset();
    }

    @Test
    void shouldActionAndBeliefBePanicSafe() {
        assertEquals(Status.FAILURE, tick(new Action<>(0, A_EXCEPTION)));
        assertEquals(Status.FAILURE, tick(new Belief<>(B_EXCEPTION)));
    }

    @Test
    void shouldShortCircuitWhenFindRunning() {
        Node<Board> root = new Fallback<>(of(new Action<>(0, AS_RN_NK)));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.FAILURE, tick(root));
    }

    @Test
    void shouldCacheSequenceActionResult() {
        Node<Board> root = new Sequence<>(of(new Action<>(0, AS_RN_NK), new Action<>(1, AS_OK)));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.FAILURE, tick(root));
        assertEquals(Status.FAILURE, tick(root));
        assertEquals(Status.FAILURE, tick(root));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(0, AS_OK.invoked);
    }

    @Test
    void shouldCacheFallbackActionResult() {
        Node<Board> root = new Fallback<>(of(new Action<>(0, AS_RN_NK), new Action<>(1, AS_OK)));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.SUCCESS, tick(root));
        assertEquals(Status.SUCCESS, tick(root));

        Assertions.assertEquals(2, AS_RN_NK.invoked);
        Assertions.assertEquals(1, AS_OK.invoked);
    }

    @Test
    void shouldRemoveCacheWhenActionIsUnreachable() {
        Node<Board> root = new Sequence<>(of(new Belief<>(BS_OK_OK_NK), new Action<>(0, AS_RN_OK)));

        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.SUCCESS, tick(root));
        assertEquals(Status.FAILURE, tick(root));

        assertEquals(0, world.cached().size());
        assertEquals(0, world.active().size());

    }

    @Test
    void shouldKeepAllTheSequenceCached() {
        Node<Board> root = new Sequence<>(of(
                new Belief<>(BS_OK),
                new Action<>(0, AR_RN_OK),
                new Action<>(1, AR_RN_OK),
                new Action<>(2, AR_RN_OK)));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.RUNNING, tick(root));
        assertEquals(Status.SUCCESS, tick(root));
        assertEquals(Status.SUCCESS, tick(root));
        assertEquals(Status.SUCCESS, tick(root));

        assertEquals(6, AR_RN_OK.invoked);
        assertEquals(3, world.cached().size());
    }

    public Status tick(Node<Board> node) {
        return new Live<>(node, world).tick();
    }

    static class Verifiable<B, T> implements Function<B, T> {
        int invoked = 0;
        private final Function<B, T> decorate;
        public  Verifiable (Function<B, T> decorate) {
            this.decorate = decorate;
        }

        @Override
        public T apply(B b) {
            invoked++;
            return this.decorate.apply(b);
        }
    }

    static class Sequential<B, T> implements Function<B, T> {

        int invoked = 0;
        private final T[] sequence;
        private int index = 0;

        @SafeVarargs
        public Sequential(T ... sequence) {
            this.sequence = sequence;
        }

        @Override
        public T apply(B b) {
            invoked++;
            if(index == sequence.length - 1) {
                return sequence[index];
            }
            return sequence[index++];
        }
    }

    static class Rotational<B, T> implements Function<B, T> {
        private final T[] rotation;
        int index = 0;

        @SafeVarargs
        Rotational(T ... rotation) {
            this.rotation = rotation;
        }

        @Override
        public T apply(B b) {
            if(index == rotation.length - 1) {
                var tmp = rotation[index];
                index = 0;
                return tmp;
            }
            return rotation[index++];
        }
    }

    private void reset() {
        BS_OK_OK_NK = new Verifiable<>(new Sequential<>(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        BS_OK = new Verifiable<>(new Sequential<>(Boolean.TRUE));
        BS_NK = new Verifiable<>(new Sequential<>(Boolean.FALSE));
        AS_OK = new Verifiable<>(new Sequential<>(Status.SUCCESS));
        AS_NK = new Verifiable<>(new Sequential<>(Status.FAILURE));
        AS_RN = new Verifiable<>(new Sequential<>(Status.RUNNING));
        AS_RN_NK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.FAILURE));
        AS_RN_NK_NK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.FAILURE, Status.FAILURE));
        AS_RN_OK = new Verifiable<>(new Sequential<>(Status.RUNNING, Status.SUCCESS));
        AR_RN_OK = new Verifiable<>(new Rotational<>(Status.RUNNING, Status.SUCCESS));
        world = new Clock.World<>(new Board(), new HashMap<>(), new HashSet<>());
    }

    public static class Board{}
}
