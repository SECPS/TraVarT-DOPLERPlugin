/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors: 
 *    @author Fabian Eger
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 * All rights reserved
 *******************************************************************************/
package edu.kit.dopler.model;

import edu.kit.dopler.exceptions.EvaluationException;

import java.util.stream.Stream;

public class AND extends BinaryExpression {

    private static final String SYMBOL = "&&";

    public AND(IExpression leftExpression, IExpression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public boolean evaluate() throws EvaluationException {
        switch (getLeftExpression()) {
            case BooleanLiteralExpression booleanLiteralExpression when getRightExpression() instanceof DecisionValueCallExpression -> {
                boolean left = booleanLiteralExpression.getLiteral();
                boolean right = (boolean) ((DecisionValueCallExpression) getRightExpression()).getValue().getValue();
                return left && right;
            }
            case DecisionValueCallExpression decisionValueCallExpression when getRightExpression() instanceof BooleanLiteralExpression -> {
                boolean left = (boolean) decisionValueCallExpression.getValue().getValue();
                boolean right = ((BooleanLiteralExpression) getRightExpression()).getLiteral();
                return left && right;
            }
            case BooleanLiteralExpression booleanLiteralExpression when getRightExpression() instanceof BooleanLiteralExpression -> {
                boolean right = ((BooleanLiteralExpression) getRightExpression()).getLiteral();
                boolean left = booleanLiteralExpression.getLiteral();
                return left && right;
            }
            case BinaryExpression binaryExpression when getRightExpression() instanceof BinaryExpression -> {
                return getLeftExpression().evaluate() && getRightExpression().evaluate();
            }
            case null, default -> throw new EvaluationException("Only Boolean Values Supported");
        }
    }

    /**
     * The boolean AND can be encoded to the SMT Encoding by simply adding (and (leftExpression) (rightExpression))
     *
     * @param builder the stream builder, where the condition is added
     */
    @Override
    public void toSMTStream(Stream.Builder<String> builder, String callingDecision) {
        builder.add("(and ");
        getLeftExpression().toSMTStream(builder, callingDecision);
        getRightExpression().toSMTStream(builder, callingDecision);
        builder.add(")");
    }

    @Override
    public String toString() {
        return String.format("(%s " + SYMBOL + " %s)", getLeftExpression(), getRightExpression());
    }
}
