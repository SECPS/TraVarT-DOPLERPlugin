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
package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.core.common.ISampler;
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ISampler} with the {@link Dopler} model as type parameter.
 */
public class DoplerSampler implements ISampler<Dopler> {

    private final ValidConfigFinder validConfigFinder;
    private final InvalidConfigFinder invalidConfigFinder;
    private final ConfigVerifier configVerifier;

    /**
     * Constructor of {@link DoplerSampler}.
     *
     * @param validConfigFinder   {@link ValidConfigFinder}
     * @param invalidConfigFinder {@link InvalidConfigFinder}
     * @param configVerifier      {@link ConfigVerifier}
     */
    public DoplerSampler(ValidConfigFinder validConfigFinder, InvalidConfigFinder invalidConfigFinder, ConfigVerifier configVerifier) {
        this.validConfigFinder = validConfigFinder;
        this.invalidConfigFinder = invalidConfigFinder;
        this.configVerifier = configVerifier;
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(Dopler dopler) {
        return validConfigFinder.getValidConfigs(dopler, Long.MAX_VALUE);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(Dopler dopler, long maxAmountOfValidConfigs) {
        return validConfigFinder.getValidConfigs(dopler, maxAmountOfValidConfigs);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(Dopler dopler) {
        return invalidConfigFinder.findInvalidConfigs(dopler, Long.MAX_VALUE);
    }

    @Override
    public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(Dopler dopler, long maxAmountOfInvalidConfigs) {
        return invalidConfigFinder.findInvalidConfigs(dopler, maxAmountOfInvalidConfigs);
    }

    @Override
    public boolean verifySampleAs(Dopler dopler, Map<IConfigurable, Boolean> map) {
        return configVerifier.verify(dopler, map);
    }
}
