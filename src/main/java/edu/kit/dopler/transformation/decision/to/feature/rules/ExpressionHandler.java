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
package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.FeatureModel;
import de.vill.model.expression.Expression;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IExpression;

import java.util.Optional;

/**
 * This interface is responsible for translating {@link IExpression}s in the {@link Dopler} model to {@link Expression}s
 * in the {@link FeatureModel}.
 */
public interface ExpressionHandler {

    /**
     * Translate an {@link IExpression} from a {@link Dopler} model to an {@link Expression} in the
     * {@link FeatureModel}.
     *
     * @param expression {@link IExpression} to translate
     *
     * @return Optional containing the translated {@link Expression}, if the translation was possible
     */
    Optional<Expression> handleExpression(IExpression expression);
}
