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
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DisAllowFunctionTest {

    private DisAllowAction da;
    private EnumerationDecision dec;
    private AbstractRangeValue<String> v;

    // sets up an EnumDecision "test" with a Range only containing a Value
    // testVal. The DisallowFunction then disallows the testVal value.
    @BeforeEach
    public void prepareObject() {
        dec = new EnumerationDecision("test");
        Range<String> ra = new Range<>();
        v = new StringValue("testVal");
        ra.add(v);
        dec.setRange(ra);
        da = new DisAllowAction(dec, v);
    }

    @Test
    public void noNullConditionTest() {
        assertThrows(NullPointerException.class, () -> new DisAllowAction(null, BooleanValue.getTrue()));
    }

    @Test
    public void noNullValueTest() {
        assertThrows(IllegalArgumentException.class, () -> new DisAllowAction(new BooleanDecision("test"), null));
    }

    @Test
    public void DecisionValueRangeTest_AllowStringForBoolDec() throws ActionExecutionException {
        assertThrows(IllegalArgumentException.class,
                () -> new DisAllowAction(new BooleanDecision("test"), new StringValue("test")));
    }

    @Test
    public void DecisionValueRangeTest_AllowDoubleForBoolDec() throws ActionExecutionException {
        assertThrows(IllegalArgumentException.class,
                () -> new DisAllowAction(new BooleanDecision("test"), new DoubleValue(1.2)));
    }

    @Test
    public void DecisionValueRangeTest_AllowDoubleForEnumDec() throws ActionExecutionException {
        assertThrows(IllegalArgumentException.class,
                () -> new DisAllowAction(new EnumerationDecision("test"), new StringValue("test")));
    }

    @Test
    public void DecisionValueRangeTest_AllowBoolForEnumDec() throws ActionExecutionException {
        assertThrows(IllegalArgumentException.class,
                () -> new DisAllowAction(new EnumerationDecision("test"), BooleanValue.getFalse()));
    }

    @Test
    public void testExecute() throws RangeValueException, ActionExecutionException {
        v.enable();
        assertTrue(v.isEnabled());
        da.execute();
        assertFalse(v.isEnabled());
        dec.getRange().add(dec.getNoneOption());
        v = dec.getNoneOption();
        da = new DisAllowAction(dec, v);
        da.execute();
        dec.setVisibility(new IsTakenFunction(new BooleanDecision("test")));
        da = new DisAllowAction(dec, v);
        da.execute();
        assertFalse(dec.isVisible());
        BooleanDecision b = new BooleanDecision("test");
        b.setValue(true);
        dec.setVisibility(new IsTakenFunction(b));
        da = new DisAllowAction(dec, v);
        da.execute();
        assertTrue(dec.isVisible());
        // try to disallow a value that is not in range
        assertThrows(IllegalArgumentException.class, () -> {
            da = new DisAllowAction(dec, new StringValue("test"));
            da.execute();
        });
    }

    @Test
    public void testIsSatisfied_NotSatisfied() throws Exception {
        dec.setValue(v.getValue());
        assertFalse(da.isSatisfied(), "Should not be satisfied upon disabling.");
    }

    @Test
    public void testIsSatisfied_Satisfied() throws Exception {
        dec.setValue(v.getValue());
        da.execute();
        assertTrue(da.isSatisfied(), "Should be satisfied after executing the function.");
    }

    @Test
    public void testIsSatisfied_ValueNotPresent() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new DisAllowAction(dec, new StringValue("test")));
        // should throw Exception because the new String is not part of the range.
    }

    @Test
    public void testEquals() {
        assertEquals(da.hashCode(), da.hashCode());
        DisAllowAction da2 = new DisAllowAction(dec, v);
        assertEquals(da.hashCode(), da2.hashCode());
        StringValue s = new StringValue("testtest");
        dec.getRange().add(s);
        DisAllowAction da3 = new DisAllowAction(dec, s);
        assertNotEquals(da.hashCode(), da3.hashCode());
        EnumerationDecision dec2 = new EnumerationDecision("diff");
        Range<String> ra = new Range<>();
        v = new StringValue("testVal");
        ra.add(v);
        dec2.setRange(ra);
        DisAllowAction da4 = new DisAllowAction(dec2, v);
        assertNotEquals(da.hashCode(), da4.hashCode());
    }

    @Test
    public void testHashCode() {
        assertNotEquals(da, null);
        assertEquals(da, da);
        assertNotEquals(da, "a String");
        DisAllowAction da2 = new DisAllowAction(dec, v);
        assertEquals(da, da2);
        assertThrows(IllegalArgumentException.class, () -> new DisAllowAction(dec, new StringValue("testtest")));
        //		assertNotEquals(da, da3);
    }

    @Test
    public void testToString() {
        assertEquals("disAllow(" + dec.toString() + "." + v.toString() + ");", da.toString());
    }
}
