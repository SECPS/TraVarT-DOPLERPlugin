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

public class LessEqualsTest {

    private LessEquals le;

    @BeforeEach
    public void setUp() throws Exception {
        le = new LessEquals(ICondition.FALSE, ICondition.TRUE);
    }

    @Test
    public void testLessEqualsGetValueFunctionAndARangeValue() throws RangeValueException {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        ed.setValue(s.getValue());
        GetValueFunction gvf = new GetValueFunction(ed);
        le = new LessEquals(gvf, s);
        assertTrue(le.evaluate(), "StringValue s is same as StringValue s");
    }

    @Test
    public void testLessEqualsGetValueFunctionAndGetValueFunctionStrings() {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        GetValueFunction gvf1 = new GetValueFunction(ed);
        GetValueFunction gvf2 = new GetValueFunction(ed);
        le = new LessEquals(gvf1, gvf2);
        assertTrue(le.evaluate(), "Both getValueFunction should return the same value.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsGetValueFunctionAndARangeValue1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(gvf1, d2);
        assertTrue(le.evaluate(), "Numberdecision has a lower value than value2 therefore should be true.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsGetValueFunctionAndARangeValue2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(gvf1, d2);
        assertFalse(le.evaluate(), "Numberdecision has a higher value than value2 therefore should be false.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsGetValueFunctionAndARangeValue3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(gvf1, d2);
        assertTrue(le.evaluate(), "Numberdecision is equal to value2 therefore should be true.");
    }

    @Test
    public void testLessEqualsARangeValueAndGetValueFunction1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(d2, gvf1);
        assertFalse(le.evaluate(), d2.toString() + " should not be <= " + nd1.getValue());
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsARangeValueAndGetValueFunction2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(d2, gvf1);
        assertTrue(le.evaluate(), d2.toString() + " should be <= " + nd1.getValue());
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsARangeValueAndGetValueFunction3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        le = new LessEquals(d2, gvf1);
        assertTrue(le.evaluate(), d2 + " <= " + d1 + " should be true");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsGetValueFunctionAndGetValueFunctionNumbers1() throws RangeValueException {
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
        le = new LessEquals(gvf1, gvf2);
        assertTrue(le.evaluate(), gvf1 + " <= " + gvf2 + " should be true");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessEqualsGetValueFunctionAndGetValueFunctionNumbers2() throws RangeValueException {
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
        le = new LessEquals(gvf2, gvf1);
        assertFalse(le.evaluate(), gvf2 + " <= " + gvf1 + " should be false");
    }

    @Test
    public void testEvaluateFalseTrue() {
        assertFalse(le.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateTrueFalse() {
        le = new LessEquals(ICondition.TRUE, ICondition.FALSE);
        assertFalse(le.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateFalseFalse() {
        le = new LessEquals(ICondition.FALSE, ICondition.FALSE);
        assertFalse(le.evaluate(), "Should return false, because two false are the are not comparables!");
    }

    @Test
    public void testEvaluateTrueTrue() {
        le = new LessEquals(ICondition.TRUE, ICondition.TRUE);
        assertTrue(le.evaluate(), "Should return true, because two true are the same!");
    }

    @Test
    public void testLessEqualsNullFalse() {
        assertThrows(NullPointerException.class, () -> le = new LessEquals(null, ICondition.FALSE));
    }

    @Test
    public void testLessEqualsFalseNull() {
        assertThrows(NullPointerException.class, () -> le = new LessEquals(ICondition.FALSE, null));
    }

    @Test
    public void testToStringFalseTrue() {
        assertEquals(ICondition.FALSE + " <= " + ICondition.TRUE, le.toString());
    }
}
