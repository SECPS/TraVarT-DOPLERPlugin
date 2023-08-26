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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;

public class GetValueFunctionTest {
	GetValueFunction gvf;
	NumberDecision nd1;

	@BeforeEach
	public void setUp() throws Exception {
		nd1 = new NumberDecision("nd");
		DoubleValue d1 = new DoubleValue(0);
		nd1.getRange().add(d1);
		nd1.setValue(d1.getValue());
		gvf = new GetValueFunction(nd1);
	}

	@Test
	public void testGetValueFunctionNull() {
		assertThrows(NullPointerException.class,()-> gvf = new GetValueFunction(null));
	}

	@Test
	public void testGetDecision() {
		assertEquals(nd1, gvf.getDecision(),"Should return decision.");
	}

	@Test
	public void testEvaluateNonNaN() {
		assertTrue(gvf.evaluate(),"True, because Non-NaN-Numberdecisions are always true.");
	}

	@Test
	public void testExecuteNoRangeValueDoubleValue() {
		assertEquals(new DoubleValue(0), gvf.execute(),"Return value should be 0.");
	}

	@Test
	public void testExecuteNoRangeValueEnumValue() {
		EnumerationDecision ed1 = new EnumerationDecision("test");
		gvf = new GetValueFunction(ed1);
		assertEquals(new StringValue(" "), gvf.execute(),"Should return single space String");
	}

	@Test
	public void testExecuteNoRangeValueStringValue() {
		StringDecision ed1 = new StringDecision("test");
		gvf = new GetValueFunction(ed1);
		assertEquals(new StringValue(""), gvf.execute(),"Should return empty String");
	}

	@Test
	public void testExecuteARangeValue() {
		AbstractDecision<Object> ad = new AbstractDecision<Object>("test", DecisionType.ENUM) {

			@Override
			public Range<Object> getRange() {
				return null;
			}

			@Override
			public AbstractRangeValue<Object> getRangeValue(final Object value) {
				return null;
			}

			@Override
			public AbstractRangeValue<Object> getRangeValue(final String value) {
				return null;
			}

			@Override
			public void setRange(final Range<Object> range) {
			}

			@Override
			public void reset() throws RangeValueException {
			}

			@Override
			public AbstractRangeValue<Object> getValue() {
				return null;
			}

			@Override
			public void setValue(final Object value) throws RangeValueException {
			}

		};
		gvf = new GetValueFunction(ad);
		assertThrows(IllegalStateException.class,()-> gvf.execute());

	}

	@Test
	public void testToString() {
		assertEquals("getValue(" + nd1.toString() + ")", gvf.toString());
	}

}
