package at.jku.cps.travart.dopler.decision.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ICondition;

public class GreaterEqualsTest {
	private GreaterEquals ge;

	@BeforeEach
	public void setUp() throws Exception {
		ge = new GreaterEquals(ICondition.FALSE, ICondition.TRUE);
	}

	@Test
	public void testGreaterEqualsGetValueFunctionAndARangeValue() throws RangeValueException {
		EnumerationDecision ed = new EnumerationDecision("testDecision");
		StringValue s = new StringValue("TestValue");
		ed.getRange().add(s);
		ed.setValue(s.getValue());
		GetValueFunction gvf = new GetValueFunction(ed);
		ge = new GreaterEquals(gvf, s);
		assertTrue(ge.evaluate(),"StringValue s is same as StringValue s");
	}

	@Test
	public void testGreaterEqualsGetValueFunctionAndGetValueFunctionStrings() {
		EnumerationDecision ed = new EnumerationDecision("testDecision");
		StringValue s = new StringValue("TestValue");
		ed.getRange().add(s);
		GetValueFunction gvf1 = new GetValueFunction(ed);
		GetValueFunction gvf2 = new GetValueFunction(ed);
		ge = new GreaterEquals(gvf1, gvf2);
		assertTrue(ge.evaluate(),"Both getValueFunction should return the same value.");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsGetValueFunctionAndARangeValue1() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(3);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(gvf1, d2);
		assertFalse(ge.evaluate(),"Numberdecision has a lower value than value2 therefore should be false.");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsGetValueFunctionAndARangeValue2() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(3);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(gvf1, d2);
		assertTrue(ge.evaluate(),"Numberdecision has a higher value than value2 therefore should be true.");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsGetValueFunctionAndARangeValue3() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(gvf1, d2);
		assertTrue(ge.evaluate(),"Numberdecision is equal to value2 therefore should be true.");
	}

	@Test
	public void testGreaterEqualsARangeValueAndGetValueFunction1() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(3);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(d2, gvf1);
		assertTrue(ge.evaluate(),d2.toString() + "should be >= " + nd1.getValue());
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsARangeValueAndGetValueFunction2() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(3);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(d2, gvf1);
		assertFalse(ge.evaluate(),d2.toString() + "should not be >= " + nd1.getValue());
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsARangeValueAndGetValueFunction3() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		ge = new GreaterEquals(d2, gvf1);
		assertTrue(ge.evaluate(),"The NumberValue is equal to the decision value, therefore should be true.");
	}

	/**
	 * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the
	 * value comparison is correct.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsGetValueFunctionAndGetValueFunctionNumbers1() throws RangeValueException {
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
		ge = new GreaterEquals(gvf1, gvf2);
		assertFalse(ge.evaluate(),"Decision1 has a lower value than decision2 and should therefore return false.");
	}

	/**
	 * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the
	 * value comparison is correct.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterEqualsGetValueFunctionAndGetValueFunctionNumbers2() throws RangeValueException {
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
		ge = new GreaterEquals(gvf2, gvf1);
		assertTrue(ge.evaluate(),"Decision2 has a lower value than decision1 and should therefore return true.");
	}

	@Test
	public void testEvaluateFalseTrue() {
		assertFalse(ge.evaluate(),"Returns false because there is no hierarchy between true and false");
	}

	@Test
	public void testEvaluateTrueFalse() {
		ge = new GreaterEquals(ICondition.TRUE, ICondition.FALSE);
		assertFalse(ge.evaluate(),"Returns false because there is no hierarchy between true and false");
	}

	@Test
	public void testEvaluateFalseFalse() {
		ge = new GreaterEquals(ICondition.FALSE, ICondition.FALSE);
		assertFalse(ge.evaluate(),"Should return true, because two false are not comparables!");
	}

	@Test
	public void testEvaluateTrueTrue() {
		ge = new GreaterEquals(ICondition.TRUE, ICondition.TRUE);
		assertTrue(ge.evaluate(),"Should return true, because two true are the same!");
	}

	@Test
	public void testGreaterEqualsNullFalse() {
		assertThrows(NullPointerException.class,()-> ge = new GreaterEquals(null, ICondition.FALSE));
	}

	@Test
	public void testGreaterEqualsFalseNull() {
		assertThrows(NullPointerException.class,()->ge = new GreaterEquals(ICondition.FALSE, null));
	}

	@Test
	public void testToStringFalseTrue() {
		assertEquals(ICondition.FALSE + " >= " + ICondition.TRUE, ge.toString(),"Text should be identical");
	}

}
