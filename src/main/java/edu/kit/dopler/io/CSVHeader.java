/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors: 
 *    @author Fabian Eger
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 * All rights reserved
 *******************************************************************************/
package edu.kit.dopler.io;

public enum CSVHeader {
    ID("ID"), QUESTION("Question"), TYPE("Type"), RANGE("Range"), CARDINALITY("Cardinality"), RULES("Constraint/Rule"),
    VISIBLITY("Visible/relevant if");

    private final String header;

    CSVHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
