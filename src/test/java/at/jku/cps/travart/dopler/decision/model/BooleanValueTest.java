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

import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanValueTest {
    private String noHierarchyError = "No hierarchy in boolean values, therefore false";

    @Test
    public void equalsTest() {
        BooleanValue b1 = BooleanValue.getTrue();
        BooleanValue b2 = BooleanValue.getTrue();
        BooleanValue b3 = BooleanValue.getFalse();
        BooleanValue b4 = BooleanValue.getFalse();
        assertEquals(b1, b2);
        assertEquals(b1, b1);
        assertEquals(b3, b4);
        assertNotEquals(b1, "a string");
        assertNotEquals(b1, new DoubleValue(1.2));
        assertNotEquals(b1, b4);
        assertNotEquals(b3, b2);
    }

    @Test
    public void testSetValue() {
        BooleanValue b1 = BooleanValue.getTrue();
        assertThrows(UnsupportedOperationException.class, () -> b1.setValue(true));
    }

    @Test
    public void testSetValueNull() {
        BooleanValue b1 = BooleanValue.getTrue();
        assertThrows(UnsupportedOperationException.class, () -> b1.setValue(null));
    }

    @Test
    public void testSetEnabled() {
        BooleanValue b1 = BooleanValue.getTrue();
        b1.enable();
        assertTrue(b1.isEnabled());
        b1.disable();
        assertFalse(b1.isEnabled());
    }

    @Test
    public void testLessThan() {
        BooleanValue tr = BooleanValue.getTrue();
        BooleanValue fa = BooleanValue.getFalse();
        assertFalse(tr.lessThan(tr), noHierarchyError);
        assertFalse(tr.lessThan(fa), noHierarchyError);
        assertFalse(fa.lessThan(tr), noHierarchyError);
        assertFalse(fa.lessThan(fa), noHierarchyError);
    }

    @Test
    public void testGreaterThan() {
        BooleanValue tr = BooleanValue.getTrue();
        BooleanValue fa = BooleanValue.getFalse();
        assertFalse(tr.greaterThan(tr), noHierarchyError);
        assertFalse(tr.greaterThan(fa), noHierarchyError);
        assertFalse(fa.greaterThan(tr), noHierarchyError);
        assertFalse(fa.greaterThan(fa), noHierarchyError);
    }

    @Test
    public void testEvaluate() {
        BooleanValue tr = BooleanValue.getTrue();
        BooleanValue fa = BooleanValue.getFalse();
        assertTrue(tr.evaluate(), "True should return true");
        assertFalse(fa.evaluate(), "False should return false");
    }

}
