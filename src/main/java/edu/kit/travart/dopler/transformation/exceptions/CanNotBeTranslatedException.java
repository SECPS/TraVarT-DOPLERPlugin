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

/** This exception is thrown when a construct could not be translated. */
public class CanNotBeTranslatedException extends RuntimeException {

    private final Object object;

    /**
     * Constructor of {@link CanNotBeTranslatedException}.
     *
     * @param object Construct that could not be translated.
     */
    public CanNotBeTranslatedException(Object object) {
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "This object could not be translated: " + object.toString();
    }
}
