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

public class InvalidActionException extends ParserException {

	private static final long serialVersionUID = 1L;

	public InvalidActionException(final Exception e) {
		super(e);
	}

	public InvalidActionException(final String string) {
		super(string);
	}

}
