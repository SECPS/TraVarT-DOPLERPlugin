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
package edu.kit.travart.dopler.transformation.decision.to.feature.rules;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.Rule;

import java.util.Set;

/** This interface is responsible for translating the actions of a given {@link Rule}. */
public interface ActionHandler {

    /**
     * Translates the given {@link IAction}s and creates a single {@link Constraint} with it. This {@link Constraint}
     * will be the right side of a {@link ImplicationConstraint}.
     *
     * @param root    Root of the {@link FeatureModel}
     * @param actions {@link IAction}s from which the right side is generated
     *
     * @return Translated {@link Constraint}
     */
    Constraint createRight(Feature root, Set<IAction> actions);
}
