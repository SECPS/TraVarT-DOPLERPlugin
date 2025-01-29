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
package edu.kit.travart.dopler.transformation.exceptions;

/**
 * This exception is thrown when a feature that should exist, could not be found.
 */
public class FeatureNotPresentException extends RuntimeException {

    private final String shouldBeFoundBy;

    /**
     * Constructor of {@link FeatureNotPresentException}
     *
     * @param shouldBeFoundBy The string with which the feature was searched.
     */
    public FeatureNotPresentException(String shouldBeFoundBy) {
        this.shouldBeFoundBy = shouldBeFoundBy;
    }

    @Override
    public String getMessage() {
        return "Feature could not be found. Searched by: " + shouldBeFoundBy;
    }
}
