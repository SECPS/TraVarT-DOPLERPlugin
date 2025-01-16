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
import edu.kit.dopler.model.IAction;

/**
 * This interface is responsible for creating {@link IAction}s in the {@link Dopler} model from given
 * {@link Constraint}s from a {@link FeatureModel}.
 */
public interface ActionCreator {

    /**
     * Creates an {@link IAction} with the given {@link Constraint}.
     *
     * @param decisionModel {@link Dopler} model, in which the {@link IAction} is put later
     * @param constraint    {@link Constraint} that will be translated
     *
     * @return Created {@link IAction}
     */
    IAction createAction(Dopler decisionModel, Constraint constraint);
}
