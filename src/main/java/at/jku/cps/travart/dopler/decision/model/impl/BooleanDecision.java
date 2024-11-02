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

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;

import java.util.Objects;

public class BooleanDecision extends AbstractDecision<Boolean> {

    private final Range<Boolean> range;
    private AbstractRangeValue<Boolean> value;

    public BooleanDecision(final String id) {
        super(id, DecisionType.BOOLEAN);
        range = new Range<>();
        range.add(BooleanValue.getTrue());
        range.add(BooleanValue.getFalse());
        value = BooleanValue.getFalse();
    }

    @Override
    public AbstractRangeValue<Boolean> getValue() {
        return value;
    }

    @Override
    public void setValue(final Boolean value) throws RangeValueException {
        this.value = getRangeValue(value);
        setSelected(value);
    }

    @Override
    public BooleanValue getRangeValue(final Boolean value) {
        Boolean v = Objects.requireNonNull(value);
        return v ? BooleanValue.getTrue() : BooleanValue.getFalse();
    }

    @Override
    public final AbstractRangeValue<Boolean> getRangeValue(final String str) {
        return getRangeValue(Boolean.valueOf(str));
    }

    @Override
    public Range<Boolean> getRange() {
        return range;
    }

    @Override
    public void setRange(final Range<Boolean> range) {
        /**
         * Does nothing as a boolean has a defined set of values
         */
    }

    @Override
    public void reset() throws RangeValueException {
        setValue(false);
        setIsTaken(false);
        range.forEach(AbstractRangeValue::enable);
    }

    //	@Override
    //	public int compareTo(final Boolean o) {
    //		return value.getValue() == o ? 0 : value.getValue() ? 1 : -1;
    //	}
}
