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

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IValueTest {

    @ParameterizedTest
    @MethodSource("instancesToTest")
    public void testGetValue(IValue val) {
        if (val instanceof EnumerationDecision) {
            assertEquals(new StringValue(" "), val.getValue());
        } else {
            val.getValue();
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("instancesToTest")
    public void testSetValue(IValue val) throws RangeValueException {
        if (!(val instanceof AbstractDecision)) {
            assertThrows(Throwable.class, () -> val.setValue(null));
        } else if (val instanceof NumberDecision) {
            val.setValue(new DoubleValue(2.5));
        } else if (val instanceof BooleanDecision) {
            val.setValue(BooleanValue.getFalse());
        } else if (val instanceof StringDecision) {
            val.setValue(new StringValue("testest"));
        }
    }

    private static Stream<Arguments> instancesToTest() {
        return Stream.of(Arguments.of(BooleanValue.getTrue()), Arguments.of(new DoubleValue(1.3)),
                Arguments.of(new StringValue("test")));
    }
}
