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
package edu.kit.travart.dopler.transformation.feature.to.decision;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import edu.kit.dopler.model.Dopler;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.ConstraintHandler;

import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ONE_WAY;
import static at.jku.cps.travart.core.common.IModelTransformer.STRATEGY.ROUNDTRIP;

/** Implementation of {@link FmToDmTransformer} */
public class FmToDmTransformerImpl implements FmToDmTransformer {

    private final FeatureAndGroupHandler featureAndGroupHandler;
    private final ConstraintHandler constraintHandler;
    private final AttributeHandler attributeHandler;

    /**
     * Constructor of {@link FmToDmTransformer}.
     *
     * @param featureAndGroupHandler {@link FeatureAndGroupHandler}
     * @param constraintHandler      {@link ConstraintHandler}
     * @param attributeHandler       {@link AttributeHandler}
     */
    public FmToDmTransformerImpl(FeatureAndGroupHandler featureAndGroupHandler, ConstraintHandler constraintHandler,
                                 AttributeHandler attributeHandler) {
        this.featureAndGroupHandler = featureAndGroupHandler;
        this.constraintHandler = constraintHandler;
        this.attributeHandler = attributeHandler;
    }

    @Override
    public Dopler transform(FeatureModel featureModel, IModelTransformer.STRATEGY level) {
        Dopler decisionModel = new Dopler();

        Feature rootFeature = featureModel.getRootFeature();
        if (ONE_WAY == level) {
            //Do not keep root feature
            featureAndGroupHandler.handleFeature(featureModel, decisionModel, rootFeature, ONE_WAY);
        } else if (ROUNDTRIP == level) {
            //Keep root feature in DM
            Group group = new Group(Group.GroupType.MANDATORY);
            group.getFeatures().add(rootFeature);
            featureAndGroupHandler.handleGroup(featureModel, decisionModel, group, ROUNDTRIP);
        }

        attributeHandler.handleAttributes(decisionModel, featureModel, level);
        constraintHandler.handleConstraints(featureModel, decisionModel);

        return decisionModel;
    }
}
