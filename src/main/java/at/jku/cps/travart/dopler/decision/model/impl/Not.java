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

import at.jku.cps.travart.dopler.decision.model.AUnaryCondition;
import at.jku.cps.travart.dopler.decision.model.ICondition;

public class Not extends AUnaryCondition {

	public static final String SYMBOL= "!";

	public Not(ICondition value) {
		super(value);
	}

	@Override
	public String toString() {
		return String.format(SYMBOL+"%s", getOperand());
	}

	@Override
	public boolean evaluate() {
		return !getOperand().evaluate();
	}
}
