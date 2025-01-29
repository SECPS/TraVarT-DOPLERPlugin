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

/**
 * This exception is thrown when a DNF is always true.
 */
public class DnfAlwaysTrueException extends Exception {

    private final List<List<Constraint>> dnf;

    /**
     * Constructor of {@link DnfAlwaysTrueException}
     *
     * @param conjunctionAlwaysTrueException Cause of this exception.
     * @param dnf                            DNF that is always true
     */
    public DnfAlwaysTrueException(ConjunctionAlwaysTrueException conjunctionAlwaysTrueException,
                                  List<List<Constraint>> dnf) {
        super(conjunctionAlwaysTrueException);
        this.dnf = new ArrayList<>();
        for (List<Constraint> constraints : dnf) {
            this.dnf.add(new ArrayList<>(constraints));
        }
    }

    @Override
    public String getMessage() {
        return "Constraint is always true. DNF: " + dnf;
    }
}

