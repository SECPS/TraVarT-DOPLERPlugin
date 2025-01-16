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
package edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.rule;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.OrConstraint;

import java.util.Optional;

/** (A & (B | C)) ~> ((A & B) | (A & C)) */
public class DistributiveRightDnfRule implements DnfRule {

    @Override
    public Optional<Constraint> replace(Constraint constraint) {
        if (constraint instanceof AndConstraint andConstraint) {
            if (andConstraint.getRight() instanceof OrConstraint orConstraint) {
                Constraint aConstraint = andConstraint.getLeft();
                Constraint bConstraint = orConstraint.getLeft();
                Constraint cConstraint = orConstraint.getRight();
                return Optional.of(new OrConstraint(new AndConstraint(aConstraint, bConstraint),
                        new AndConstraint(aConstraint, cConstraint)));
            }
        }
        return Optional.empty();
    }
}
