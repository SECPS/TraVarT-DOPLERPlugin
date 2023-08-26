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

public interface IRangeValue<T> extends IValue<T>, IValueCompareable<AbstractRangeValue<T>>, ICondition {

	void enable();

	void disable();

	boolean isEnabled();
}
