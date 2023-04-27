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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class AFunction<R> implements ICondition {

	private final String name;
	private List<IDecision> parameters;

	public AFunction(final String name, final List<IDecision> parameters) {
		this.name = Objects.requireNonNull(name);
		if (parameters.contains(null)) {
			throw new NullPointerException();
		}
		this.parameters = Objects.requireNonNull(parameters);
	}

	public AFunction(final String name, final IDecision... parameters) {
		this(name, Arrays.asList(parameters));
	}

	public abstract R execute();

	public String getName() {
		return name;
	}

	public List<IDecision> getParameters() {
		return parameters;
	}

	public void setParameters(final List<IDecision> parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AFunction)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		AFunction other = (AFunction) o;
		if (this.name.equals(other.name) && this.parameters.equals(other.parameters)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parameters);
	}
}
