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
import at.jku.cps.travart.dopler.decision.model.IDecision;

@SuppressWarnings("rawtypes")
public class IsSelectedFunction extends AFunction<Boolean> {

	public static final String FUNCTION_NAME = "isSelected";

	private final IDecision parameter;

	public IsSelectedFunction(final IDecision parameter) {
		super(FUNCTION_NAME, Arrays.asList(parameter));
		this.parameter = Objects.requireNonNull(parameter);
	}

	@Override
	public boolean evaluate() {
		return parameter.isSelected();
	}

	@Override
	public Boolean execute() {
		return parameter.isSelected();
	}

	@Override
	public String toString() {
		return FUNCTION_NAME + "(" + parameter + ")";
	}

}
