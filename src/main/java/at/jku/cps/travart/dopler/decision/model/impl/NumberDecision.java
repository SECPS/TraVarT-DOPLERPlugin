package at.jku.cps.travart.dopler.decision.model.impl;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueNotEnabledException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;

public class NumberDecision extends ADecision<Double> {

	private static final String RANGE_VALUE_ERROR = "Value %s is not a range value of decision %s";
	private static final String RANGE_VALUE_NOT_ENABLED_ERROR = "Value %s is not enabled. Can't be set for Number decision %s";

	private Range<Double> range;
	private ARangeValue<Double> value;

	public NumberDecision(final String id) {
		super(id, DecisionType.NUMBER);
		range = new Range<>();
		value = getMinRangeValue();
	}

	@Override
	public ARangeValue<Double> getValue() {
		return value;
	}

	@Override
	public void setValue(final Double value) throws RangeValueException {
		if (value == null || Double.isNaN(value)) {
			throw new RangeValueException(String.format(RANGE_VALUE_ERROR, value, getId()));
		}
		ARangeValue<Double> rangeValue = getRangeValue(value);
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
	public ARangeValue<Double> getRangeValue(final Double value) {
		Double v = Objects.requireNonNull(value);
		for (ARangeValue<Double> rangeValue : range) {
			if (rangeValue.getValue().equals(v)) {
				return rangeValue;
			}
		}
		return null;
	}

	@Override
	public final ARangeValue<Double> getRangeValue(final String str) {
		return getRangeValue(Double.parseDouble(str));
	}

	@Override
	public void reset() throws RangeValueException {
		value = getMinRangeValue();
		setSelected(false);
		setIsTaken(false);
	}

	public ARangeValue<Double> getMinRangeValue() {
		if (range.isEmpty()) {
			return new DoubleValue(0);
		}
		return range.stream().min(Comparator.comparing(ARangeValue<Double>::getValue))
				.orElseThrow(NoSuchElementException::new);
	}

	public ARangeValue<Double> getMaxRangeValue() {
		if (range.isEmpty()) {
			return new DoubleValue(0);
		}
		return range.stream().max(Comparator.comparing(ARangeValue<Double>::getValue))
				.orElseThrow(NoSuchElementException::new);
	}
}
