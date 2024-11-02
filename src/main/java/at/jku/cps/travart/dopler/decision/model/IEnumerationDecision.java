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

import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.exc.UnsatisfiedCardinalityException;
import at.jku.cps.travart.dopler.decision.model.impl.Cardinality;

import java.util.Set;

public interface IEnumerationDecision<T> extends IDecision<T> {

    void setValues(Set<AbstractRangeValue<T>> values) throws RangeValueException, UnsatisfiedCardinalityException;

    Set<AbstractRangeValue<T>> getValues();

    Cardinality getCardinality();

    void setCardinality(Cardinality cardinality);

    AbstractRangeValue<String> getNoneOption();

    boolean isNoneOption(AbstractRangeValue<T> value);

    boolean hasNoneOption();
}
