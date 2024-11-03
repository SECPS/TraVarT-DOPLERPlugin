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

public class LessTest {

    private Less l;

    @BeforeEach
    public void setUp() throws Exception {
        l = new Less(ICondition.FALSE, ICondition.TRUE);
    }

    @Test
    public void testLessGetValueFunctionAndARangeValue() throws RangeValueException {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        ed.setValue(s.getValue());
        GetValueFunction gvf = new GetValueFunction(ed);
        l = new Less(gvf, s);
        assertFalse(l.evaluate(), gvf.getDecision().getValue() + " < " + s + " should be false");
    }

    @Test
    public void testLessGetValueFunctionAndGetValueFunctionStrings() {
        EnumerationDecision ed = new EnumerationDecision("testDecision");
        StringValue s = new StringValue("TestValue");
        ed.getRange().add(s);
        GetValueFunction gvf1 = new GetValueFunction(ed);
        GetValueFunction gvf2 = new GetValueFunction(ed);
        l = new Less(gvf1, gvf2);
        assertFalse(l.evaluate(),
                gvf1.getDecision().getValue() + " < " + gvf2.getDecision().getValue() + " should be false");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessGetValueFunctionAndARangeValue1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(gvf1, d2);
        assertTrue(l.evaluate(), "Numberdecision has a lower value than value2 therefore should be true.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessGetValueFunctionAndARangeValue2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(gvf1, d2);
        assertFalse(l.evaluate(), "Numberdecision has a higher value than value2 therefore should be false.");
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessGetValueFunctionAndARangeValue3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(gvf1, d2);
        assertFalse(l.evaluate(), "Numberdecision is equal to value2 therefore should be false.");
    }

    @Test
    public void testLessARangeValueAndGetValueFunction1() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(3);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(d2, gvf1);
        assertFalse(l.evaluate(), d2.toString() + " should not be < " + nd1.getValue());
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessARangeValueAndGetValueFunction2() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(3);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(d2, gvf1);
        assertTrue(l.evaluate(), d2.toString() + " should be < " + nd1.getValue());
    }

    /**
     * Checks a getvaluefunction against a concrete value.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessARangeValueAndGetValueFunction3() throws RangeValueException {
        NumberDecision nd1 = new NumberDecision("testDecision1");

        DoubleValue d1 = new DoubleValue(4);
        DoubleValue d2 = new DoubleValue(4);
        nd1.getRange().add(d1);
        nd1.setValue(d1.getValue());
        GetValueFunction gvf1 = new GetValueFunction(nd1);
        l = new Less(d2, gvf1);
        assertFalse(l.evaluate(), d2 + " < " + d1 + " should be false");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessGetValueFunctionAndGetValueFunctionNumbers1() throws RangeValueException {
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
        l = new Less(gvf1, gvf2);
        assertTrue(l.evaluate(), gvf1 + " < " + gvf2 + " should be true");
    }

    /**
     * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the value comparison is correct.
     *
     * @throws RangeValueException
     */
    @Test
    public void testLessGetValueFunctionAndGetValueFunctionNumbers2() throws RangeValueException {
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
        l = new Less(gvf2, gvf1);
        assertFalse(l.evaluate(), gvf2 + " < " + gvf1 + " should be false");
    }

    @Test
    public void testEvaluateFalseTrue() {
        assertFalse(l.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateTrueFalse() {
        l = new Less(ICondition.TRUE, ICondition.FALSE);
        assertFalse(l.evaluate(), "Returns false because there is no hierarchy between true and false");
    }

    @Test
    public void testEvaluateFalseFalse() {
        l = new Less(ICondition.FALSE, ICondition.FALSE);
        assertFalse(l.evaluate(), "Should return false, because two false are the same!");
    }

    @Test
    public void testEvaluateTrueTrue() {
        l = new Less(ICondition.TRUE, ICondition.TRUE);
        assertFalse(l.evaluate(), "Should return false, because two true are the same!");
    }

    @Test
    public void testLessNullFalse() {
        assertThrows(NullPointerException.class, () -> l = new Less(null, ICondition.FALSE));
    }

    @Test
    public void testLessFalseNull() {
        assertThrows(NullPointerException.class, () -> l = new Less(ICondition.FALSE, null));
    }

    @Test
    public void testToStringFalseTrue() {
        assertEquals(ICondition.FALSE + " < " + ICondition.TRUE, l.toString());
    }
}
