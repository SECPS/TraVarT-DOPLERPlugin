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

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import edu.kit.dopler.model.Rule;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Implementation of {@link RuleHandler}. */
public class RuleHandlerImpl implements RuleHandler {

    private final ConditionHandler conditionHandler;
    private final ActionHandler actionHandler;

    /**
     * Constructor of {@link RuleHandlerImpl}.
     *
     * @param conditionHandler {@link ConditionHandler}
     * @param actionHandler    {@link ActionHandler}
     */
    public RuleHandlerImpl(ConditionHandler conditionHandler, ActionHandler actionHandler) {
        this.conditionHandler = conditionHandler;
        this.actionHandler = actionHandler;
    }

    @Override
    public void handleRules(FeatureModel featureModel, List<Rule> allRules) {
        for (Rule rule : allRules) {
            handleRule(featureModel, rule);
        }

        //Sort constraints
        featureModel.getOwnConstraints().sort(Comparator.comparing(constraint -> constraint.toString(true, "")));
    }

    private void handleRule(FeatureModel featureModel, Rule rule) {
        Optional<Constraint> left = conditionHandler.handleCondition(rule.getCondition());
        Constraint right = actionHandler.createRight(featureModel.getRootFeature(), rule.getActions());

        Constraint newConstraint;
        if (left.isPresent()) {
            // constraint: 'left -> right'
            newConstraint = new ImplicationConstraint(left.get(), right);
        } else {
            // constraint: 'right'
            newConstraint = right;
        }
        // Check if constraint already exists before adding
        boolean constraintExists = featureModel.getOwnConstraints().stream()
            .anyMatch(existingConstraint -> existingConstraint.equals(newConstraint));
        if (!constraintExists) {
            featureModel.getOwnConstraints().add(newConstraint);
        }
    }
}
