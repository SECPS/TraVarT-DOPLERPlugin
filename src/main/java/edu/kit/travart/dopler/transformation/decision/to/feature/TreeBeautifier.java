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

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

/** This interface is responsible for beautifying the tree structure of the feature model. */
public interface TreeBeautifier {

    /**
     * This is the standard name for the root feature in the {@link FeatureModel}.
     */
    String STANDARD_MODEL_NAME = "STANDARD_MODEL_NAME";

    /**
     * Make the given Root {@link Feature} look good. What is considered good, depends on the implementation and the
     * given {@link IModelTransformer.STRATEGY}.
     *
     * @param root     Root {@link Feature} to beautify
     * @param strategy Currently used {@link IModelTransformer.STRATEGY}
     *
     * @return Beautified root {@link Feature}
     */
    Feature beautify(Feature root, IModelTransformer.STRATEGY strategy);
}
