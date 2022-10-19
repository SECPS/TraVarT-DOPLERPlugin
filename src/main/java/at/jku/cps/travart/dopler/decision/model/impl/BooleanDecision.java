package at.jku.cps.travart.dopler.decision.model.impl;

import java.util.Objects;

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;

public class BooleanDecision extends ADecision<Boolean> {

	private final Range<Boolean> range;
	private ARangeValue<Boolean> value;

	public BooleanDecision(final String id) {
		super(id, DecisionType.BOOLEAN);
		range = new Range<>();
		range.add(BooleanValue.getTrue());
		range.add(BooleanValue.getFalse());
		value = BooleanValue.getFalse();
	}

	@Override
	public ARangeValue<Boolean> getValue() {
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
	public final ARangeValue<Boolean> getRangeValue(final String str) {
		return getRangeValue(Boolean.valueOf(str));
	}

	@Override
	public Range<Boolean> getRange() {
		return range;
	}

	/**
	 * Does nothing as a boolean has a defined set of values
	 */
	@Override
	public void setRange(final Range<Boolean> range) {
	}

	@Override
	public void reset() throws RangeValueException {
		setValue(false);
		setIsTaken(false);
		range.forEach(ARangeValue::enable);
	}
}
