/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.exc;

public class NoValidConfigurationPossibleException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new NoValidConfigurationPossibleException when a non-boolean
	 * decision should be set.
	 *
	 * @param message a string.
	 */
	public NoValidConfigurationPossibleException(final String message) {
		super(message);
	}

	/**
	 * Creates a new NoValidConfigurationPossibleException when a non-boolean
	 * decision should be set.
	 *
	 * @param e an exception.
	 */
	public NoValidConfigurationPossibleException(final Exception e) {
		super(e);
	}
}
