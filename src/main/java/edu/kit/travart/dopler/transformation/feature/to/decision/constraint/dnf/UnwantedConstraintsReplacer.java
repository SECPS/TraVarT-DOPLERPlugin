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
package edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

/** Replaces unwanted constraints and replaces them with different ones. */
public interface UnwantedConstraintsReplacer {

    /**
     * Replaces unwanted constraints by semantically equal ones.
     *
     * @param constraint Constraint in which unwanted constraints should be replaced
     *
     * @return Copy of given constraint with the replaced constraints.
     */
    Constraint replaceUnwantedConstraints(Constraint constraint);
}
