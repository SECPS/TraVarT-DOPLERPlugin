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
package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Rule;

/**
 * This interface is responsible for translating {@link Constraint}s from a {@link FeatureModel} to {@link Rule}s from a
 * {@link Dopler} model.
 */
public interface ConstraintHandler {

    /**
     * Translates the {@link Constraint}s of the given {@link FeatureModel} to {@link Rule}s and adds them to the given
     * {@link Dopler} model.
     *
     * @param featureModel  {@link FeatureModel} that contains the {@link Constraint}s
     * @param decisionModel {@link Dopler} model that should have the created {@link Rule}s at the end
     */
    void handleConstraints(FeatureModel featureModel, Dopler decisionModel);
}
