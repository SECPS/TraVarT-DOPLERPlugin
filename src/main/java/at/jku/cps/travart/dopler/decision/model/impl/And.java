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

public class And extends ABinaryCondition {

    public static final String SYMBOL = "&&";

    public And(ICondition left, ICondition right) {
        super(left, right);
    }

    @Override
    public boolean evaluate() {
        return getLeft().evaluate() && getRight().evaluate();
    }

    @Override
    public String toString() {
        return String.format("(%s " + SYMBOL + " %s)", getLeft(), getRight());
    }
}
