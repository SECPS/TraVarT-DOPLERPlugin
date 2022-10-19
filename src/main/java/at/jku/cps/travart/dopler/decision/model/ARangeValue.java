package at.jku.cps.travart.dopler.decision.model;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class ARangeValue<T> implements IRangeValue<T> {

	private T value;
	private boolean enabled;

	public ARangeValue() {
		enabled = true;
	}

	public ARangeValue(final T value) {
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
		return value.toString().replaceAll("\\.", ",");
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
	public boolean equalTo(final ARangeValue<T> other) {
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ARangeValue other = (ARangeValue) obj;
		return Objects.equals(value, other.value);
	}
}
