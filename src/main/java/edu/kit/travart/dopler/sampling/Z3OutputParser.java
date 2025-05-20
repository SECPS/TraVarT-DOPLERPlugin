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

import java.util.Map;

/** This interface is responsible for parsing the output of the z3 sat solver. */
public interface Z3OutputParser {

    /**
     * Parse the answer of the z3 sat solver and converts it into a map of {@link IConfigurable}.
     *
     * @param z3Answer Answer from the z3 sat solver that should be parsed
     *
     * @return Map of {@link IConfigurable}s as keys and {@link Boolean}s as values
     */
    Map<IConfigurable, Boolean> parseSatAnswer(String z3Answer);
}
