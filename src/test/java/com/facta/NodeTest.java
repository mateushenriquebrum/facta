package com.facta;

import com.facta.Node.*;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {

    Verifiable OK = new Verifiable(() -> Verification.SUCCESS);
    Verifiable NK = new Verifiable(() -> Verification.FAILURE);

    @Test
    public void shouldInvertBelief() {
        assertEquals(Status.FAILURE, new Inverse(new Belief(OK)).tick());
        assertEquals(Status.SUCCESS, new Inverse(new Belief(NK)).tick());
    }
    @Test
    public void shouldShortCircuitLikeLogicalAnd() {
        assertEquals(Status.SUCCESS, new Sequence(new Belief(OK), new Belief(OK)).tick());
        assertEquals(2, OK.invoked);
        assertEquals(0, NK.invoked);

        assertEquals(Status.FAILURE, new Sequence(new Belief(OK), new Belief(NK)).tick());
        assertEquals(2+1, OK.invoked);
        assertEquals(0+1, NK.invoked);


        assertEquals(Status.FAILURE, new Sequence(new Belief(NK), new Belief(OK)).tick());
        assertEquals(2+1+0, OK.invoked);
        assertEquals(0+1+1, NK.invoked);

        assertEquals(Status.FAILURE, new Sequence(new Belief(NK), new Belief(NK)).tick());
        assertEquals(2+1+0+0, OK.invoked);
        assertEquals(0+1+1+1, NK.invoked);
    }
    @Test
    public void shouldShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(OK), new Belief(OK)).tick());
        assertEquals(1, OK.invoked);
        assertEquals(0, NK.invoked);
        assertEquals(Status.SUCCESS, new Fallback(new Belief(OK), new Belief(NK)).tick());
        assertEquals(1+1, OK.invoked);
        assertEquals(0+0, NK.invoked);
        assertEquals(Status.SUCCESS, new Fallback(new Belief(NK), new Belief(OK)).tick());
        assertEquals(1+1+1, OK.invoked);
        assertEquals(0+0+1, NK.invoked);
        assertEquals(Status.FAILURE, new Fallback(new Belief(NK), new Belief(NK)).tick());
        assertEquals(1+1+1+0, OK.invoked);
        assertEquals(0+0+1+2, NK.invoked);
    }

    static class Verifiable implements Supplier<Verification> {

        private final Supplier<Verification> action;
        public int invoked = 0;

        public Verifiable(Supplier<Verification> action) {
            this.action = action;
        }
        @Override
        public Verification get() {
            invoked++;
            return action.get();
        }
    }
}
