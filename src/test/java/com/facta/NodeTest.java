package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

    @BeforeEach
    public void setUp() {
        B_OK = new VerifiableCondition(Verification.SUCCESS);
        B_NK = new VerifiableCondition(Verification.FAILURE);
        A_OK = new VerifiableAction(Status.SUCCESS);
        A_NK = new VerifiableAction(Status.FAILURE);
        A_RN = new VerifiableAction(Status.RUNNING);
        A_RN_NK = new VerifiableAction(Status.RUNNING, Status.FAILURE);
    }

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, new Inverse(new Belief(B_OK)).tick());
        assertEquals(Status.SUCCESS, new Inverse(new Belief(B_NK)).tick());
    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Belief(B_OK), new Belief(B_OK)).tick());
        assertEquals(2, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_OK), new Belief(B_NK)).tick());
        assertEquals(1, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_NK), new Belief(B_OK)).tick());
        assertEquals(0, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(B_NK), new Belief(B_NK)).tick());
        assertEquals(0, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_OK), new Belief(B_OK)).tick());
        assertEquals(1, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_OK), new Belief(B_NK)).tick());
        assertEquals(1, B_OK.invoked);
        assertEquals(0, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(B_NK), new Belief(B_OK)).tick());
        assertEquals(1, B_OK.invoked);
        assertEquals(1, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;

        assertEquals(Status.FAILURE, new Fallback(new Belief(B_NK), new Belief(B_NK)).tick());
        assertEquals(0, B_OK.invoked);
        assertEquals(2, B_NK.invoked);
        B_OK.invoked = B_NK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Action(A_OK), new Action(A_OK)).tick());
        assertEquals(2, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(A_NK), new Action(A_OK)).tick());
        assertEquals(0, A_OK.invoked);
        assertEquals(1, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(A_OK), new Action(A_NK)).tick());
        assertEquals(1, A_OK.invoked);
        assertEquals(1, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_OK)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_NK)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(A_RN), new Action(A_RN)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_OK)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_NK)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(A_RN), new Action(A_RN)).tick());
        assertEquals(1, A_RN.invoked);
        assertEquals(0, A_OK.invoked);
        assertEquals(0, A_NK.invoked);
        A_NK.invoked = A_OK.invoked = A_RN.invoked = 0;
    }

    @Test
    void shouldActionAndBeliefBePanicSafe() {
        assertEquals(Status.FAILURE, new Action(() -> {
            throw new RuntimeException();
        }).tick());

        assertEquals(Status.FAILURE, new Belief(() -> {
            throw new RuntimeException();
        }).tick());
    }

    @Test
    void shouldShortCircuitWhenFindRunning() {
        Node root = new Fallback(new Action(A_RN_NK));
        assertEquals(Status.RUNNING, root.tick());
        assertEquals(Status.FAILURE, root.tick());
    }


    @Test
    void shouldCacheActionResult() {
        Node root = new Sequence(new Memoizer(new Action(A_RN_NK)));
        assertEquals(Status.RUNNING, root.tick());
        assertEquals(Status.FAILURE, root.tick());
        assertEquals(Status.FAILURE, root.tick());
        assertEquals(Status.FAILURE, root.tick());
        Assertions.assertEquals(2, A_RN_NK.invoked);
    }

    @Test
    void shouldCleanUpFallbackBranchNotBeingActive() {
        Node fallback = new Fallback(
                new Memoizer(new Action(A_RN_NK)),
                new Memoizer(new Action(A_OK)));
        assertEquals(Status.RUNNING, fallback.tick());
        assertEquals(Status.SUCCESS, fallback.tick());
        // tick make A_RN_NK evaluate again
        assertEquals(Status.RUNNING, fallback.tick());
        assertEquals(Status.SUCCESS, fallback.tick());

        Assertions.assertEquals(4, A_RN_NK.invoked);
        Assertions.assertEquals(2, A_OK.invoked);
    }

    @Test
    void shouldCleanUpSequenceBranchNotBeingActive() {
        Node sequence = new Sequence(
                new Memoizer(new Action(A_RN_NK)),
                new Memoizer(new Action(A_OK)));
        assertEquals(Status.RUNNING, sequence.tick());
        assertEquals(Status.FAILURE, sequence.tick());
        assertEquals(Status.FAILURE, sequence.tick());
        assertEquals(Status.FAILURE, sequence.tick());

        Assertions.assertEquals(2, A_RN_NK.invoked);
        Assertions.assertEquals(0, A_OK.invoked);
    }

    static class VerifiableCondition implements Supplier<Verification> {
        public int invoked = 0;
        private final Verification[] sequence;
        int index = 0;

        public VerifiableCondition(Verification ... result) {
            this.sequence = result;
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

        public VerifiableAction(Status ... result) {
            this.sequence = result;
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
