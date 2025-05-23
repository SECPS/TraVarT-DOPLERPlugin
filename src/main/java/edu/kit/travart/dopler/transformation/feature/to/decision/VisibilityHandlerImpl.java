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
import edu.kit.dopler.model.BooleanDecision;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.StringLiteralExpression;
import edu.kit.travart.dopler.transformation.util.DecisionFinder;
import edu.kit.travart.dopler.transformation.util.FeatureFinder;
import edu.kit.travart.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.travart.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Collections;
import java.util.Optional;

/** Implementation of {@link VisibilityHandler} */
public class VisibilityHandlerImpl implements VisibilityHandler {

    private final FeatureFinder featureFinder;
    private final DecisionFinder decisionFinder;

    /**
     * Constructor of {@link VisibilityHandlerImpl}.
     *
     * @param featureFinder  {@link FeatureFinder}
     * @param decisionFinder {@link DecisionFinder}
     */
    public VisibilityHandlerImpl(FeatureFinder featureFinder, DecisionFinder decisionFinder) {
        this.featureFinder = featureFinder;
        this.decisionFinder = decisionFinder;
    }

    @Override
    public IExpression resolveVisibility(FeatureModel featureModel, Dopler decisionModel, Feature feature,
                                         IModelTransformer.STRATEGY level) {
        Optional<Feature> parent = switch (level) {
            case ONE_WAY -> featureFinder.findFirstNonMandatoryParent(featureModel, feature);
            case ROUNDTRIP -> Optional.ofNullable(feature);
        };

        //Parent has no parent group or is not present. Decision is always visible.
        if (parent.isEmpty() || null == parent.get().getParentGroup()) {
            return new BooleanLiteralExpression(true);
        }

        // If parent is present, then there is a non-mandatory parent.
        // -> Look at parent group of this non-mandatory parent
        return switch (parent.get().getParentGroup().GROUPTYPE) {
            case OPTIONAL -> handleOptionalFeature(decisionModel, parent.get());
            case ALTERNATIVE, OR -> handleAlternativeAndOrFeature(decisionModel, parent.get());
            case MANDATORY -> handleMandatoryFeature(decisionModel, parent.get());
            case GROUP_CARDINALITY -> throw new UnexpectedTypeException(parent.get().getParentGroup());
        };
    }

    @Override
    public IExpression resolveVisibilityForTypeDecisions(Dopler dopler, FeatureModel featureModel, Feature feature,
                                                         String id, IModelTransformer.STRATEGY level) {
        IExpression visibility;
        Group parentGroup = feature.getParentGroup();
        Feature parentFeature = feature.getParentFeature();
        switch (parentGroup.GROUPTYPE) {
            case MANDATORY -> //visibility depends on first non-mandatory parent
                    visibility = resolveVisibility(featureModel, dopler, parentFeature, level);
            case OPTIONAL -> {
                //Insert a check decision. Visibility is this newly created check decision.
                BooleanDecision checkDecision = new BooleanDecision(id + "Check",
                        String.format(FeatureAndGroupHandlerImpl.BOOLEAN_QUESTION, id), "",
                        resolveVisibility(featureModel, dopler, parentFeature, level), Collections.emptySet());
                dopler.addDecision(checkDecision);
                visibility = new StringLiteralExpression(checkDecision.getDisplayId());
            }
            case OR, ALTERNATIVE -> {
                //visibility is 'decision.value', where the 'decision' is the decision of the parent group and
                // 'value' is one of the enum values of this decision
                String value = feature.getFeatureName();
                IDecision<?> decisionByValue = decisionFinder.findDecisionByValue(dopler, value)
                        .orElseThrow(() -> new DecisionNotPresentException(value));
                visibility = new StringLiteralExpression(decisionByValue.getDisplayId() + "." + value);
            }
            case null, default -> throw new UnexpectedTypeException(parentGroup.GROUPTYPE);
        }

        return visibility;
    }

    /**
     * Returns a {@link StringLiteralExpression} containing {@code decision.value}, where {@code value} is the name of
     * the given feature and {@code decision} is the name of the parent of the given feature. If the parent is the root
     * of the model, then a {@link BooleanLiteralExpression} with the value {@code true} is returned.
     */
    private IExpression handleAlternativeAndOrFeature(Dopler decisionModel, Feature parent) {
        Optional<IDecision<?>> parentOfParentDecision =
                decisionFinder.findDecisionByValue(decisionModel, parent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return new BooleanLiteralExpression(true);
        }

        return new StringLiteralExpression(parentOfParentDecision.get().getDisplayId() + "." + parent.getFeatureName());
    }

    private IExpression handleMandatoryFeature(Dopler decisionModel, Feature feature) {
        Optional<IDecision<?>> decisionByValue =
                decisionFinder.findDecisionByValue(decisionModel, feature.getFeatureName());
        if (decisionByValue.isPresent()) {
            return new StringLiteralExpression(decisionByValue.get().getDisplayId() + "." + feature.getFeatureName());
        }

        Optional<IDecision<?>> decisionById = decisionFinder.findDecisionById(decisionModel, feature.getFeatureName());
        if (decisionById.isPresent()) {
            return new StringLiteralExpression(decisionById.get().getDisplayId());
        }

        throw new DecisionNotPresentException(feature.getFeatureName());
    }

    /** Returns a {@link StringLiteralExpression} only containing the name of the given feature. */
    private IExpression handleOptionalFeature(Dopler dopler, Feature parent) {
        Optional<IDecision<?>> decisionById = decisionFinder.findDecisionById(dopler, parent.getFeatureName());
        if (decisionById.isEmpty()) {
            throw new DecisionNotPresentException(parent.getFeatureName());
        }

        return new StringLiteralExpression(decisionById.get().getDisplayId());
    }
}
