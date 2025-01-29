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
package edu.kit.travart.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.EqualEquationConstraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.GreaterEqualsEquationConstraint;
import de.vill.model.constraint.GreaterEquationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.LowerEqualsEquationConstraint;
import de.vill.model.constraint.LowerEquationConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.NotEqualsEquationConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.expression.Expression;
import de.vill.model.expression.LiteralExpression;
import de.vill.model.expression.NumberExpression;
import de.vill.model.expression.StringExpression;
import edu.kit.dopler.model.AND;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.DoubleLiteralExpression;
import edu.kit.dopler.model.Equals;
import edu.kit.dopler.model.GreatherThan;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.LessThan;
import edu.kit.dopler.model.NOT;
import edu.kit.dopler.model.OR;
import edu.kit.dopler.model.StringLiteralExpression;
import edu.kit.travart.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.travart.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.travart.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.travart.dopler.transformation.util.DecisionFinder;
import edu.kit.travart.dopler.transformation.util.FeatureFinder;

import java.util.Optional;

/** Implementation of ConditionCreator */
public class ConditionCreatorImpl implements ConditionCreator {

    private final DecisionFinder decisionFinder;
    private final FeatureFinder featureFinder;

    /**
     * Constructor of {@link ConditionCreatorImpl}.
     *
     * @param decisionFinder {@link DecisionFinder}
     * @param featureFinder  {@link FeatureFinder}
     */
    public ConditionCreatorImpl(DecisionFinder decisionFinder, FeatureFinder featureFinder) {
        this.decisionFinder = decisionFinder;
        this.featureFinder = featureFinder;
    }

    @Override
    public IExpression createCondition(Dopler decisionModel, FeatureModel featureModel, Constraint constraint) {
        return switch (constraint) {
            case NotConstraint notConstraint -> handleNot(decisionModel, featureModel, notConstraint);
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, featureModel, literalConstraint);
            case OrConstraint orConstraint -> handleOr(decisionModel, featureModel, orConstraint);
            case AndConstraint andConstraint -> handleAnd(decisionModel, featureModel, andConstraint);
            case ExpressionConstraint expressionConstraint -> handleExpressionConstraint(expressionConstraint);
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }

    private IExpression handleNot(Dopler decisionModel, FeatureModel featureModel, NotConstraint left) {
        return new NOT(createCondition(decisionModel, featureModel, left.getContent()));
    }

    private IExpression handleAnd(Dopler decisionModel, FeatureModel featureModel, AndConstraint left) {
        return new AND(createCondition(decisionModel, featureModel, left.getLeft()),
                createCondition(decisionModel, featureModel, left.getRight()));
    }

    private IExpression handleOr(Dopler decisionModel, FeatureModel featureModel, OrConstraint left) {
        return new OR(createCondition(decisionModel, featureModel, left.getLeft()),
                createCondition(decisionModel, featureModel, left.getRight()));
    }

    private IExpression handleLiteral(Dopler decisionModel, FeatureModel featureModel,
                                      LiteralConstraint literalConstraint) {

        LiteralConstraint firstNonMandatoryParent;
        Optional<LiteralConstraint> replaced =
                featureFinder.findFirstNonMandatoryParent(featureModel, literalConstraint);
        if (replaced.isEmpty()) {
            return new BooleanLiteralExpression(true);
        } else {
            firstNonMandatoryParent = replaced.get();
        }

        IExpression condition;
        String literal = firstNonMandatoryParent.getLiteral();
        Optional<IDecision<?>> decisionById = decisionFinder.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = decisionFinder.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            return new StringLiteralExpression(decisionByValue.get().getDisplayId() + "." + literal);
        } else if (decisionById.isPresent()) {
            condition = new StringLiteralExpression(literal);
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return condition;
    }

    private IExpression handleExpressionConstraint(ExpressionConstraint constraint) {
        return switch (constraint) {
            case EqualEquationConstraint ignored ->
                    new Equals(handleExpression(constraint.getLeft()), handleExpression(constraint.getRight()));
            case NotEqualsEquationConstraint ignored -> new NOT(new Equals(handleExpression(constraint.getLeft()),
                    handleExpression(constraint.getRight())));
            case GreaterEquationConstraint ignored ->
                    new GreatherThan(handleExpression(constraint.getLeft()), handleExpression(constraint.getRight()));
            case GreaterEqualsEquationConstraint ignored ->
                    new OR(new Equals(handleExpression(constraint.getLeft()), handleExpression(constraint.getRight())),
                            new GreatherThan(handleExpression(constraint.getLeft()),
                                    handleExpression(constraint.getRight())));
            case LowerEquationConstraint ignored ->
                    new LessThan(handleExpression(constraint.getLeft()), handleExpression(constraint.getRight()));
            case LowerEqualsEquationConstraint ignored ->
                    new OR(new Equals(handleExpression(constraint.getLeft()), handleExpression(constraint.getRight())),
                            new LessThan(handleExpression(constraint.getLeft()),
                                    handleExpression(constraint.getRight())));
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }

    private IExpression handleExpression(Expression expression) {
        //Ignore constraints that contains complex expressions;
        return switch (expression) {
            case NumberExpression numberExpression -> new DoubleLiteralExpression(numberExpression.getNumber());
            case StringExpression stringExpression -> new StringLiteralExpression(stringExpression.getString());
            case LiteralExpression literalExpression -> new StringLiteralExpression(literalExpression.getFeatureName());
            case null, default -> throw new CanNotBeTranslatedException(expression);
        };
    }
}
