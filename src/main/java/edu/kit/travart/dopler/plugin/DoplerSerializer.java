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
package edu.kit.travart.dopler.plugin;

import at.jku.cps.travart.core.common.Format;
import at.jku.cps.travart.core.common.ISerializer;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.io.DecisionModelWriter;
import edu.kit.dopler.model.Dopler;

import java.io.IOException;

/**
 * Implementation of {@link ISerializer} with the {@link Dopler} model as type variable.
 */
public class DoplerSerializer implements ISerializer<Dopler> {

    @Override
    public String serialize(Dopler model) throws NotSupportedVariabilityTypeException {
        try {
            return new DecisionModelWriter().write(model);
        } catch (IOException e) {
            throw new NotSupportedVariabilityTypeException(e);
        }
    }

    @Override
    public Format getFormat() {
        return new CsvFormat();
    }
}
