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

import java.util.List;

/**
 * Converts a DNF in form of a double list to a tree.
 */
public interface DnfToTreeConverter {

    /**
     * Converts a DNF from a {@link List} containing {@link List}s to a tree.
     *
     * @param dnf DNF to convert as a {@link List}.
     *
     * @return DNF as a tree.
     */
    Constraint createDnfFromList(List<List<Constraint>> dnf);
}
