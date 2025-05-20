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

import de.vill.model.Feature;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.DecisionValueCallExpression;
import edu.kit.dopler.model.EnumeratorLiteralExpression;
import edu.kit.dopler.model.Equals;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.travart.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.travart.dopler.transformation.util.FeatureFinder;

import java.util.Optional;
import java.util.Set;

/** Implementation of {@link ParentFinder}. */
public class ParentFinderImpl implements ParentFinder {

    private final FeatureFinder featureFinder;

    /**
     * Constructor of {@link ParentFinderImpl}
     *
     * @param featureFinder {@link FeatureFinder}
     */
    public ParentFinderImpl(FeatureFinder featureFinder) {
        this.featureFinder = featureFinder;
    }

    @Override
    public Optional<Feature> getParentFromVisibility(Set<Feature> allFeatures, IExpression visibility) {

        //covers 'true'
        if (visibility instanceof BooleanLiteralExpression booleanLiteralExpression &&
                booleanLiteralExpression.getLiteral()) {
            return Optional.empty(); //Visibility is `true`. The root is the parent.
        }

        //covers 'getValue(someDecision) = someValue'
        if (visibility instanceof Equals equals) {
            IExpression left = equals.getLeftExpression();
            IExpression right = equals.getRightExpression();

            if (left instanceof DecisionValueCallExpression decisionValueCallExpression) {
                IDecision<?> decision = decisionValueCallExpression.getDecision();

                if (right instanceof BooleanLiteralExpression) {
                    return Optional.of(
                            featureFinder.findFeatureByName(allFeatures, decision.getDisplayId()).orElseThrow());
                } else if (right instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                    String value = enumeratorLiteralExpression.getEnumerationLiteral().getValue();
                    return Optional.of(featureFinder.findFeatureByName(allFeatures, value).orElseThrow());
                }
            }
        }

        throw new CanNotBeTranslatedException(visibility);
    }
}
