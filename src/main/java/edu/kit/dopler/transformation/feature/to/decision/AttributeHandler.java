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
package edu.kit.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;

/** This interface is responsible for handling all attributes inside a {@link FeatureModel}. */
public interface AttributeHandler {

    /**
     * Depending on the given {@link FeatureModel}, this method creates new {@link IDecision}s and new {@link Rule}s and
     * adds them to the given {@link Dopler} model.
     *
     * @param decisionModel Decision model to add the newly created decisions and rules
     * @param featureModel  Feature model containing features with their attributes
     * @param level         Strategy that is used to handle the attributes
     */
    void handleAttributes(Dopler decisionModel, FeatureModel featureModel, IModelTransformer.STRATEGY level);
}
