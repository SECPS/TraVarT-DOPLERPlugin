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
import edu.kit.dopler.model.Dopler;

import java.util.Map;
import java.util.Set;

/** This interface is responsible for finding invalid configurations of a {@link Dopler} model. */
public interface InvalidConfigFinder {

    /**
     * Searches for invalid configurations of the given {@link Dopler} model.
     *
     * @param dopler             {@link Dopler} model for which invalid configurations are searched
     * @param maxAmountOfConfigs Maximal number of configurations that are searched
     *
     * @return Set of configurations. A single configuration is represented by a map of {@link IConfigurable}s as keys
     * and {@link Boolean}s as values.
     */
    Set<Map<IConfigurable, Boolean>> findInvalidConfigs(Dopler dopler, long maxAmountOfConfigs);
}
