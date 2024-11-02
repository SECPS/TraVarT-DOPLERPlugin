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
import at.jku.cps.travart.dopler.decision.exc.RangeValueNotEnabledException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class NumberDecision extends AbstractDecision<Double> {

    private static final String RANGE_VALUE_ERROR = "Value %s is not a range value of decision %s";
    private static final String RANGE_VALUE_NOT_ENABLED_ERROR =
            "Value %s is not enabled. Can't be set for Number decision %s";
    private Range<Double> range;
    private AbstractRangeValue<Double> value;

    public NumberDecision(final String id) {
        super(id, DecisionType.NUMBER);
        range = new Range<>();
        value = getMinRangeValue();
    }

    @Override
    public AbstractRangeValue<Double> getValue() {
        return value;
    }

    @Override
    public void setValue(final Double value) throws RangeValueException {
        if (value == null || Double.isNaN(value)) {
            throw new RangeValueException(String.format(RANGE_VALUE_ERROR, value, getId()));
        }
        AbstractRangeValue<Double> rangeValue = getRangeValue(value);
        if (rangeValue == null) {
            throw new RangeValueException(String.format(RANGE_VALUE_ERROR, value, getId()));
        }
        if (!rangeValue.isEnabled()) {
            throw new RangeValueNotEnabledException(String.format(RANGE_VALUE_NOT_ENABLED_ERROR, value, getId()));
        }
        this.value = rangeValue;
        setSelected(true);
    }

    @Override
    public Range<Double> getRange() {
        return range;
    }

    @Override
    public void setRange(final Range<Double> range) {
        this.range = range;
        value = getMinRangeValue();
        setSelected(true);
    }

    @Override
    public AbstractRangeValue<Double> getRangeValue(final Double value) {
        Double v = Objects.requireNonNull(value);
        for (AbstractRangeValue<Double> rangeValue : range) {
            if (rangeValue.getValue().equals(v)) {
                return rangeValue;
            }
        }
        return null;
    }

    @Override
    public final AbstractRangeValue<Double> getRangeValue(final String str) {
        return getRangeValue(Double.parseDouble(str));
    }

    @Override
    public void reset() throws RangeValueException {
        value = getMinRangeValue();
        setSelected(false);
        setIsTaken(false);
    }

    public AbstractRangeValue<Double> getMinRangeValue() {
        if (range.isEmpty()) {
            return new DoubleValue(0);
        }
        return range.stream().min(Comparator.comparing(AbstractRangeValue<Double>::getValue))
                .orElseThrow(NoSuchElementException::new);
    }

    public AbstractRangeValue<Double> getMaxRangeValue() {
        if (range.isEmpty()) {
            return new DoubleValue(0);
        }
        return range.stream().max(Comparator.comparing(AbstractRangeValue<Double>::getValue))
                .orElseThrow(NoSuchElementException::new);
    }
}
