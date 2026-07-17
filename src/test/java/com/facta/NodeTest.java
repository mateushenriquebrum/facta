package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {

    VerifiableCondition BOK = new VerifiableCondition(Verification.SUCCESS);
    VerifiableCondition BNK = new VerifiableCondition(Verification.FAILURE);
    VerifiableAction    AOK = new VerifiableAction(Status.SUCCESS);
    VerifiableAction    ANK = new VerifiableAction(Status.FAILURE);
    VerifiableAction    ARN = new VerifiableAction(Status.RUNNING);

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, new Inverse(new Belief(BOK)).tick());
        assertEquals(Status.SUCCESS, new Inverse(new Belief(BNK)).tick());
    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Belief(BOK), new Belief(BOK)).tick());
        assertEquals(2, BOK.invoked);
        assertEquals(0, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(BOK), new Belief(BNK)).tick());
        assertEquals(1, BOK.invoked);
        assertEquals(1, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(BNK), new Belief(BOK)).tick());
        assertEquals(0, BOK.invoked);
        assertEquals(1, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Belief(BNK), new Belief(BNK)).tick());
        assertEquals(0, BOK.invoked);
        assertEquals(1, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

    }
    @Test
    public void shouldBeliefShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(BOK), new Belief(BOK)).tick());
        assertEquals(1, BOK.invoked);
        assertEquals(0, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(BOK), new Belief(BNK)).tick());
        assertEquals(1, BOK.invoked);
        assertEquals(0, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.SUCCESS, new Fallback(new Belief(BNK), new Belief(BOK)).tick());
        assertEquals(1, BOK.invoked);
        assertEquals(1, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;

        assertEquals(Status.FAILURE, new Fallback(new Belief(BNK), new Belief(BNK)).tick());
        assertEquals(0, BOK.invoked);
        assertEquals(2, BNK.invoked);
        BOK.invoked = BNK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Action(AOK), new Action(AOK)).tick());
        assertEquals(2, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(ANK), new Action(AOK)).tick());
        assertEquals(0, AOK.invoked);
        assertEquals(1, ANK.invoked);
        ANK.invoked = AOK.invoked = 0;

        assertEquals(Status.FAILURE, new Sequence(new Action(AOK), new Action(ANK)).tick());
        assertEquals(1, AOK.invoked);
        assertEquals(1, ANK.invoked);
        ANK.invoked = AOK.invoked = 0;
    }

    @Test
    void shouldActionShortCircuitLikeLogicalOr() {
        assertEquals(Status.RUNNING, new Sequence(new Action(ARN), new Action(AOK)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(ARN), new Action(ANK)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;

        assertEquals(Status.RUNNING, new Sequence(new Action(ARN), new Action(ARN)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(ARN), new Action(AOK)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(ARN), new Action(ANK)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;

        assertEquals(Status.RUNNING, new Fallback(new Action(ARN), new Action(ARN)).tick());
        assertEquals(1, ARN.invoked);
        assertEquals(0, AOK.invoked);
        assertEquals(0, ANK.invoked);
        ANK.invoked = AOK.invoked = ARN.invoked = 0;
    }

    static class VerifiableCondition implements Supplier<Verification> {
        public int invoked = 0;
        private final Verification result;

        public VerifiableCondition(Verification result) {
            this.result = result;
        }

        @Override
        public Verification get() {
            invoked++;
            return result;
        }
    }

    static class VerifiableAction implements Supplier<Status> {
        public int invoked = 0;
        private final Status result;

        public VerifiableAction(Status result) {
            this.result = result;
        }

        @Override
        public Status get() {
            invoked++;
            return result;
        }
    }

}
