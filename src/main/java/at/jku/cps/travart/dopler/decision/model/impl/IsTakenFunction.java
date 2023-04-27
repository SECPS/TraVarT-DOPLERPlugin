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
public class IsTakenFunction extends AFunction<Boolean> {

	public static final String FUNCTION_NAME = "isTaken";

	private final IDecision parameter;

	public IsTakenFunction(final IDecision parameter) {
		super(FUNCTION_NAME, Arrays.asList(parameter));
		this.parameter = Objects.requireNonNull(parameter);
	}

	@Override
	public Boolean execute() {
		return parameter.isTaken();
	}

	@Override
	public boolean evaluate() {
		return parameter.isTaken();
	}

	@Override
	public String toString() {
		return FUNCTION_NAME + "(" + parameter + ")";
	}
}
