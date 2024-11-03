/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model;

import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotTest {

    private Not n;

    @BeforeEach
    public void init() {
        n = new Not(ICondition.TRUE);
    }

    @Test
    public void testNotICondition() throws CircleInConditionException {
        n = new Not(ICondition.TRUE);
        assertFalse(n.evaluate());
        n = new Not(ICondition.FALSE);
        assertTrue(n.evaluate());
    }

    @Test
    public void testToString() {
        // tests tostring for default not. DEPRECATED because empty NOT is now allowed
        // anymore
        //		assertEquals("!", n.toString());
        // tests toString for a not with a set value
        n = new Not(ICondition.TRUE);
        assertEquals("!true", n.toString());
    }

    @Test
    public void testEval() throws CircleInConditionException {
        // tests default evaluation of Not
        assertFalse(n.evaluate());
    }
}
