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

import de.vill.model.Feature;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IDecision;

/** This interface is responsible for creating the identifier of {@link IDecision}s. */
public interface IdHandler {

    /**
     * Creates an identifier for a {@link IDecision} with the given {@link Feature}.
     *
     * @param decisionModel {@link Dopler} model that will contain the {@link IDecision}
     * @param feature       {@link Feature} from which the {@link IDecision} is created
     *
     * @return Identifier of the {@link IDecision}
     */
    String resolveId(Dopler decisionModel, Feature feature);
}
