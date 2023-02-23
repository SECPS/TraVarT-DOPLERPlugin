package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DoubleValue;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class IDecisionTest {

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testIsSelected(final IDecision dec) {
		assertFalse(dec.isSelected());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testSetSelected(final IDecision dec) {
		assertFalse(dec.isSelected());
		dec.setSelected(true);
		assertTrue(dec.isSelected());
		dec.setSelected(false);
		assertFalse(dec.isSelected());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetId(final IDecision dec) {
		assertEquals("test", dec.getId());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testSetGetQuestion(final IDecision dec) {
		assertEquals("", dec.getQuestion());
		String t = "test";
		dec.setQuestion(t);
		assertEquals(t, dec.getQuestion());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testSetGetDescription(final IDecision dec) {
		assertEquals("", dec.getDescription());
		String t = "test";
		dec.setDescription(t);
		assertEquals(t, dec.getDescription());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetType(final IDecision dec) {
		assertNotNull(dec.getType());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetRange(final IDecision dec) {
		if (dec instanceof BooleanDecision) {
			Range r = new Range();
			r.add(BooleanValue.getFalse());
			r.add(BooleanValue.getTrue());
			assertEquals(r, dec.getRange());
		} else {
			assertNotNull(dec.getRange());
		}
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetRangeValue(final IDecision dec) throws RangeValueException {
		dec.reset();
		if (dec instanceof BooleanDecision) {
			assertNotNull(dec.getRangeValue(false));
		} else if (dec instanceof StringDecision) {
			assertEquals(new StringValue("test"), dec.getRangeValue("test"));
		} else if (dec instanceof NumberDecision) {
			DoubleValue dv = new DoubleValue(0d);
			dec.getRange().add(dv);
			assertEquals(new DoubleValue(0d), dec.getRangeValue(0d));
		} else {
			assertNull(dec.getRangeValue("test"));
		}
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testContainsRangeValue(final IDecision dec) {
		assertFalse(dec.getRange().contains(new StringValue("test")));
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetRules(final IDecision dec) {
		assertTrue(dec.getRules().isEmpty());
	}

//	@ParameterizedTest
//	@MethodSource("instancesToTest")
//	public void testExecuteRules(IDecision dec) {
//		try {
//			dec.executeRules();
//		} catch (ActionExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testAddRule(final IDecision dec) throws RangeValueException {
		Rule r = new Rule(ICondition.TRUE, new AllowAction(dec, new StringValue("test")));
		dec.addRule(r);
		assertTrue(dec.getRules().contains(r));
		dec.getRules().clear();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testAddRules(final IDecision dec) {
		Set<Rule> sr = new HashSet<>();
		Rule r1 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test")));
		Rule r2 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test2")));
		Rule r3 = new Rule(ICondition.FALSE, new SetValueAction(dec, new StringValue("test3")));
		sr.add(r1);
		sr.add(r2);
		sr.add(r3);
		dec.addRules(sr);
		assertTrue(dec.getRules().contains(r1) && dec.getRules().contains(r2) && dec.getRules().contains(r3));
		dec.getRules().clear();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testRemoveRule(final IDecision dec) {
		Set<Rule> sr = new HashSet<>();
		Rule r1 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test")));
		Rule r2 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test2")));
		Rule r3 = new Rule(ICondition.FALSE, new SetValueAction(dec, new StringValue("test3")));
		sr.add(r1);
		sr.add(r2);
		sr.add(r3);
		dec.addRules(sr);
		assertTrue(dec.getRules().contains(r1) && dec.getRules().contains(r2) && dec.getRules().contains(r3));
		assertTrue(dec.removeRule(r1));
		assertTrue(dec.removeRule(r2));
		assertTrue(dec.removeRule(r3));
		assertTrue(dec.getRules().isEmpty());
		dec.getRules().clear();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testSetRules(final IDecision dec) {
		Set<Rule> sr = new HashSet<>();
		Rule r1 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test")));
		Rule r2 = new Rule(ICondition.TRUE, new SetValueAction(dec, new StringValue("test2")));
		Rule r3 = new Rule(ICondition.FALSE, new SetValueAction(dec, new StringValue("test3")));
		Rule r4 = new Rule(ICondition.FALSE, new SetValueAction(dec, new StringValue("test4")));
		dec.addRule(r4);
		assertTrue(dec.getRules().contains(r4));
		sr.add(r1);
		sr.add(r2);
		sr.add(r3);
		dec.setRules(sr);
		assertTrue(!dec.getRules().contains(r4) && dec.getRules().contains(r1) && dec.getRules().contains(r2)
				&& dec.getRules().contains(r3));
		dec.getRules().clear();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetVisibility(final IDecision dec) {
		assertNotNull(dec.getVisiblity());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testSetVisibility(final IDecision dec) {
		dec.setVisibility(ICondition.TRUE);
		assertEquals(ICondition.TRUE, dec.getVisiblity());
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testReset(final IDecision dec) throws Throwable {
		dec.reset();
		if (dec instanceof BooleanDecision) {
			assertNotNull(dec.getValue());

		} else if (dec instanceof NumberDecision) {
			dec.getValue();
			// just don't fail
		} else if (dec instanceof StringDecision) {
			assertEquals(new StringValue(""), dec.getValue());
		} else {
//			ThrowingRunnable r = () -> {
			assertEquals(new StringValue(" "), dec.getValue());
//			};
//			assertThrows(NoSuchElementException.class, r);
		}
//		assertFalse(dec.hasNoneOption());
	}

	private static Stream<Arguments> instancesToTest() {
		return Stream.of(Arguments.of(new BooleanDecision("test")), Arguments.of(new EnumDecision("test")),
				Arguments.of(new NumberDecision("test")), Arguments.of(new StringDecision("test")));
	}
}
