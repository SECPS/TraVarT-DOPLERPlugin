package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.dopler.transformation.util.MyUtil;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

class ConditionCreatorImpl implements ConditionCreator {

    @Override
    public IExpression createCondition(Dopler decisionModel, FeatureModel featureModel, Constraint left) {
        return switch (left) {
            case NotConstraint notConstraint -> handleNot(decisionModel, featureModel, notConstraint);
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, featureModel, literalConstraint);
            case OrConstraint orConstraint -> handleOr(decisionModel, featureModel, orConstraint);
            case AndConstraint andConstraint -> handleAnd(decisionModel, featureModel, andConstraint);
            case null, default -> throw new UnexpectedTypeException(left);
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

    private static IExpression handleLiteral(Dopler decisionModel, FeatureModel featureModel,
                                             LiteralConstraint literalConstraint) {

        Optional<LiteralConstraint> replaced = MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
        if (replaced.isEmpty()) {
            return new BooleanLiteralExpression(true);
        } else {
            literalConstraint = replaced.get();
        }

        IExpression condition;
        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            return new StringLiteralExpression(decisionByValue.get().getDisplayId() + "." + literal);
        } else if (decisionById.isPresent()) {
            condition = new StringLiteralExpression(literal);
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return condition;
    }
}
