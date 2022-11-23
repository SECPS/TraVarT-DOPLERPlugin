package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

public class AllowFunctionTest {
	private AllowAction da;
	private EnumDecision dec;
	private ARangeValue<String> v;

	@BeforeEach
	public void prepareObject() {
		dec = new EnumDecision("test");
		Range<String> ra = new Range<>();
		v = new StringValue("testVal");
		ra.add(v);
		dec.setRange(ra);
		da = new AllowAction(dec, v);
	}

	@Test
	public void noNullConditionTest() {
		assertThrows(NullPointerException.class,()-> new AllowAction(null, BooleanValue.getTrue()));

	}

	@Test
	public void noNullValueTest() {
		assertThrows(NullPointerException.class,()->new AllowAction(new BooleanDecision("test"), null));
	}

	@Test
	public void DecisionValueRangeTest_AllowStringForBoolDec() throws ActionExecutionException {
		IAction allow = new AllowAction(new BooleanDecision("test"), new StringValue("test"));
		assertThrows(ActionExecutionException.class,()->allow.execute());
	}

	@Test
	public void DecisionValueRangeTest_AllowDoubleForBoolDec() throws ActionExecutionException {
		IAction allow = new AllowAction(new BooleanDecision("test"), new DoubleValue(1.2));
		assertThrows(ActionExecutionException.class,()->allow.execute());
	}

	@Test
	public void DecisionValueRangeTest_AllowDoubleForEnumDec() throws ActionExecutionException {
		IAction allow = new AllowAction(new EnumDecision("test"), new StringValue("test"));
		assertThrows(ActionExecutionException.class,()->allow.execute());
	}

	@Test
	public void DecisionValueRangeTest_AllowBoolForEnumDec() throws ActionExecutionException {
		IAction allow = new AllowAction(new EnumDecision("test"), BooleanValue.getFalse());
		assertThrows(ActionExecutionException.class,()->allow.execute());
	}

	@Test
	public void testExecute() throws ActionExecutionException, RangeValueException {
		v.disable();
		assertFalse(v.isEnabled());
		da.execute();
		assertTrue(v.isEnabled());
		dec.getRange().add(dec.getNoneOption());
		v = dec.getNoneOption();
		da = new AllowAction(dec, v);
		da.execute();
		dec.setVisibility(new IsTakenFunction(new BooleanDecision("test")));
		da = new AllowAction(dec, v);
		da.execute();
		assertFalse(dec.isVisible());
		BooleanDecision b = new BooleanDecision("test");
		b.setValue(true);
		dec.setVisibility(new IsTakenFunction(b));
		da = new AllowAction(dec, v);
		da.execute();
		assertTrue(dec.isVisible());
	}

	@Test
	public void testIsSatisfied_NotSatisfied() throws Exception {
		dec.setValue(v.getValue());
		v.disable();
		assertFalse(da.isSatisfied(),"Should not be satisfied upon disabling.");
	}

	@Test
	public void testIsSatisfied_Satisfied() throws Exception {
		dec.setValue(v.getValue());
		v.disable();
		da.execute();
		assertTrue(da.isSatisfied(),"Should be satisfied after executing the function.");
	}

	@Test
	public void testIsSatisfied_ValueNotPresent() throws Exception {
		da = new AllowAction(dec, new StringValue("test"));
		assertThrows(ActionExecutionException.class,()->da.isSatisfied());
		// should throw Exception because the new String is not part of the range.
	}

	@Test
	public void testEquals() {
		assertEquals(da.hashCode(), da.hashCode());
		AllowAction da2 = new AllowAction(dec, v);
		assertEquals(da.hashCode(), da2.hashCode());
		AllowAction da3 = new AllowAction(dec, new StringValue("testtest"));
		assertNotEquals(da.hashCode(), da3.hashCode());
		EnumDecision dec2 = new EnumDecision("diff");
		Range<String> ra = new Range<>();
		v = new StringValue("testVal");
		ra.add(v);
		dec2.setRange(ra);
		AllowAction da4 = new AllowAction(dec2, v);
		assertNotEquals(da.hashCode(), da4.hashCode());
		EnumDecision dec3 = new EnumDecision("diff");
		dec2.setRange(ra);
		AllowAction da5 = new AllowAction(dec3, v);
		assertNotEquals(da.hashCode(), da5.hashCode());
	}

	@Test
	public void testHashCode() {
		assertNotEquals(da,null);
		assertEquals(da,da);
		assertNotEquals(da,"a String");
		AllowAction da2 = new AllowAction(dec, v);
		assertEquals(da,da2);
		AllowAction da3 = new AllowAction(dec, new StringValue("testtest"));
		assertNotEquals(da,da3);
	}

	@Test
	public void testToString() {
		assertEquals("allow(" + dec.toString() + "." + v.toString() + ");", da.toString());
	}
}
