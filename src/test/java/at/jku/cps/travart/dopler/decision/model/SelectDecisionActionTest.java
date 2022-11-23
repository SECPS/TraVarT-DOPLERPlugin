package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;

public class SelectDecisionActionTest {
	private SelectDecisionAction s;
	private ADecision<Boolean> d;

	@BeforeEach
	public void setUp() throws Exception {
		d = new BooleanDecision("test");
		d.setValue(false);
		s = new SelectDecisionAction(d);
	}

	@Test
	public void testExecute() throws ActionExecutionException {
		s.execute();
		assertTrue(d.getValue().getValue());
		s.execute();
		assertTrue(d.getValue().getValue());
	}

	@Test
	public void testIsSatisfied() throws ActionExecutionException {
		assertFalse(s.isSatisfied());
		s.execute();
		assertTrue(s.isSatisfied());
		s = new SelectDecisionAction(d);
		assertTrue(s.isSatisfied());
		s.execute();
		assertTrue(s.isSatisfied());
	}

	@Test
	public void testSelectDecisionAction() {
		assertThrows(NullPointerException.class, ()->new SelectDecisionAction(null));
	}

	@Test
	public void testEquals() {
		assertNotEquals(s,null);
		assertNotEquals(s,"a String");
		assertEquals(s,s);
		SelectDecisionAction s2 = new SelectDecisionAction(d);
		assertEquals(s,s2);
		ADecision<?> d2 = new StringDecision("test2");
		SelectDecisionAction s3 = new SelectDecisionAction(d2);
		assertNotEquals(s,s3);
	}

}
