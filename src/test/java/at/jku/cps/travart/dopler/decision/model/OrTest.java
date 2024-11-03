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
import at.jku.cps.travart.dopler.decision.model.impl.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrTest {

    private Or or;

    @BeforeEach
    public void init() {
        or = new Or(ICondition.TRUE, ICondition.TRUE);
    }

    @Test
    public void testToString() throws CircleInConditionException {
        assertEquals(or.getLeft() + " || " + or.getRight(), or.toString());
        or.setRight(ICondition.TRUE);
        or.setLeft(ICondition.TRUE);
        assertEquals(or.getLeft().toString() + " || " + or.getRight().toString(), or.toString());
    }

    @Test
    public void testEval() throws CircleInConditionException {
        // not possible anymore, because left and right are not allowed to be null
        //		ThrowingRunnable runner = () -> or.eval();
        //		Assert.assertThrows(NullPointerException.class, runner);
        or.setRight(ICondition.TRUE);
        or.setLeft(ICondition.TRUE);
        assertTrue(or.evaluate());
        or.setRight(ICondition.FALSE);
        or.setLeft(ICondition.FALSE);
        assertFalse(or.evaluate());
        or.setRight(ICondition.FALSE);
        or.setLeft(ICondition.TRUE);
        assertTrue(or.evaluate());
        or.setRight(ICondition.TRUE);
        or.setLeft(ICondition.FALSE);
        assertTrue(or.evaluate());
    }
}
