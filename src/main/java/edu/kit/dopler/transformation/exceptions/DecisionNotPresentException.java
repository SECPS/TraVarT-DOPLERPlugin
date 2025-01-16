/*******************************************************************************
 * SPDX-License-Identifier: MPL-2.0
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors:
 *    @author Yannick Kraml
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.dopler.transformation.exceptions;

/** This exception is thrown when a decision that should exist in the model, could not be found by its name. */
public class DecisionNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    /**
     * Constructor of {@link DecisionNotPresentException}.
     *
     * @param shouldBeFoundBy The string with which the decision was searched.
     */
    public DecisionNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Decision could not be found. Searched by: " + shouldBeFoundBy;
    }
}
