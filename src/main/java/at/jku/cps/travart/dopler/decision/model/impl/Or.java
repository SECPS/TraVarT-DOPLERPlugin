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

import at.jku.cps.travart.dopler.decision.model.ABinaryCondition;
import at.jku.cps.travart.dopler.decision.model.ICondition;

public class Or extends ABinaryCondition {

	public static final String SYMBOL= "||";

	public Or(ICondition left, ICondition right) {
		super(left, right);
	}

	@Override
	public String toString() {
		return String.format("%s "+SYMBOL+" %s", getLeft(), getRight());
	}

	@Override
	public boolean evaluate() {
		return getLeft().evaluate() || getRight().evaluate();
	}
}
