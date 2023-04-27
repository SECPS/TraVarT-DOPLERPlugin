package at.jku.cps.travart.dopler.decision.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueNotEnabledException;

public class DisAllowActionTest {
	private DisAllowAction daa;
	private EnumerationDecision ed;
	private StringValue s;

	@BeforeEach
	public void setUp() throws Exception {
		ed = new EnumerationDecision("test");
		s = new StringValue("testValue");
		ed.getRange().add(s);
		daa = new DisAllowAction(ed, s);
	}

	@Test
	public void testDisAllowActionNullARangeValue() {
		assertThrows(NullPointerException.class, ()-> new DisAllowAction(null, s));
	}

	@Test
	public void testDisAllowActionIDecisionNull() {
		assertThrows(IllegalArgumentException.class, ()-> new DisAllowAction(ed, null));
	}

	@Test
	public void testDisAllowActionNotInRange() throws ActionExecutionException {
		assertThrows(IllegalArgumentException.class, ()-> new DisAllowAction(ed, new StringValue("someNotContainedValue")));
	}

	@Test
	public void testExecuteNotInRange(){
		StringValue s1 = new StringValue("someNotContainedValue");
		ed.getRange().add(s1);
		daa = new DisAllowAction(ed, s1);
		ed.getRange().remove(s1);
		assertThrows(ActionExecutionException.class, ()->daa.execute());
	}

	@Test
	public void testExecuteWorking() throws ActionExecutionException {
		daa.execute();
	}

	@Test
	public void testExecuteEnumNoneOption() throws ActionExecutionException, RangeValueException {
		ed.getRange().add(ed.getNoneOption());
		daa = new DisAllowAction(ed, ed.getNoneOption());
		daa.execute();
		assertThrows(RangeValueNotEnabledException.class, ()->ed.setValue(ed.getNoneOption().getValue()));
	}

	@Test
	public void testExecuteEnumNoneOptionVisibilityFalse() throws ActionExecutionException, RangeValueException {
		ed.getRange().add(ed.getNoneOption());
		BooleanDecision bd = new BooleanDecision("testbool");
		bd.setSelected(false);
		ed.setVisibility(new IsSelectedFunction(bd));
		daa = new DisAllowAction(ed, ed.getNoneOption());
		daa.execute();
		assertEquals(BooleanValue.getFalse(), bd.getValue());
		assertThrows(RangeValueNotEnabledException.class, ()->ed.setValue(ed.getNoneOption().getValue()));
	}

	@Test
	public void testIsSatisfiedNotSatisfied() throws ActionExecutionException {
		assertFalse(daa.isSatisfied());
	}

	@Test
	public void testIsSatisfiedIsSatisfied() throws ActionExecutionException {
		daa.execute();
		assertTrue(daa.isSatisfied());
	}

	@Test
	public void testIsSatisfiedNotInRange(){
		StringValue s1 = new StringValue("someNotContainedValue");
		ed.getRange().add(s1);
		daa = new DisAllowAction(ed, s1);
		ed.getRange().remove(s1);
		assertThrows(ActionExecutionException.class, ()->daa.isSatisfied());
	}

	@Test
	public void testGetVariable() {
		assertEquals(ed, daa.getVariable());
	}

	@Test
	public void testGetValue() {
		assertEquals(s, daa.getValue());
	}

	@Test
	public void testEqualsDisAllowActionSame() {
		DisAllowAction daa2 = new DisAllowAction(ed, s);
		assertEquals(daa,daa2);
	}

	@Test
	public void testEqualsItself() {
		assertEquals(daa,daa);
	}

	@Test
	public void testEqualsNull() {
		assertNotEquals(daa,null);
	}

	@Test
	public void testEqualsObject() {
		assertNotEquals(daa,s);
	}

	@Test
	public void testEqualsDisAllowActionDifferent() {
		EnumerationDecision ed2 = new EnumerationDecision("Test2");
		StringValue s2 = new StringValue("Value2");
		ed2.getRange().add(s2);
		DisAllowAction daa2 = new DisAllowAction(ed2, s2);
		assertNotEquals(daa,daa2);
	}

	@Test
	public void testToString() {
		assertEquals("disAllow(" + daa.getVariable() + "." + daa.getValue() + ");", daa.toString());
	}

}
