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

/** This interface is responsible for checking if a config of a {@link Dopler} model is valid. */
public interface ConfigVerifier {

    /**
     * Checks of the given configuration is a valid configuration of the given {@link Dopler} model
     *
     * @param dopler {@link Dopler} model to check the configuration against
     * @param map    Configuration to check for validity
     *
     * @return true if the configuration is valid, false otherwise
     */
    boolean verify(Dopler dopler, Map<IConfigurable, Boolean> map);
}
