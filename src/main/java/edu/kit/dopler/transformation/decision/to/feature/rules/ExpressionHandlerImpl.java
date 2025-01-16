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

import de.vill.model.expression.Expression;
import de.vill.model.expression.StringExpression;
import edu.kit.dopler.model.DecisionValueCallExpression;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.LiteralExpression;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

/** Implementation of {@link ExpressionHandler}. */
public class ExpressionHandlerImpl implements ExpressionHandler {

    @Override
    public Optional<Expression> handleExpression(IExpression expression) {
        return Optional.of(switch (expression) {
            case LiteralExpression literalExpression -> new StringExpression(literalExpression.toString());
            case DecisionValueCallExpression decisionValueCallExpression ->
                    new StringExpression(decisionValueCallExpression.getValue().getValue().toString());
            default -> throw new UnexpectedTypeException(expression);
        });
    }
}
