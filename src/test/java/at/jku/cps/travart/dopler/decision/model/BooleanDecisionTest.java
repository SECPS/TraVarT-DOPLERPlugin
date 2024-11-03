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

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanDecisionTest {

    @Test
    public void getValueTest() throws RangeValueException {
        BooleanDecision b = new BooleanDecision("test");
        assertFalse(b.getValue().getValue());
        b.setValue(true);
        assertTrue(b.getValue().getValue());
        b.setValue(false);
        assertFalse(b.getValue().getValue());
    }

    @Test
    public void resetTest() throws RangeValueException {
        BooleanDecision b = new BooleanDecision("test");
        b.setValue(true);
        b.reset();
        assertFalse(b.getValue().getValue());
    }

    @Test
    public void evalTest() throws RangeValueException {
        BooleanDecision b = new BooleanDecision("test");
        b.setValue(true);
        assertTrue(b.getValue().evaluate());
        b.setValue(false);
        assertFalse(b.getValue().evaluate());
    }

    @Test
    public void testGetRangeValueBooleanTrue() {
        BooleanDecision bd = new BooleanDecision("test");
        assertEquals(BooleanValue.getTrue(), bd.getRangeValue(true));
    }

    @Test
    public void testGetRangeValueBooleanFalse() {
        BooleanDecision bd = new BooleanDecision("test");
        assertEquals(BooleanValue.getFalse(), bd.getRangeValue(false));
    }

    @Test
    public void testGetRangeValueStringNullString() {
        BooleanDecision bd = new BooleanDecision("test");
        assertEquals(BooleanValue.getFalse(), bd.getRangeValue("null"));
    }

    @Test
    public void testGetRangeValueStringTrueString() {
        BooleanDecision bd = new BooleanDecision("test");
        assertEquals(BooleanValue.getTrue(), bd.getRangeValue("true"));
    }

    @Test
    public void testGetRangeValueStringFalseString() {
        BooleanDecision bd = new BooleanDecision("test");
        assertEquals(BooleanValue.getFalse(), bd.getRangeValue("false"));
    }

    @Test
    public void testGetRange() {
        BooleanDecision bd = new BooleanDecision("test");

        Range<Boolean> r = new Range<>();
        r.add(BooleanValue.getFalse());
        r.add(BooleanValue.getTrue());
        assertEquals(r, bd.getRange());
    }

    @Test
    public void testContainsRangeValueTrue() {
        BooleanDecision bd = new BooleanDecision("test");
        assertTrue(bd.getRange().contains(BooleanValue.getTrue()), "Should contain true");
    }

    @Test
    public void testContainsRangeValueFalse() {
        BooleanDecision bd = new BooleanDecision("test");
        assertTrue(bd.getRange().contains(BooleanValue.getFalse()), "Should contain false");
    }

    @Test
    public void testContainsRangeValueNull() {
        BooleanDecision bd = new BooleanDecision("test");
        assertFalse(bd.getRange().contains(null), "Should contain false");
    }
}
