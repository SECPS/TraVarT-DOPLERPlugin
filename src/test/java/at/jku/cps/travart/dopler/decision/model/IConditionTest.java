package at.jku.cps.travart.dopler.decision.model;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsTakenFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Not;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Or;
import at.jku.cps.travart.dopler.decision.model.impl.StringDecision;

public class IConditionTest {

	@ParameterizedTest
	@MethodSource("instancesToTest")
	public void evalTest(ICondition cond) throws CircleInConditionException {
		//just pass without failure
		cond.evaluate();
	}
	
	private static Stream<Arguments> instancesToTest() {
		return Stream.of(
				Arguments.of(ICondition.FALSE),
				Arguments.of(ICondition.TRUE),
				Arguments.of(new IsTakenFunction(new BooleanDecision("test"))),
				Arguments.of(new IsTakenFunction(new EnumerationDecision("test"))),
				Arguments.of(new And(ICondition.TRUE, ICondition.TRUE)),
				Arguments.of(new Or(ICondition.TRUE, ICondition.TRUE)),
				Arguments.of(new IsTakenFunction(new StringDecision("test"))),
				Arguments.of(new Not(ICondition.TRUE)),
				Arguments.of(new IsTakenFunction(new BooleanDecision("id"))),
				Arguments.of(new IsTakenFunction(new EnumerationDecision("id"))),
				Arguments.of(new IsTakenFunction(new NumberDecision("id"))),
				Arguments.of(new IsTakenFunction(new StringDecision("id"))),
				Arguments.of(new And(ICondition.TRUE, ICondition.TRUE)),
				Arguments.of(new Or(ICondition.TRUE, ICondition.TRUE)),
				Arguments.of(new Not(ICondition.TRUE))
				);
	}
	
}

