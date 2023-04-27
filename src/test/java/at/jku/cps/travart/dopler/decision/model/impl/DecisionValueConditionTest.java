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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;

public class DecisionValueConditionTest {
	private DecisionValueCondition dvc;
	private BooleanDecision bd;

	@BeforeEach
	public void setUp() throws Exception {
		bd = new BooleanDecision("test");
		bd.setValue(true);
		dvc = new DecisionValueCondition(bd, BooleanValue.getTrue());
	}

	@Test
	public void testDecisionValueConditionNullARangeValue() {
		assertThrows(NullPointerException.class, ()-> new DecisionValueCondition(null, BooleanValue.getTrue()));
	}

	@Test
	public void testDecisionValueConditionIDecisionNull() {
		assertThrows(NullPointerException.class, ()-> new DecisionValueCondition(bd, null));
	}

	@Test
	public void testGetDecision() {
		assertEquals(bd, dvc.getDecision());
	}

	@Test
	public void testGetValue() {
		assertEquals(BooleanValue.getTrue(), dvc.getValue());
	}

	@Test
	public void testEvaluateEquals() {
		assertTrue(dvc.evaluate());
	}

	@Test
	public void testEvaluateNotEquals() throws RangeValueException {
		bd.setValue(false);
		assertFalse(dvc.evaluate());
	}

	@Test
	public void testEvaluateNoValueSet() throws RangeValueException {
		StringDecision sd = new StringDecision("stest");
		dvc = new DecisionValueCondition(sd, new StringValue("AValue"));
		assertFalse(dvc.evaluate());
	}

	@Test
	public void testToString() {
		assertEquals(dvc.getDecision() + "." + dvc.getValue(), dvc.toString());
	}

}
