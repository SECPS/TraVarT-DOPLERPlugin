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

import java.util.Objects;

import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;

@SuppressWarnings("rawtypes")
public class DecisionValueCondition implements ICondition {

	private final IDecision decision;
	private final AbstractRangeValue value;

	public DecisionValueCondition(final IDecision decision, final AbstractRangeValue value) {
		this.decision = Objects.requireNonNull(decision);
		this.value = Objects.requireNonNull(value);
	}

	public IDecision getDecision() {
		return decision;
	}

	public AbstractRangeValue getValue() {
		return value;
	}

	@Override
	public boolean evaluate() {
		return decision.getValue().equals(value);
	}

	@Override
	public String toString() {
		return decision + "." + value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(decision, value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DecisionValueCondition other = (DecisionValueCondition) obj;
		return Objects.equals(decision, other.decision) && Objects.equals(value, other.value);
	}
}
