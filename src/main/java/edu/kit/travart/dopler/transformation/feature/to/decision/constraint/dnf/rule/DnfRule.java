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
package edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.Constraint;

import java.util.Optional;

/**
 * Interface for a single rule for the DNF algorithm.
 */
public interface DnfRule {

    /**
     * Check if the {@link DnfRule} matches the given {@link Constraint}. If it's matching, then return a new
     * {@link Constraint} that replaces the old one. Both are semantically equal.
     *
     * @param constraint {@link Constraint} to replace (if {@link DnfRule} matches)
     *
     * @return New {@link Constraint} that replaces the old one
     */
    Optional<Constraint> replace(Constraint constraint);
}
