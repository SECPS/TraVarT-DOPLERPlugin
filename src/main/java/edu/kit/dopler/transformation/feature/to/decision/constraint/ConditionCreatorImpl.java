package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;
import de.vill.model.expression.Expression;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.util.DecisionFinder;
import edu.kit.dopler.transformation.util.FeatureFinder;

import java.util.Optional;

/** Implementation of ConditionCreator */
public class ConditionCreatorImpl implements ConditionCreator {

    private final DecisionFinder decisionFinder;
    private final FeatureFinder featureFinder;

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
        //Ignore constraints that contains expression;
        throw new CanNotBeTranslatedException(expression);
    }
}
