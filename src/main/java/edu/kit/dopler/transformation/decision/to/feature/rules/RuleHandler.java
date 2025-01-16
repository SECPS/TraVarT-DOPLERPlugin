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
package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Rule;

import java.util.List;

/**
 * This interface is responsible for translating {@link Rule}s inside a {@link Dopler} model to constraints inside a
 * {@link FeatureModel}.
 */
public interface RuleHandler {

    /**
     * Converts the given {@link Rule}s to {@link Constraint}s and adds them to the given {@link FeatureModel}.
     *
     * @param featureModel {@link FeatureModel} to which the {@link Constraint}s are added
     * @param allRules     {@link Rule}s to convert
     */
    void handleRules(FeatureModel featureModel, List<Rule> allRules);
}
