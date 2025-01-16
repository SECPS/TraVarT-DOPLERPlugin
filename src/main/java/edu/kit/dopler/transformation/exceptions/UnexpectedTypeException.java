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

import java.util.Objects;

/**
 * This exception is thrown when an object has an unexpected type.
 */
public class UnexpectedTypeException extends RuntimeException {

    private final Object object;

    /**
     * Constructor of {@link UnexpectedTypeException}
     *
     * @param object Object that had an unexpected type
     */
    public UnexpectedTypeException(Object object) {
        Objects.requireNonNull(object);
        this.object = object;
    }

    @Override
    public String getMessage() {
        return "Unexpected type. Class: '%s', Value: '%s'".formatted(object.getClass().getSimpleName(),
                object.toString());
    }
}
