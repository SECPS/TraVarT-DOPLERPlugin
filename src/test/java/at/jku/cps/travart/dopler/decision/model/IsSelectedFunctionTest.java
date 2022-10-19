package at.jku.cps.travart.dopler.decision.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;

public class IsSelectedFunctionTest {

	private IsSelectedFunction fun;
	private BooleanDecision b;

	@Before
	public void prepareObject() {
		b = new BooleanDecision("test");
		b.setSelected(true);
		fun = new IsSelectedFunction(b);
	}

	@Test(expected = NullPointerException.class)
	/**
	 * Null as a parameter for the constructor should not be allowed
	 */
	public void testIsSelectedFunction() {
		fun = new IsSelectedFunction(null);
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
