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

public interface ICondition {

    ICondition TRUE = new ICondition() {
        @Override
        public boolean evaluate() {
            return true;
        }

        @Override
        public String toString() {
            return "true";
        }
    };
    ICondition FALSE = new ICondition() {
        @Override
        public boolean evaluate() {
            return false;
        }

        @Override
        public String toString() {
            return "false";
        }
    };

    boolean evaluate();

    @Override
    String toString();
}
