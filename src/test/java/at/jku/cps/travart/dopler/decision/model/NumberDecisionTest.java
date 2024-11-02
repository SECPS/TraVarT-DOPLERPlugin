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
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class NumberDecisionTest {
    private NumberDecision nd;

    @BeforeEach
    public void init() {
        nd = new NumberDecision("test");
        nd.getRange().add(new DoubleValue(1.0));
        nd.getRange().add(new DoubleValue(1.2));
        nd.getRange().add(new DoubleValue(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNumberDecisionString() {
        nd = new NumberDecision("test");
        assertThrows(NullPointerException.class, () -> new NumberDecision(null));
    }

    @Test
    public void testEval_validDefault() throws RangeValueException {
        assertTrue(nd.getValue().evaluate());
    }

    @Test
    public void testEval_invalidInfinity() throws RangeValueException {
        nd.setValue(Double.POSITIVE_INFINITY);
        assertFalse(nd.getValue().evaluate());
    }

    @Test
    public void testEval_validOne() throws RangeValueException {
        nd.setValue(1.0);
        assertTrue(nd.getValue().evaluate());
    }

    @Test
    public void testEval_InvalidValue() {
        assertThrows(RangeValueException.class, () -> nd.setValue(Double.NaN));
    }

    @Test
    public void testGetValue() throws RangeValueException {
        assertTrue(0d == nd.getValue().getValue());
        nd.setValue(Double.POSITIVE_INFINITY);
        assertTrue(Double.POSITIVE_INFINITY == nd.getValue().getValue());
        nd.setValue(1.2);
        assertTrue(1.2d == nd.getValue().getValue());
    }

    @Test
    public void testSetValue_setNull() throws RangeValueException {
        assertThrows(RangeValueException.class, () -> nd.setValue(null));
    }

    @Test
    public void testSetValue_valueDisabled() {
        DoubleValue s = new DoubleValue(4.3);
        s.disable();
        assertThrows(RangeValueException.class, () -> nd.setValue(s.getValue()));
    }

    @Test
    public void testSetValue() throws RangeValueException {
        nd.setValue(Double.POSITIVE_INFINITY);
        assertTrue(Double.POSITIVE_INFINITY == nd.getValue().getValue());
        nd.setValue(1.2);
        assertTrue(1.2d == nd.getValue().getValue());
    }

    @Test
    public void testGetRange() {
        assertNotNull(nd.getRange());
    }

    @Test
    public void testSetRange() {
        // No need for this. Should this maybe be changed?
        // Maybe there is a need, to set a certain range for number decisions?
        nd.setRange(new Range<Double>());
    }

    @Test
    public void testGetRangeValue() {
        DoubleValue d = new DoubleValue(12);
        nd.getRange().add(d);
        assertEquals(d, nd.getRangeValue(d.getValue()));
    }

    @Test
    public void testReset() throws RangeValueException {
        nd.setValue(1.0);
        assertTrue(1.0 == nd.getValue().getValue());
        nd.reset();
        assertTrue(nd.getValue().getValue().equals(1.0));
    }

    @Test
    public void testGetMinRangeValue() {
        DoubleValue[] dvRange = new DoubleValue[10];
        for (int i = 0; i < 10; i++) {
            dvRange[i] = new DoubleValue(i - 5);
        }
        nd.getRange().addAll(Arrays.asList(dvRange));
        assertEquals(dvRange[0], nd.getMinRangeValue(),
                "Lowest value is " + dvRange[0] + " lowest found is: " + nd.getMinRangeValue());
    }

    @Test
    public void getRangeValueStringMultipleValues() {
        DoubleValue[] dvRange = new DoubleValue[10];
        for (int i = 0; i < 10; i++) {
            dvRange[i] = new DoubleValue(i - 5);
        }
        nd.getRange().addAll(Arrays.asList(dvRange));
        assertEquals(dvRange[0], nd.getRangeValue("-5"), dvRange[0] + " should equal " + nd.getRangeValue("-5"));
    }

    @Test
    public void getRangeValueStringNoValues() {
        assertNull(nd.getRangeValue("-5"), "Value -5 should not be found");
    }

    @Test
    public void testGetMaxRangeValueNoValues() {
        assertEquals(new DoubleValue(0.0), new NumberDecision("nd").getMaxRangeValue(),
                "No values in range, so highest value should be 0.");
    }

    @Test
    public void testGetMaxRangeValue() {
        DoubleValue[] dvRange = new DoubleValue[10];
        for (int i = 0; i < 10; i++) {
            dvRange[i] = new DoubleValue(i - 5);
        }
        nd.getRange().addAll(Arrays.asList(dvRange));
        assertEquals(new DoubleValue(Double.POSITIVE_INFINITY), nd.getMaxRangeValue(),
                "Highest value is " + Double.POSITIVE_INFINITY + " highest found is: " + nd.getMaxRangeValue());
        nd.getRange().clear();
        nd.getRange().addAll(Arrays.asList(dvRange));
        assertEquals(dvRange[9], nd.getMaxRangeValue(),
                "Highest value is " + dvRange[9] + " highest found is: " + nd.getMaxRangeValue());

    }
}
