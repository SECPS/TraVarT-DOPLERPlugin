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
package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf;

import de.vill.model.constraint.Constraint;

import java.util.List;

/** Converts a {@link Constraint} into DNF */
public interface TreeToDnfConverter {

    /**
     * Converts the given {@link Constraint} into DNF.
     *
     * @param constraint {@link Constraint} to convert
     *
     * @return DNF. The inner lists are conjunctions. The outer list is the disjunction.
     */
    List<List<Constraint>> convertToDnf(Constraint constraint);
}
