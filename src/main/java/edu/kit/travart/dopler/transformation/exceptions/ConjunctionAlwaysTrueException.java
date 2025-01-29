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

import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/** This exception is thrown when a conjunction of a DNF is always true. */
public class ConjunctionAlwaysTrueException extends Exception {

    private final List<Constraint> conjunction;

    /**
     * Constructor of {@link ConjunctionAlwaysTrueException}
     *
     * @param conjunction Conjunction that is always true, represented as a List of {@link Constraint}s
     */
    public ConjunctionAlwaysTrueException(List<Constraint> conjunction) {
        this.conjunction = new ArrayList<>(conjunction);
    }

    @Override
    public String getMessage() {
        return "Conjunction is always true: " + conjunction;
    }
}
