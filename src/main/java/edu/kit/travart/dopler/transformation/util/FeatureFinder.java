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
package edu.kit.travart.dopler.transformation.util;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Collection;
import java.util.Optional;

/** This interface is responsible for finding {@link Feature}s inside the {@link FeatureModel}. */
public interface FeatureFinder {

    /**
     * Finds for the given {@link Feature} the first parent that is NOT mandatory. A {@link Feature} is considered
     * mandatory of all its parent {@link Group}s are mandatory.
     *
     * @param featureModel {@link FeatureModel} that contains the given feature
     * @param feature      {@link Feature} from which a non-mandatory parent should be found
     *
     * @return Optional containing nothing if there is no non-mandatory parent or containing the non-mandatory parent if
     * one was found
     */
    Optional<Feature> findFirstNonMandatoryParent(FeatureModel featureModel, Feature feature);

    /**
     * This method is there for replacing {@link LiteralConstraint}s inside more complex constraints. This is needed
     * because some {@link LiteralConstraint}s are always true because the linked {@link Feature} itself and all its
     * parents are mandatory.
     * <p>
     * How it works: the code finds for the given {@link LiteralConstraint} the {@link Feature} that is linked to it.
     * Then it searches the first non-mandatory parent of the found {@link Feature}. At last, it returns a new
     * {@link LiteralConstraint} with the name of the non-mandatory parent.
     *
     * @param featureModel      {@link FeatureModel} containing the {@link Feature} and {@link LiteralConstraint}
     * @param literalConstraint {@link LiteralConstraint} to replace
     *
     * @return Optional containing nothing if there is no non-mandatory parent or containing the non-mandatory parent as
     * a {@link LiteralConstraint} if one was found
     */
    Optional<LiteralConstraint> findFirstNonMandatoryParent(FeatureModel featureModel,
                                                            LiteralConstraint literalConstraint);

    /**
     * Finds a {@link Feature} with the given name inside a collection of {@link Feature}s.
     *
     * @param features    Collection of {@link Feature}s to search in
     * @param featureName Name of the {@link Feature} to search for
     *
     * @return Optional that contains the {@link Feature} if one was found
     */
    Optional<Feature> findFeatureByName(Collection<Feature> features, String featureName);

    /**
     * Finds a {@link Feature} with the given name inside a {@link Feature} tree structure.
     *
     * @param root        STANDARD_MODEL_NAME of tree to search in
     * @param featureName Name of the {@link Feature} to search for
     *
     * @return Optional that contains the {@link Feature} if one was found
     */
    Optional<Feature> findFeatureByName(Feature root, String featureName);
}
