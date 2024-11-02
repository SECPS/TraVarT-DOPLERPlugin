/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model.impl;

import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;

import java.util.Objects;

public final class StringValue extends AbstractRangeValue<String> {

    public StringValue(final String str) {
        super(Objects.requireNonNull(str));
    }

    @Override
    public boolean lessThan(final AbstractRangeValue<String> other) {
        return getValue().compareTo(other.getValue()) < 0;
    }

    @Override
    public boolean greaterThan(final AbstractRangeValue<String> other) {
        return getValue().compareTo(other.getValue()) > 0;
    }

    @Override
    public boolean evaluate() {
        return !getValue().isBlank();
    }
}
