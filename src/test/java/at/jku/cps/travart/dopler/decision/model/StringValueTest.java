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

import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringValueTest {

    StringValue s;

    @BeforeEach
    public void setUp() throws Exception {
        s = new StringValue("test");
    }

    @Test
    public void testToString() {
        assertEquals("test", s.toString());
    }

    @Test
    public void testLessThanActuallyLess() {
        assertTrue(s.lessThan(new StringValue("zest")), "test should be lower than zest.");
    }

    @Test
    public void testLessThanActuallyEqual() {
        assertFalse(s.lessThan(new StringValue("test")), "test should not be lower than test.");
    }

    @Test
    public void testLessThanActuallyGreater() {
        assertFalse(s.lessThan(new StringValue("aest")), "test should not be lower than aest.");
    }

    @Test
    public void testGreaterThanActuallyLess() {
        assertFalse(s.greaterThan(new StringValue("zest")), "test should be not greater than zest.");
    }

    @Test
    public void testGreaterThanActuallyEqual() {
        assertFalse(s.greaterThan(new StringValue("test")), "test should not be greater than test.");
    }

    @Test
    public void testGreaterThanActuallyGreater() {
        assertTrue(s.greaterThan(new StringValue("aest")), "test should be greater than aest.");
    }

    @Test
    public void testevaluateisBlank() {
        s = new StringValue("");
        assertFalse(s.evaluate(), "Blank value should return false.");
    }

    @Test
    public void testevaluateisNotBlank() {
        assertTrue(s.evaluate(), "Non-Blank value should return true.");
    }
}
