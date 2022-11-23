package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;

public class IsTakenFunctionTest {

	private IsTakenFunction fun;
	private BooleanDecision b;

	@BeforeEach
	public void prepareObject() {
		b = new BooleanDecision("test");
		fun = new IsTakenFunction(b);
	}

	@Test
	/**
	 * Null as a parameter for the constructor should not be allowed
	 */
	public void testIsTakenFunction() {
		assertThrows(NullPointerException.class, () -> fun = new IsTakenFunction(null));
	}

	@Test
	public void testExecute() throws RangeValueException {
		assertFalse(fun.execute());
		b.setSelected(false);
		assertTrue(fun.execute());
		b.reset();
		assertFalse(fun.execute());
		b.setSelected(true);
		assertTrue(fun.execute());
	}

	@Test
	public void testEval() throws RangeValueException {
		assertFalse(fun.execute());
		b.setSelected(false);
		assertTrue(fun.execute());
		b.reset();
		assertFalse(fun.execute());
		b.setSelected(true);
		assertTrue(fun.execute());
	}

	@Test
	public void testToString() {
		assertEquals("isTaken(" + b.toString() + ")", fun.toString());
	}

	@Test
	public void testGetName() {
		assertEquals("isTaken", fun.getName());
	}

	@Test
	public void testEqualsObject() {
		assertNotEquals(fun, null);
		assertNotEquals(fun, "a String");
		assertEquals(fun, fun);
		BooleanDecision b2 = new BooleanDecision("test");
		b2.setSelected(true);
		IsTakenFunction fun2 = new IsTakenFunction(b2);
		BooleanDecision b3 = new BooleanDecision("test2");
		b3.setSelected(false);
		IsTakenFunction fun3 = new IsTakenFunction(b3);
		assertEquals(fun, fun2);
		assertNotEquals(fun, fun3);

	}

}
