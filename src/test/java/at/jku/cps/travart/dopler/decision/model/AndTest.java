package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

public class AndTest {

	@Test
	public void toStringSimpleTest() {
		And a = new And(ICondition.TRUE, ICondition.TRUE);
		assertEquals("(true && true)", a.toString());
	}

	/*
	 * This may have to be rewritten, to test for correct String representation of
	 * Decisions later.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void toStringAdvancedTest() throws RangeValueException, CircleInConditionException {
		And a = new And(ICondition.TRUE, ICondition.TRUE);
		IDecision b = new BooleanDecision("test");
		IDecision c = null;
		b.setValue(true);
		a.setLeft(new IsTakenFunction(b));
		c = new EnumerationDecision("test2");
		StringValue s = new StringValue("EnumValue");
		Range r = new Range();
		r.add(s);
		c.setRange(r);
		c.setValue(s.getValue());
		a.setRight(new IsTakenFunction(c));
		// TODO: now throws a null pointer exception
		assertEquals("(" + a.getLeft().toString() + " && " + a.getRight().toString() + ")", a.toString());
	}

	@Test
	public void convolutedOnceEvalTest() throws CircleInConditionException {
		And aLeft = new And(ICondition.TRUE, ICondition.TRUE);
		And aRight = new And(ICondition.TRUE, ICondition.TRUE);
		And aMid = new And(aLeft, aRight);
		assertTrue(aMid.evaluate());
		aLeft.setLeft(ICondition.FALSE);
		assertFalse(aMid.evaluate());
	}

	@Test
	public void toStringTest() {
		And aLeft = new And(ICondition.TRUE, ICondition.TRUE);
		And aRight = new And(ICondition.TRUE, ICondition.TRUE);
		And aMid = new And(aLeft, aRight);
		assertEquals("(" + aLeft.toString() + " && " + aRight.toString() + ")", aMid.toString());
	}

	@Test
	public void testInfiniteLoop() throws CircleInConditionException {
		And begin = new And(ICondition.TRUE, ICondition.TRUE);
		And left = new And(ICondition.TRUE, ICondition.TRUE);
		begin.setLeft(left);
		assertThrows(CircleInConditionException.class,()-> left.setLeft(begin));
	}
}
