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

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;

public class DeSelectDecisionAction implements IAction {

	private final ADecision<Boolean> decision;

	public DeSelectDecisionAction(final ADecision<Boolean> decision) {
		this.decision = Objects.requireNonNull(decision);
	}

	@Override
	public void execute() throws ActionExecutionException {
		try {
			decision.setValue(false);
		} catch (RangeValueException e) {
			throw new ActionExecutionException(e);
		}
	}

	@Override
	public boolean isSatisfied() throws ActionExecutionException {
		return !decision.isSelected();
	}

	@Override
	public IDecision<Boolean> getVariable() {
		return decision;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public IValue getValue() {
		return BooleanValue.getFalse();
	}

	@Override
	public int hashCode() {
		return Objects.hash(decision);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DeSelectDecisionAction other = (DeSelectDecisionAction) obj;
		return Objects.equals(decision, other.decision);
	}

	@Override
	public String toString() {
		return decision + " = false;";
	}
}
