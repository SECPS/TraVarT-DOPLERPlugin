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
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringDecisionTest {

    private StringDecision sdec;

    @BeforeEach
    public void init() {
        sdec = new StringDecision("test");
    }

    @Test
    public void testStringDecisionString() {
        assertThrows(NullPointerException.class, () -> sdec = new StringDecision(null));
    }

    @Test
    public void testStringDecisionStringBoolean_NullId_true() {
        assertThrows(NullPointerException.class, () -> new StringDecision(null));
    }

    @Test
    public void testStringDecisionStringBoolean_NullId_false() {
        assertThrows(NullPointerException.class, () -> new StringDecision(null));
    }

    @Test
    public void testGetValue() throws RangeValueException {
        sdec.setValue("test");
        assertEquals("test", sdec.getValue().getValue());
    }

    @Test
    public void testSetValue_setNull() {
        assertThrows(RangeValueException.class, () -> sdec.setValue(null));
    }

    @Test
    public void testSetValue_setDisabledValue() {
        StringValue s = new StringValue("test");
        sdec.getRange().add(s);
        s.disable();
        assertThrows(RangeValueException.class, () -> sdec.setValue(s.getValue()));
    }

    @Test
    public void testSetValue_valid() throws RangeValueException {
        sdec.setValue("test");
        assertEquals("test", sdec.getValue().getValue());
    }

    @Test
    public void testGetRange() {
        assertNotNull(sdec.getRange());
    }

    @Test
    public void testSetRange() {
        // just don't fail
        sdec.setRange(new Range<String>());
    }

    @Test
    public void testGetRangeValue() {
        String s = "test";
        assertEquals(s, sdec.getRangeValue(s).toString());
    }

    @Test
    public void testReset() throws RangeValueException {
        sdec.setValue("test");
        assertEquals("test", sdec.getValue().toString());
        sdec.reset();
        assertEquals("", sdec.getValue().toString());
    }
}
