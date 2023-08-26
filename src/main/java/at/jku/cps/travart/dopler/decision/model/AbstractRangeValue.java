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

import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class AbstractRangeValue<T> implements IRangeValue<T> {

	private T value;
	private boolean enabled;

	protected AbstractRangeValue() {
		enabled = true;
	}

	protected AbstractRangeValue(final T value) {
		this();
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(final T value) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public String toString() {
		return value.toString().replace('.', ',');
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void enable() {
		this.enabled = true;
	}

	@Override
	public void disable() {
		this.enabled = false;
	}

	@Override
	public boolean equalTo(final AbstractRangeValue<T> other) {
		return getValue().equals(other.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		AbstractRangeValue other = (AbstractRangeValue) obj;
		return Objects.equals(value, other.value);
	}
}
