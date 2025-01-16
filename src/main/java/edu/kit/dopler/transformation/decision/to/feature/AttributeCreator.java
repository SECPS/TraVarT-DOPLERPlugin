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
package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;

import java.util.List;

/**
 * This interface is responsible for creating {@link Attribute}s in the {@link FeatureModel}.
 */
public interface AttributeCreator {

    /**
     * Creates attributes for the {@link FeatureModel}.
     *
     * @param attributeDecisions List of all {@link IDecision}s that represent attributes. The visibility is false and
     *                           the name has a specific pattern
     * @param rootFeature        STANDARD_MODEL_NAME feature of the {@link FeatureModel}
     * @param allActions         List of all actions in the {@link Dopler} model. The list will be modified by this
     *                           method. Actions that set the values of attributes will be removed.
     */
    void handleAttributeDecisions(List<IDecision<?>> attributeDecisions, Feature rootFeature, List<IAction> allActions);
}
