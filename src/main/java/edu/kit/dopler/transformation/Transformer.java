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
package edu.kit.dopler.transformation;

import at.jku.cps.travart.core.common.IModelTransformer;
import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.transformation.decision.to.feature.DmToFmTransformer;
import edu.kit.dopler.transformation.feature.to.decision.FmToDmTransformer;

/**
 * Transforms {@link Dopler} models into {@link FeatureModel}s and vice versa.
 */
public class Transformer implements IModelTransformer<Dopler> {

    private final DmToFmTransformer dmToFmTransformer;
    private final FmToDmTransformer fmToDmTransformer;

    /**
     * Constructor of {@link Transformer}.
     *
     * @param dmToFmTransformer {@link DmToFmTransformer}
     * @param fmToDmTransformer {@link FmToDmTransformer}
     */
    public Transformer(DmToFmTransformer dmToFmTransformer, FmToDmTransformer fmToDmTransformer) {
        this.dmToFmTransformer = dmToFmTransformer;
        this.fmToDmTransformer = fmToDmTransformer;
    }

    @Override
    public FeatureModel transform(Dopler model, String s, STRATEGY strategy) {
        return dmToFmTransformer.transform(model, strategy);
    }

    @Override
    public Dopler transform(FeatureModel featureModel, String s, STRATEGY strategy) {
        return fmToDmTransformer.transform(featureModel, strategy);
    }
}
