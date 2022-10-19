package at.jku.cps.travart.dopler.decision.model.impl;

import java.util.Objects;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueNotEnabledException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;

public class StringDecision extends ADecision<String> {

	private static final String RANGE_VALUE_NULL_ERROR = "Given value for decision %s is null";
	private static final String RANGE_VALUE_NOT_ENABLED_ERROR = "Value %s is not enabled. Can't be set for String decision %s";

	private Range<String> range;
	private ARangeValue<String> value;

	public StringDecision(final String id) {
		super(id, DecisionType.STRING);
		range = new Range<>();
		value = new StringValue("");
	}

	@Override
	public ARangeValue<String> getValue() {
		return value;
	}

	@Override
	public void setValue(final String value) throws RangeValueException {
		if (value == null) {
			throw new RangeValueException(String.format(RANGE_VALUE_NULL_ERROR, getId()));
		}
		ARangeValue<String> rangeValue = getRangeValue(value);
		if (!rangeValue.isEnabled()) {
			throw new RangeValueNotEnabledException(String.format(RANGE_VALUE_NOT_ENABLED_ERROR, value, getId()));
		}
		this.value = rangeValue;
		setSelected(true);
	}

	@Override
	public Range<String> getRange() {
		return range;
	}

	@Override
	public void setRange(final Range<String> range) {
		this.range = range;
	}

	@Override
	public ARangeValue<String> getRangeValue(final String str) {
		String v = Objects.requireNonNull(str);
		for (ARangeValue<String> r : range) {
			if (r.getValue().equals(v)) {
				return r;
			}
		}
		return new StringValue(v);
	}

	@Override
	public void reset() throws RangeValueException {
		value = new StringValue("");
		setSelected(false);
		setIsTaken(false);
	}
}
