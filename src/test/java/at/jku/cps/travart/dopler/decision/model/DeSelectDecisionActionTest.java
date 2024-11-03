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
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeSelectDecisionActionTest {
    private DeSelectDecisionAction des;
    private BooleanDecision b;
    private final String testString = "test";

    @BeforeEach
    public void init() {
        b = new BooleanDecision(testString);
        des = new DeSelectDecisionAction(b);
    }

    @Test
    public void testHashCode() {
        DeSelectDecisionAction des2 = new DeSelectDecisionAction(b);
        assertEquals(des2.hashCode(), des.hashCode());
    }

    @Test
    public void testDeSelectDecisionAction() {
        assertThrows(NullPointerException.class, () -> new DeSelectDecisionAction(null));
    }

    @Test
    public void testExecute() throws ActionExecutionException, RangeValueException {
        b.setValue(true);
        assertTrue(b.getValue().getValue());
        des.execute();
        assertFalse(b.getValue().getValue());
    }

    @Test
    public void testIsSatisfied() throws ActionExecutionException, RangeValueException {
        assertTrue(des.isSatisfied());
        b.setValue(true);
        assertFalse(des.isSatisfied());
        des.execute();
        assertTrue(des.isSatisfied());
    }

    @Test
    public void testGetVariable() {
        assertEquals(b, des.getVariable());
        assertNotEquals(new BooleanDecision("different"), des.getVariable());
    }

    @Test
    public void testGetValue() {
        assertEquals(BooleanValue.getFalse(), des.getValue());
    }

    @Test
    public void testEqualsObject() {
        assertNotEquals(des, "a String");
        assertNotEquals(des, null);
        assertEquals(des, des);
        DeSelectDecisionAction des2 = new DeSelectDecisionAction(b);
        assertEquals(des, des2);
    }

    @Test
    public void testToString() {
        assertEquals(testString + " = false;", des.toString());
    }

}
