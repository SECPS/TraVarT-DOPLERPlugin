package at.jku.cps.travart.dopler.decision.model;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;


public class IActionTest {
	
	
	private  IAction a=null;
	private static EnumDecision e = new EnumDecision("test");
	private static BooleanDecision b = new BooleanDecision("test");
	
	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testExecute(IAction ac) {
		// just process without an error
		a=ac;
		try {
			a.execute();
		} catch (ActionExecutionException e) {
			e.printStackTrace();
		}
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testIsSatisfied(IAction ac) throws ActionExecutionException {
		// just process without an error
		a=ac;
		a.isSatisfied();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetVariable(IAction ac) {
		// just process without an error
		a=ac;
		a.getVariable();
	}

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void testGetValue(IAction ac) {
		// just process without an error
		a=ac;
		a.getValue();
	}


	private static Stream<Arguments> instancesToTest() {
		Range<String> r = new Range<>();
		StringValue s = new StringValue("test");
		r.add(s);
		e.setRange(r);
		return Stream.of(
				Arguments.of(new AllowAction(e, s)),
				Arguments.of(new DeSelectDecisionAction(b)),
				Arguments.of(new SelectDecisionAction(b)),
				Arguments.of(new DisAllowAction(e, s)),
				Arguments.of(new SetValueAction(e, s))
				);
	}

}
