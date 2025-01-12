/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package edu.kit.dopler.plugin;

import java.util.Arrays;

public enum DMCSVHeader {
    ID("ID"), QUESTION("Question"), TYPE("Type"), RANGE("Range"), CARDINALITY("Cardinality"), RULES("Constraint/Rule"),
    VISIBLITY("Visible/relevant if");

    private final String header;

    DMCSVHeader(String header) {
        this.header = header;
    }

    public static String[] stringArray() {
        return Arrays.stream(values()).map(DMCSVHeader::toString).toArray(String[]::new);
    }

    @Override
    public String toString() {
        return header;
    }
}