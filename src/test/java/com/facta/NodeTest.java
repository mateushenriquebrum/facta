package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {

    VerifiableCondition B_OK;
    VerifiableCondition B_NK;
    VerifiableAction    A_OK;
    VerifiableAction    A_NK;
    VerifiableAction    A_RN;
    VerifiableAction    A_RN_NK;
    VerifiableAction    A_RN_NK_NK;

    Context context;

    @BeforeEach
    public void setUp() {
        B_OK = new VerifiableCondition(Verification.SUCCESS);
        B_NK = new VerifiableCondition(Verification.FAILURE);
        A_OK = new VerifiableAction(Status.SUCCESS);
        A_NK = new VerifiableAction(Status.FAILURE);
        A_RN = new VerifiableAction(Status.RUNNING);
        A_RN_NK = new VerifiableAction(Status.RUNNING, Status.FAILURE);
        A_RN_NK_NK = new VerifiableAction(Status.RUNNING, Status.FAILURE, Status.FAILURE);
        context = new Context();
    }

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, new Inverse(new Belief(B_OK)).tick(context));
        assertEquals(Status.SUCCESS, new Inverse(new Belief(B_NK)).tick(context));
    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Belief(B_OK), new Belief(B_OK)).tick(context));
        assertEquals(2, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_OK), new Belief(B_NK)).tick(context));
        assertEquals(1, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_NK), new Belief(B_OK)).tick(context));
        assertEquals(0, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_NK), new Belief(B_NK)).tick(context));
        assertEquals(0, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_OK), new Belief(B_OK)).tick(context));
        assertEquals(1, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_OK), new Belief(B_NK)).tick(context));
        assertEquals(1, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_NK), new Belief(B_OK)).tick(context));
        assertEquals(1, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Fallback(new Belief(B_NK), new Belief(B_NK)).tick(context));
        assertEquals(0, B_OK.invoked);
        assertEquals(2, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Action(0, A_OK), new Action(1, A_OK)).tick(context));
        assertEquals(2, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(2, A_NK), new Action(3, A_OK)).tick(context));
        assertEquals(0, A_OK.invoked);
        assertEquals(1, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(4, A_OK), new Action(5, A_NK)).tick(context));
        assertEquals(1, A_OK.invoked);
        assertEquals(1, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_OK)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_NK)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_RN)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_OK)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_NK)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_RN)).tick(context));
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;
    }

    @Test
    void shouldActionAndBeliefBePanicSafe() {
        assertEquals(Status.FAILURE, new Action(() -> {
            throw new RuntimeException();
        }).tick(context));

        assertEquals(Status.FAILURE, new Belief(() -> {
            throw new RuntimeException();
        }).tick(context));
    }

    @Test
    void shouldShortCircuitWhenFindRunning() {
        Node root = new Fallback(new Action(A_RN_NK));
        assertEquals(Status.RUNNING, root.tick(context));
        assertEquals(Status.FAILURE, root.tick(context));
    }


    @Test
    void shouldCacheActionResult() {
        Node root = new Sequence(new Action(A_RN_NK));
        assertEquals(Status.RUNNING, root.tick(context));
        assertEquals(Status.FAILURE, root.tick(context));
        assertEquals(Status.FAILURE, root.tick(context));
        assertEquals(Status.FAILURE, root.tick(context));
        Assertions.assertEquals(2, A_RN_NK.invoked);
    }

    @Test
    void shouldCleanUpFallbackBranchNotBeingActive() {
        Node fallback = new Fallback(
                new Action(0, A_RN_NK_NK),
                new Action(1, A_OK));
        assertEquals(Status.RUNNING, fallback.tick(context));
        assertEquals(Status.SUCCESS, fallback.tick(context));
        assertEquals(Status.SUCCESS, fallback.tick(context));

        Assertions.assertEquals(2, A_RN_NK.invoked);
        Assertions.assertEquals(1, A_OK.invoked);
    }

    @Test
    void shouldCleanUpSequenceBranchNotBeingActive() {
        Node sequence = new Sequence(
                new Action(A_RN_NK),
                new Action(A_OK));
        assertEquals(Status.RUNNING, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));
        assertEquals(Status.FAILURE, sequence.tick(context));

        Assertions.assertEquals(2, A_RN_NK.invoked);
        Assertions.assertEquals(0, A_OK.invoked);
    }

//    @Test
//    void shouldTrampolineBetweenActions() {
//        Node root = new Fallback(
//                new Sequence(new Belief(B_OK_OK_NK_NK), new Action(0, A_RN_OK)),
//                new Sequence(new Belief(B_OK_OK), new Action(1, A_RN_OK))
//        );
//        assertEquals(Status.RUNNING, root.tick(context));
//        assertEquals(Status.SUCCESS, root.tick(context));
//        assertEquals(Status.RUNNING, root.tick(context));
//        assertEquals(Status.SUCCESS, root.tick(context));
//
//        assertEquals(Status.RUNNING, root.tick(context));
//        assertEquals(Status.SUCCESS, root.tick(context));
//        assertEquals(Status.RUNNING, root.tick(context));
//        assertEquals(Status.SUCCESS, root.tick(context));
//
//    }

    static class VerifiableCondition implements Supplier<Verification> {
        public int invoked = 0;
        private final Verification[] sequence;
        int index = 0;

        public VerifiableCondition(Verification ... rotate) {
            this.sequence = rotate;
        }

        @Override
        public Verification get() {
            invoked++;
            if(index == sequence.length - 1) {
                return sequence[index];
            }
            return sequence[index++];
        }
    }

    static class VerifiableAction implements Supplier<Status> {
        public int invoked = 0;
        private final Status[] sequence;
        int index = 0;

        public VerifiableAction(Status ... sequence) {
            this.sequence = sequence;
        }

        @Override
        public Status get() {
            invoked++;
            if(index == sequence.length - 1) {
                return sequence[index];
            }
            return sequence[index++];
        }
    }
}
