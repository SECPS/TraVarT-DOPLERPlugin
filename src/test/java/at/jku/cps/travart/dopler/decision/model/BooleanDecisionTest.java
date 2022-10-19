package at.jku.cps.travart.dopler.decision.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.Range;

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
		assertTrue("Should contain true", bd.getRange().contains(BooleanValue.getTrue()));
	}

	@Test
	public void testContainsRangeValueFalse() {
		BooleanDecision bd = new BooleanDecision("test");
		assertTrue("Should contain false", bd.getRange().contains(BooleanValue.getFalse()));
	}

	@Test
	public void testContainsRangeValueNull() {
		BooleanDecision bd = new BooleanDecision("test");
		assertFalse("Should contain false", bd.getRange().contains(null));
	}
}
