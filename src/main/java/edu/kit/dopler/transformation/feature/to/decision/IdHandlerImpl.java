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

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.util.DecisionFinder;

/** Implementation of {@link IdHandler}. */
public class IdHandlerImpl implements IdHandler {

    private final DecisionFinder decisionFinder;

    /**
     * Constructor of {@link IdHandlerImpl}.
     *
     * @param decisionFinder {@link DecisionFinder}
     */
    public IdHandlerImpl(DecisionFinder decisionFinder) {
        this.decisionFinder = decisionFinder;
    }

    @Override
    public String resolveId(Dopler decisionModel, Feature feature) {
        StringBuilder featureNameBuilder = new StringBuilder(feature.getFeatureName());

        //If decision or value already exists then append "*" to the id.
        while (nameExistsAlready(decisionModel, featureNameBuilder.toString())) {
            featureNameBuilder.append("*");
        }

        return featureNameBuilder.toString();
    }

    private boolean nameExistsAlready(Dopler decisionModel, String featureName) {
        return decisionFinder.findDecisionById(decisionModel, featureName).isPresent() ||
                decisionFinder.findDecisionByValue(decisionModel, featureName).isPresent();
    }
}
