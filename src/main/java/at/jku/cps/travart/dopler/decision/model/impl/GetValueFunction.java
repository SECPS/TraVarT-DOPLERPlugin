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

import java.util.Arrays;
import java.util.Objects;

import at.jku.cps.travart.dopler.decision.model.AFunction;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.IDecision;

@SuppressWarnings("rawtypes")
public class GetValueFunction extends AFunction<AbstractRangeValue> {

	public static final String FUNCTION_NAME = "getValue";

	private final IDecision parameter;

	public GetValueFunction(final IDecision parameter) {
		super(FUNCTION_NAME, Arrays.asList(parameter));
		this.parameter = Objects.requireNonNull(parameter);
	}

	public IDecision getDecision() {
		return parameter;
	}

	@Override
	public boolean evaluate() {
		return parameter.isSelected();
	}

	@Override
	public AbstractRangeValue execute() {
		Object obj = parameter.getValue();
		if (!(obj instanceof AbstractRangeValue)) {
			throw new IllegalStateException("Parameter has not returned a " + AbstractRangeValue.class);
		}
		return (AbstractRangeValue) obj;
	}

	@Override
	public String toString() {
		return "getValue(" + parameter + ")";
	}
}
