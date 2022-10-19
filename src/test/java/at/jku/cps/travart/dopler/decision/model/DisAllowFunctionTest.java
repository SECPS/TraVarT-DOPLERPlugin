package at.jku.cps.travart.dopler.decision.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

public class DisAllowFunctionTest {
	private DisAllowAction da;
	private EnumDecision dec;
	private ARangeValue<String> v;

	// sets up an EnumDecision "test" with a Range only containing a Value
	// testVal. The DisallowFunction then disallows the testVal value.
	@Before
	public void prepareObject() {
		dec = new EnumDecision("test");
		Range<String> ra = new Range<>();
		v = new StringValue("testVal");
		ra.add(v);
		dec.setRange(ra);
		da = new DisAllowAction(dec, v);
	}

	@Test(expected = NullPointerException.class)
	public void noNullConditionTest() {
		new DisAllowAction(null, BooleanValue.getTrue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void noNullValueTest() {
		new DisAllowAction(new BooleanDecision("test"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void DecisionValueRangeTest_AllowStringForBoolDec() throws ActionExecutionException {
		IAction allow = new DisAllowAction(new BooleanDecision("test"), new StringValue("test"));
		allow.execute();
	}

	@Test(expected = IllegalArgumentException.class)
	public void DecisionValueRangeTest_AllowDoubleForBoolDec() throws ActionExecutionException {
		IAction allow = new DisAllowAction(new BooleanDecision("test"), new DoubleValue(1.2));
		allow.execute();
	}

	@Test(expected = IllegalArgumentException.class)
	public void DecisionValueRangeTest_AllowDoubleForEnumDec() throws ActionExecutionException {
		IAction allow = new DisAllowAction(new EnumDecision("test"), new StringValue("test"));
		allow.execute();
	}

	@Test(expected = IllegalArgumentException.class)
	public void DecisionValueRangeTest_AllowBoolForEnumDec() throws ActionExecutionException {
		IAction allow = new DisAllowAction(new EnumDecision("test"), BooleanValue.getFalse());
		allow.execute();
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
		ThrowingRunnable run = () -> {
			da = new DisAllowAction(dec, new StringValue("test"));
			da.execute();
		};
		assertThrows(IllegalArgumentException.class, run);

	}

	@Test
	public void testIsSatisfied_NotSatisfied() throws Exception {
		dec.setValue(v.getValue());
		assertFalse("Should not be satisfied upon disabling.", da.isSatisfied());
	}

	@Test
	public void testIsSatisfied_Satisfied() throws Exception {
		dec.setValue(v.getValue());
		da.execute();
		assertTrue("Should be satisfied after executing the function.", da.isSatisfied());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsSatisfied_ValueNotPresent() throws Exception {
		da = new DisAllowAction(dec, new StringValue("test"));
		da.isSatisfied();
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
		EnumDecision dec2 = new EnumDecision("diff");
		Range<String> ra = new Range<>();
		v = new StringValue("testVal");
		ra.add(v);
		dec2.setRange(ra);
		DisAllowAction da4 = new DisAllowAction(dec2, v);
		assertNotEquals(da.hashCode(), da4.hashCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHashCode() {
		assertNotEquals(da,null);
		assertEquals(da,da);
		assertNotEquals(da,"a String");
		DisAllowAction da2 = new DisAllowAction(dec, v);
		assertEquals(da,da2);
		DisAllowAction da3 = new DisAllowAction(dec, new StringValue("testtest"));
		assertNotEquals(da,da3);
	}

	@Test
	public void testToString() {
		assertEquals("disAllow(" + dec.toString() + "." + v.toString() + ");", da.toString());
	}
}
