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
package edu.kit.travart.dopler.transformation.decision.to.feature;

import de.vill.model.Attribute;
import de.vill.model.Feature;
import edu.kit.dopler.model.Enforce;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IValue;
import edu.kit.travart.dopler.transformation.util.FeatureFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Implementation of AttributeCreator. */
public class AttributeCreatorImpl implements AttributeCreator {

    private final FeatureFinder featureFinder;

    /**
     * Constructor of {@link AttributeCreatorImpl}.
     *
     * @param featureFinder {@link FeatureFinder}
     */
    public AttributeCreatorImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    @Override
    public void handleAttributeDecisions(List<IDecision<?>> attributeDecisions, Feature rootFeature,
                                         List<IAction> allActions) {

        //Create one attribute for each attribute decision.
        for (IDecision<?> attributeDecision : attributeDecisions) {
            String[] split = attributeDecision.getDisplayId().split("#");
            String featureName = split[0];
            String attributeName = split[1];
            IValue<?> attributeValue = findAttributeValue(allActions, featureName, attributeName).orElseThrow();

            Feature feature = featureFinder.findFeatureByName(rootFeature, featureName).orElseThrow();
            feature.getAttributes().put(attributeName, new Attribute<>(attributeName, attributeValue.getValue()));
        }
    }

    private Optional<IValue<?>> findAttributeValue(List<IAction> allActions, String featureName, String attributeName) {
        //Search in all actions for the attribute value
        for (IAction action : new ArrayList<>(allActions)) {

            //Action must be 'enforce'
            if (!(action instanceof Enforce enforce)) {
                continue;
            }

            //displayId of the decision of the 'enforce' must match the pattern
            String displayId = enforce.getDecision().getDisplayId();
            if (!TreeBuilderImpl.ATTRIBUTE_PATTERN.matcher(displayId).matches()) {
                continue;
            }

            //check if the action is really for the given feature and attribute
            String[] split = displayId.split("#");
            if (Objects.equals(split[0], featureName) && Objects.equals(split[1], attributeName)) {
                allActions.remove(action);
                return Optional.ofNullable(enforce.getValue());
            }
        }

        return Optional.empty();
    }
}
