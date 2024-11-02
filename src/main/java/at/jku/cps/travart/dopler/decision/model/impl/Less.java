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
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Less extends ABinaryCondition {

    public static final String SYMBOL = "<";

    public Less(final ICondition left, final ICondition right) {
        super(left, right);
    }

    @Override
    public boolean evaluate() {
        if (getLeft() == ICondition.TRUE || getLeft() == ICondition.FALSE) {
            return false;
        }
        if (getRight() == ICondition.TRUE || getRight() == ICondition.FALSE) {
            return false;
        }
        if (getLeft() instanceof GetValueFunction && getRight() instanceof AbstractRangeValue) {
            GetValueFunction function = (GetValueFunction) getLeft();
            AbstractRangeValue value = (AbstractRangeValue) getRight();
            return function.execute().lessThan(value);
        }
        if (getLeft() instanceof AbstractRangeValue && getRight() instanceof GetValueFunction) {
            AbstractRangeValue value = (AbstractRangeValue) getLeft();
            GetValueFunction function = (GetValueFunction) getRight();
            return value.lessThan(function.execute());
        }
        if (getLeft() instanceof GetValueFunction && getRight() instanceof GetValueFunction) {
            GetValueFunction leftFunction = (GetValueFunction) getLeft();
            GetValueFunction rightFunction = (GetValueFunction) getRight();
            return leftFunction.execute().lessThan(rightFunction.execute());
        }
        return getLeft().evaluate() && getRight().evaluate();
    }

    @Override
    public String toString() {
        return getLeft() + " " + SYMBOL + " " + getRight();
    }
}
