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
package edu.kit.travart.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.GreaterEquationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;
import de.vill.model.expression.Expression;
import edu.kit.dopler.model.AND;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.DecisionValueCallExpression;
import edu.kit.dopler.model.EnumeratorLiteralExpression;
import edu.kit.dopler.model.Equals;
import edu.kit.dopler.model.GreatherThan;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.NOT;
import edu.kit.dopler.model.OR;
import edu.kit.travart.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.travart.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

/** Implementation of {@link ConditionHandler}. */
public class ConditionHandlerImpl implements ConditionHandler {

    private final ExpressionHandler expressionHandler;

    /**
     * Constructor of {@link ConditionHandlerImpl}.
     *
     * @param expressionHandler {@link ExpressionHandler}
     */
    public ConditionHandlerImpl(ExpressionHandler expressionHandler) {
        this.expressionHandler = expressionHandler;
    }

    private static Optional<Constraint> handleEquals(Equals equals) {
        //TODO: There are probably a lot of other cases here.

        //Covers 'getValue(someDecision) = ...'
        if (equals.getLeftExpression() instanceof DecisionValueCallExpression decisionValueCallExpression) {

            // Covers: 'getValue(someDecision) = true' or 'getValue(someDecision) = false'
            if (equals.getRightExpression() instanceof BooleanLiteralExpression booleanLiteralExpression) {
                String decisionId = decisionValueCallExpression.getDecision().getDisplayId();
                if (booleanLiteralExpression.getLiteral()) {
                    return Optional.of(new LiteralConstraint(decisionId));
                } else {
                    return Optional.of(new NotConstraint(new LiteralConstraint(decisionId)));
                }
            }

            // Covers: 'getValue(someDecision) = someEnum'
            if (equals.getRightExpression() instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                return Optional.of(new LiteralConstraint(enumeratorLiteralExpression.toString()));
            }
        }

        throw new CanNotBeTranslatedException(equals);
    }

    private static Optional<Constraint> handleBooleanLiteralExpression(
            BooleanLiteralExpression booleanLiteralExpression) {
        if (booleanLiteralExpression.getLiteral()) {
            //Value is TRUE
            return Optional.empty();
        } else {
            //Value is FALSE
            throw new UnexpectedTypeException(booleanLiteralExpression);
        }
    }

    @Override
    public Optional<Constraint> handleCondition(IExpression condition) {
        //TODO: A lot of cases a missing here.
        return switch (condition) {
            case NOT not -> handleNot(not);
            case BooleanLiteralExpression booleanLiteralExpression ->
                    handleBooleanLiteralExpression(booleanLiteralExpression);
            case Equals equals -> handleEquals(equals);
            case AND and -> handleAnd(and);
            case OR or -> handleOr(or);
            case GreatherThan greatherThan -> handleGreaterThen(greatherThan);
            case null, default -> throw new UnexpectedTypeException(condition);
        };
    }

    private Optional<Constraint> handleGreaterThen(GreatherThan greatherThan) {
        Optional<Expression> left = expressionHandler.handleExpression(greatherThan.getLeftExpression());
        Optional<Expression> right = expressionHandler.handleExpression(greatherThan.getRightExpression());

        if (right.isPresent() && left.isPresent()) {
            return Optional.of(new ParenthesisConstraint(new GreaterEquationConstraint(left.get(), right.get())));
        }

        return Optional.empty();
    }

    private Optional<Constraint> handleOr(OR or) {
        Optional<Constraint> left = handleCondition(or.getLeftExpression());
        Optional<Constraint> right = handleCondition(or.getRightExpression());

        if (right.isPresent() && left.isPresent()) {
            return Optional.of(new ParenthesisConstraint(new OrConstraint(left.get(), right.get())));
        }

        return Optional.empty();
    }

    private Optional<Constraint> handleAnd(AND and) {
        Optional<Constraint> left = handleCondition(and.getLeftExpression());
        Optional<Constraint> right = handleCondition(and.getRightExpression());

        if (right.isPresent() && left.isPresent()) {
            return Optional.of(new ParenthesisConstraint(new AndConstraint(left.get(), right.get())));
        }

        return Optional.empty();
    }

    private Optional<Constraint> handleNot(NOT not) {
        return handleCondition(not.getOperand()).map(NotConstraint::new);
    }
}
