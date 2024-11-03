/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model.impl;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EqualsTest {

    private Equals e;

    @BeforeEach
    public void setUp() throws Exception {
        e = new Equals(ICondition.FALSE, ICondition.TRUE);
    }

    @Test
    public void testEqualsGetValueFunctionAndARangeValue() throws RangeValueException {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        ed.setValue(s.getValue());
        GetValueFunction gvf = new GetValueFunction(ed);
        e = new Equals(gvf, s);
        assertTrue(e.evaluate(), "StringValue s is same as StringValue s");
    }

    @Test
    public void testEqualsGetValueFunctionAndGetValueFunctionStrings() {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        GetValueFunction gvf1 = new GetValueFunction(ed);
        GetValueFunction gvf2 = new GetValueFunction(ed);
        e = new Equals(gvf1, gvf2);
        assertTrue(e.evaluate(), "Both getValueFunction should return the same value.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsGetValueFunctionAndARangeValue1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(gvf1, d2);
        assertFalse(e.evaluate(), gvf1.getDecision().getValue() + " == " + d2 + " should be false");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsGetValueFunctionAndARangeValue2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(gvf1, d2);
        assertFalse(e.evaluate(), gvf1.getDecision().getValue() + " == " + d2 + " should be false");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsGetValueFunctionAndARangeValue3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(gvf1, d2);
        assertTrue(e.evaluate(), gvf1.getDecision().getValue() + " == " + d2 + " should be true");
    }

    @Test
    public void testEqualsARangeValueAndGetValueFunction1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(d2, gvf1);
        assertFalse(e.evaluate(), d2 + " == " + gvf1.getDecision().getValue() + " should be false");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsARangeValueAndGetValueFunction2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(d2, gvf1);
        assertFalse(e.evaluate(), d2 + " == " + gvf1.getDecision().getValue() + " should be false");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsARangeValueAndGetValueFunction3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        e = new Equals(d2, gvf1);
        assertTrue(e.evaluate(), d2 + " == " + gvf1.getDecision().getValue() + " should be true");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsGetValueFunctionAndGetValueFunctionNumbers1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");
        NumberDecision nd2 = new NumberDecision("testDecision2");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd2.getRange().add(d2);
        nd1.setValue(d1.getValue());
        nd2.setValue(d2.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        GetValueFunction gvf2 = new GetValueFunction(nd2);
        e = new Equals(gvf1, gvf2);
        assertFalse(e.evaluate(),
                gvf1.getDecision().getValue() + " == " + gvf2.getDecision().getValue() + " should be false");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testEqualsGetValueFunctionAndGetValueFunctionNumbers2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");
        NumberDecision nd2 = new NumberDecision("testDecision2");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd2.getRange().add(d2);
        nd1.setValue(d1.getValue());
        nd2.setValue(d2.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        GetValueFunction gvf2 = new GetValueFunction(nd2);
        e = new Equals(gvf2, gvf1);
        assertFalse(e.evaluate(),
                gvf2.getDecision().getValue() + " == " + gvf1.getDecision().getValue() + " should be false");
    }

    @Test
    public void testEvaluateFalseTrue() {
        assertFalse(e.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateTrueFalse() {
        e = new Equals(ICondition.TRUE, ICondition.FALSE);
        assertFalse(e.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateFalseFalse() {
        e = new Equals(ICondition.FALSE, ICondition.FALSE);
        assertFalse(e.evaluate(), "Should return true, because two false are not comparables!");
    }

    @Test
    public void testEvaluateTrueTrue() {
        e = new Equals(ICondition.TRUE, ICondition.TRUE);
        assertTrue(e.evaluate(), "Should return true, because two true are the same!");
    }

    @Test
    public void testEqualsNullFalse() {
        assertThrows(NullPointerException.class, () -> new Equals(null, ICondition.FALSE));
    }

    @Test
    public void testEqualsFalseNull() {
        assertThrows(NullPointerException.class, () -> new Equals(ICondition.FALSE, null));
    }

    @Test
    public void testToStringFalseTrue() {
        assertEquals(ICondition.FALSE + " == " + ICondition.TRUE, e.toString());
    }
}
