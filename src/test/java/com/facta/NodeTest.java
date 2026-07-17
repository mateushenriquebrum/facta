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
        assertEquals(OK.invoked, 2);
        assertEquals(NK.invoked, 0);

        assertEquals(Status.FAILURE, new Sequence(new Belief(OK), new Belief(NK)).tick());
        assertEquals(OK.invoked, 2+1);
        assertEquals(NK.invoked, 0+1);


        assertEquals(Status.FAILURE, new Sequence(new Belief(NK), new Belief(OK)).tick());
        assertEquals(OK.invoked, 2+1+0);
        assertEquals(NK.invoked, 0+1+1);

        assertEquals(Status.FAILURE, new Sequence(new Belief(NK), new Belief(NK)).tick());
        assertEquals(OK.invoked, 2+1+0+0);
        assertEquals(NK.invoked, 0+1+1+1);
    }
    @Test
    public void shouldShortCircuitLikeLogicalOr() {
        assertEquals(Status.SUCCESS, new Fallback(new Belief(OK), new Belief(OK)).tick());
        assertEquals(OK.invoked, 1);
        assertEquals(NK.invoked, 0);
        assertEquals(Status.SUCCESS, new Fallback(new Belief(OK), new Belief(NK)).tick());
        assertEquals(OK.invoked, 1+1);
        assertEquals(NK.invoked, 0+0);
        assertEquals(Status.SUCCESS, new Fallback(new Belief(NK), new Belief(OK)).tick());
        assertEquals(OK.invoked, 1+1+1);
        assertEquals(NK.invoked, 0+0+1);
        assertEquals(Status.FAILURE, new Fallback(new Belief(NK), new Belief(NK)).tick());
        assertEquals(OK.invoked, 1+1+1+0);
        assertEquals(NK.invoked, 0+0+1+2);
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
