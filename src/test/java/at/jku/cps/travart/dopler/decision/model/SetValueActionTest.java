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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

public class SetValueActionTest {
	private SetValueAction sva;
	private SetValueAction sva2;
	private EnumerationDecision dec;
	private StringValue sv;
	private StringValue sv2;

	@BeforeEach
	public void setUp() throws Exception {
		Range<String> r = new Range<>();
		dec = new EnumerationDecision("test");
		sv = new StringValue("test");
		sv2 = new StringValue("test2");
		r.add(sv);
		r.add(sv2);
		sv.enable();
		sv2.enable();
		dec.setCardinality(new Cardinality(0, 2));
		dec.setRange(r);
		sva = new SetValueAction(dec, sv);
		sva2 = new SetValueAction(dec, sv2);
	}

	@Test
	public void testExecute() throws ActionExecutionException {
		sva.execute();
		assertEquals(sv, dec.getValue());
		assertNotEquals(sv2, dec.getValue());
	}

	@Test
	public void testExecuteEnumDecisionAlternative() throws ActionExecutionException {
		dec.setCardinality(new Cardinality(1, 1));
		sva.execute();
		assertEquals(sv, dec.getValue());
	}

	@Test
	public void testExecuteEnumDecisionMultiples() throws ActionExecutionException, RangeValueException {
		dec.setValue(sv2.getValue());
		sva.execute();
		assertTrue(dec.getValues().contains(sv) && dec.getValues().contains(sv2),
				"Should contain old and new value after execution.");
	}

	@Test
	public void testExecuteEnumDecisionMultiplesExceedingCardinality()
			throws ActionExecutionException, RangeValueException {
		dec.setCardinality(new Cardinality(0, 1));
		dec.setValue(sv2.getValue());
		assertThrows(ActionExecutionException.class, () -> sva.execute());
	}

	@Test
	public void testExecuteNumberDecisionStringValue() throws ActionExecutionException {
		NumberDecision nd = new NumberDecision("test");
		sva = new SetValueAction(nd, sv);
		assertThrows(ClassCastException.class, ()->sva.execute());
	}

	@Test
	public void testExecuteNumberDecisionNull() throws ActionExecutionException {
		NumberDecision nd = new NumberDecision("test");
		assertThrows(NullPointerException.class, ()-> sva=new SetValueAction(nd, null));
		sva.execute();
	}

	@Test
	public void testisSatsifiedEnumDecisionAlternative() throws ActionExecutionException {
		dec.setCardinality(new Cardinality(1, 1));
		sva.execute();
		assertTrue(sva.isSatisfied());
	}

	@Test
	public void testisSatsifiedEnumDecisionAlternativeNotPerformed() throws ActionExecutionException {
		dec.setCardinality(new Cardinality(1, 1));
		assertFalse(sva.isSatisfied());
	}

	@Test
	public void testIsSatisfiedNumberDecisionNull() throws ActionExecutionException {
		NumberDecision nd = new NumberDecision("test");
		assertThrows(NullPointerException.class, ()->sva = new SetValueAction(nd, null));
		sva.isSatisfied();
	}

	@Test
	public void testIsSatisfiedNumberDecisionStringValue() throws ActionExecutionException {
		NumberDecision nd = new NumberDecision("test");
		sva = new SetValueAction(nd, sv);
		assertFalse(sva.isSatisfied());
	}

	@Test
	public void testExecuteNumberDecisionDoubleValue() throws ActionExecutionException {
		NumberDecision nd = new NumberDecision("test");
		DoubleValue val = new DoubleValue(5);
		nd.getRange().add(val);
		sva = new SetValueAction(nd, new DoubleValue(5));
		sva.execute();
		assertEquals(new DoubleValue(5), nd.getValue());
	}

	@Test
	public void testIsSatisfied() throws ActionExecutionException {
		assertFalse(sva.isSatisfied());
		assertFalse(sva2.isSatisfied());
		sva.execute();
		assertTrue(sva.isSatisfied());
		assertFalse(sva2.isSatisfied());
	}

	@Test
	public void testGetVariable() {
		assertEquals(dec, sva.getVariable());
	}

	@Test
	public void testSetValueAction_NullDecision() {
		assertThrows(NullPointerException.class, ()->new SetValueAction(null, sv));
	}

	@Test
	public void testSetValueAction_NullAction() {
		assertThrows(NullPointerException.class, ()->new SetValueAction(dec, null));
	}

	@Test
	public void testEquals() {
		assertNotEquals(sva, null);
		assertNotEquals(sva, "a String");
		assertEquals(sva, sva);
		assertNotEquals(sva, sva2);
		sva2 = new SetValueAction(new EnumerationDecision("other"), sv);
		assertNotEquals(sva, sva2);
		sva2 = new SetValueAction(dec, sv);
		assertEquals(sva, sva2);
	}

	@Test
	public void testToString() {
		assertNotNull(sva.toString());
		assertEquals(dec.toString() + " = " + sva.getValue().toString() + ";", sva.toString());
	}

}
