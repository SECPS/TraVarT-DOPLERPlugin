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

/**
 * This exception is thrown when a model is invalid.
 */
public class ModelInvalidException extends RuntimeException {

    /**
     * Constructor of {@link ModelInvalidException}.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param e       the cause (which is saved for later retrieval by the getCause() method). (A null value is
     *                permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ModelInvalidException(String message, DnfAlwaysFalseException e) {
        super(message, e);
    }
}
