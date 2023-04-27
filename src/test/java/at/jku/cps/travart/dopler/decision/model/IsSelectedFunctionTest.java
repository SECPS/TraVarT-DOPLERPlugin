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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;

public class IsSelectedFunctionTest {

	private IsSelectedFunction fun;
	private BooleanDecision b;

	@BeforeEach
	public void prepareObject() {
		b = new BooleanDecision("test");
		b.setSelected(true);
		fun = new IsSelectedFunction(b);
	}

	@Test
	/**
	 * Null as a parameter for the constructor should not be allowed
	 */
	public void testIsSelectedFunction() {
		assertThrows(NullPointerException.class,()->new IsSelectedFunction(null));
	}

	@Test
	public void testExecute() {
		assertTrue(fun.execute());
		b.setSelected(false);
		assertFalse(fun.execute());
	}

	@Test
	public void testEval() {
		assertTrue(fun.evaluate());
		b.setSelected(false);
		assertFalse(fun.evaluate());
	}

	@Test
	public void testToString() {
		assertEquals("isSelected(" + b.toString() + ")", fun.toString());
	}

	@Test
	public void testGetName() {
		assertEquals("isSelected", fun.getName());
	}

	@Test
	public void testEqualsObject() {
		assertNotEquals(fun, null);
		assertNotEquals(fun, "a String");
		assertEquals(fun, fun);
		BooleanDecision b2 = new BooleanDecision("test");
		b2.setSelected(true);
		IsSelectedFunction fun2 = new IsSelectedFunction(b2);
		BooleanDecision b3 = new BooleanDecision("test2");
		b3.setSelected(false);
		IsSelectedFunction fun3 = new IsSelectedFunction(b3);
		assertEquals(fun, fun2);
		assertNotEquals(fun, fun3);
	}

}
