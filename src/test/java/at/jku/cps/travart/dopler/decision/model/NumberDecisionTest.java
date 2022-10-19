package at.jku.cps.travart.dopler.decision.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;

public class NumberDecisionTest {
	private NumberDecision nd;

	@Before
	public void init() {
		nd = new NumberDecision("test");
		nd.getRange().add(new DoubleValue(1.0));
		nd.getRange().add(new DoubleValue(1.2));
		nd.getRange().add(new DoubleValue(Double.POSITIVE_INFINITY));
	}

	@Test(expected = NullPointerException.class)
	public void testNumberDecisionString() {
		nd = new NumberDecision("test");
		nd = new NumberDecision(null);
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

	@Test(expected = RangeValueException.class)
	public void testEval_InvalidValue() throws RangeValueException {
		nd.setValue(Double.NaN);
	}

	@Test
	public void testGetValue() throws RangeValueException {
		assertTrue(0d == nd.getValue().getValue());
		nd.setValue(Double.POSITIVE_INFINITY);
		assertTrue(Double.POSITIVE_INFINITY == nd.getValue().getValue());
		nd.setValue(1.2);
		assertTrue(1.2d == nd.getValue().getValue());
	}

	@Test(expected = RangeValueException.class)
	public void testSetValue_setNull() throws RangeValueException {
		nd.setValue(null);
	}

	@Test(expected = RangeValueException.class)
	public void testSetValue_valueDisabled() throws RangeValueException {
		DoubleValue s = new DoubleValue(4.3);
		s.disable();
		nd.setValue(s.getValue());
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
		assertEquals("Lowest value is " + dvRange[0] + " lowest found is: " + nd.getMinRangeValue(), dvRange[0],
				nd.getMinRangeValue());
	}

	@Test
	public void getRangeValueStringMultipleValues() {
		DoubleValue[] dvRange = new DoubleValue[10];
		for (int i = 0; i < 10; i++) {
			dvRange[i] = new DoubleValue(i - 5);
		}
		nd.getRange().addAll(Arrays.asList(dvRange));
		assertEquals(dvRange[0] + " should equal " + nd.getRangeValue("-5"), dvRange[0], nd.getRangeValue("-5"));
	}

	@Test
	public void getRangeValueStringNoValues() {
		assertNull("Value -5 should not be found", nd.getRangeValue("-5"));
	}

	@Test
	public void testGetMaxRangeValueNoValues() {
		assertEquals("No values in range, so highest value should be 0.", new DoubleValue(0.0),
				new NumberDecision("nd").getMaxRangeValue());
	}

	@Test
	public void testGetMaxRangeValue() {
		DoubleValue[] dvRange = new DoubleValue[10];
		for (int i = 0; i < 10; i++) {
			dvRange[i] = new DoubleValue(i - 5);
		}
		nd.getRange().addAll(Arrays.asList(dvRange));
		assertEquals("Highest value is " + Double.POSITIVE_INFINITY + " highest found is: " + nd.getMaxRangeValue(),
				new DoubleValue(Double.POSITIVE_INFINITY), nd.getMaxRangeValue());
		nd.getRange().clear();
		nd.getRange().addAll(Arrays.asList(dvRange));
		assertEquals("Highest value is " + dvRange[9] + " highest found is: " + nd.getMaxRangeValue(), dvRange[9],
				nd.getMaxRangeValue());

	}
}
