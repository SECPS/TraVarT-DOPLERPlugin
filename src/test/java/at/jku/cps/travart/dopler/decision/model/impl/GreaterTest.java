package at.jku.cps.travart.dopler.decision.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ICondition;

public class GreaterTest {
	private Greater g;

	@BeforeEach
	public void setUp() throws Exception {
		g = new Greater(ICondition.FALSE, ICondition.TRUE);
	}

	@Test
	public void testGreaterGetValueFunctionAndARangeValue() throws RangeValueException {
		EnumDecision ed = new EnumDecision("testDecision");
		StringValue s = new StringValue("TestValue");
		ed.getRange().add(s);
		ed.setValue(s.getValue());
		GetValueFunction gvf = new GetValueFunction(ed);
		g = new Greater(gvf, s);
		assertFalse(g.evaluate(),"StringValue s is same as StringValue s");
	}

	@Test
	public void testGreaterGetValueFunctionAndGetValueFunctionStrings() {
		EnumDecision ed = new EnumDecision("testDecision");
		StringValue s = new StringValue("TestValue");
		ed.getRange().add(s);
		GetValueFunction gvf1 = new GetValueFunction(ed);
		GetValueFunction gvf2 = new GetValueFunction(ed);
		g = new Greater(gvf1, gvf2);
		assertFalse(g.evaluate(),"Both getValueFunction should return the same value.");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterGetValueFunctionAndARangeValue1() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(3);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(gvf1, d2);
		assertFalse(g.evaluate(),gvf1.getDecision().getValue() + " > " + d2 + " should be false");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterGetValueFunctionAndARangeValue2() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(3);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(gvf1, d2);
		assertTrue(g.evaluate(),gvf1.getDecision().getValue() + " > " + d2 + " should be True");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterGetValueFunctionAndARangeValue3() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(gvf1, d2);
		assertFalse(g.evaluate(),gvf1.getDecision().getValue() + " > " + d2 + " should be false");
	}

	@Test
	public void testGreaterARangeValueAndGetValueFunction1() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(3);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(d2, gvf1);
		assertTrue(g.evaluate(),d2.toString() + " > " + nd1.getValue() + " should be true");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterARangeValueAndGetValueFunction2() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(3);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(d2, gvf1);
		assertFalse(g.evaluate(),d2.toString() + " > " + nd1.getValue() + " should be false");
	}

	/**
	 * Checks a getvaluefunction against a concrete value.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterARangeValueAndGetValueFunction3() throws RangeValueException {
		NumberDecision nd1 = new NumberDecision("testDecision1");

		DoubleValue d1 = new DoubleValue(4);
		DoubleValue d2 = new DoubleValue(4);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		GetValueFunction gvf1 = new GetValueFunction(nd1);
		g = new Greater(d2, gvf1);
		assertFalse(g.evaluate(),d2 + " > " + gvf1.getDecision().getValue() + " should be false");
	}

	/**
	 * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the
	 * value comparison is correct.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterGetValueFunctionAndGetValueFunctionNumbers1() throws RangeValueException {
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
		g = new Greater(gvf1, gvf2);
		assertFalse(g.evaluate(),gvf1.getDecision().getValue() + " > " + gvf2.getDecision().getValue() + " should be false");
	}

	/**
	 * Creates 2 NumberDecisions, adds them to getValueFunctions and checks if the
	 * value comparison is correct.
	 *
	 * @throws RangeValueException
	 */
	@Test
	public void testGreaterGetValueFunctionAndGetValueFunctionNumbers2() throws RangeValueException {
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
		g = new Greater(gvf2, gvf1);
		assertTrue(g.evaluate(),gvf1.getDecision().getValue() + " > " + gvf2.getDecision().getValue() + " should be true");
	}

	@Test
	public void testEvaluateFalseTrue() {
		assertFalse(g.evaluate(),"Returns false because there is no hierarchy between true and false");
	}

	@Test
	public void testEvaluateTrueFalse() {
		g = new Greater(ICondition.TRUE, ICondition.FALSE);
		assertFalse(g.evaluate(),"Returns false because there is no hierarchy between true and false");
	}

	@Test
	public void testEvaluateFalseFalse() {
		g = new Greater(ICondition.FALSE, ICondition.FALSE);
		assertFalse(g.evaluate(),"Should return false, because two false are the same!");
	}

	@Test
	public void testEvaluateTrueTrue() {
		g = new Greater(ICondition.TRUE, ICondition.TRUE);
		assertFalse(g.evaluate(),"Should return false, because two true are the same!");
	}

	@Test
	public void testGreaterNullFalse() {
		assertThrows(NullPointerException.class,()->g = new Greater(null, ICondition.FALSE));
	}

	@Test
	public void testGreaterFalseNull() {
		assertThrows(NullPointerException.class,()->g = new Greater(ICondition.FALSE, null));
	}

	@Test
	public void testToStringFalseTrue() {
		assertEquals(ICondition.FALSE + " > " + ICondition.TRUE, g.toString());
	}

}
