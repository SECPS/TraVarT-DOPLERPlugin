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

import at.jku.cps.travart.dopler.decision.exc.CircleInConditionException;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class IConditionTest {

    @ParameterizedTest
    @MethodSource("instancesToTest")
    public void evalTest(ICondition cond) throws CircleInConditionException {
        //just pass without failure
        cond.evaluate();
    }

    private static Stream<Arguments> instancesToTest() {
        return Stream.of(Arguments.of(ICondition.FALSE), Arguments.of(ICondition.TRUE),
                Arguments.of(new IsTakenFunction(new BooleanDecision("test"))),
                Arguments.of(new IsTakenFunction(new EnumerationDecision("test"))),
                Arguments.of(new And(ICondition.TRUE, ICondition.TRUE)),
                Arguments.of(new Or(ICondition.TRUE, ICondition.TRUE)),
                Arguments.of(new IsTakenFunction(new StringDecision("test"))), Arguments.of(new Not(ICondition.TRUE)),
                Arguments.of(new IsTakenFunction(new BooleanDecision("id"))),
                Arguments.of(new IsTakenFunction(new EnumerationDecision("id"))),
                Arguments.of(new IsTakenFunction(new NumberDecision("id"))),
                Arguments.of(new IsTakenFunction(new StringDecision("id"))),
                Arguments.of(new And(ICondition.TRUE, ICondition.TRUE)),
                Arguments.of(new Or(ICondition.TRUE, ICondition.TRUE)), Arguments.of(new Not(ICondition.TRUE)));
    }
}

