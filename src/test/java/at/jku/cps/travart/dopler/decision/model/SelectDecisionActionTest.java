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

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SelectDecisionActionTest {

    private SelectDecisionAction s;
    private AbstractDecision<Boolean> d;

    @BeforeEach
    public void setUp() throws Exception {
        d = new BooleanDecision("test");
        d.setValue(false);
        s = new SelectDecisionAction(d);
    }

    @Test
    public void testExecute() throws ActionExecutionException {
        s.execute();
        assertTrue(d.getValue().getValue());
        s.execute();
        assertTrue(d.getValue().getValue());
    }

    @Test
    public void testIsSatisfied() throws ActionExecutionException {
        assertFalse(s.isSatisfied());
        s.execute();
        assertTrue(s.isSatisfied());
        s = new SelectDecisionAction(d);
        assertTrue(s.isSatisfied());
        s.execute();
        assertTrue(s.isSatisfied());
    }

    @Test
    public void testSelectDecisionAction() {
        assertThrows(NullPointerException.class, () -> new SelectDecisionAction(null));
    }

    @Test
    public void testEquals() {
        assertNotEquals(s, null);
        assertNotEquals(s, "a String");
        assertEquals(s, s);
        SelectDecisionAction s2 = new SelectDecisionAction(d);
        assertEquals(s, s2);
        AbstractDecision<?> d2 = new StringDecision("test2");
        SelectDecisionAction s3 = new SelectDecisionAction(d2);
        assertNotEquals(s, s3);
    }
}
