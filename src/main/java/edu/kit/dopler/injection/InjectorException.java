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
package edu.kit.dopler.injection;

/** This exception will be thrown, if the injector could not find an instance of a given class. */
class InjectorException extends RuntimeException {

    private final Class<?> clazz;

    InjectorException(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getMessage() {
        return "Could not find instance of class '%s'. Did you install it and is the order correct?".formatted(clazz);
    }
}
