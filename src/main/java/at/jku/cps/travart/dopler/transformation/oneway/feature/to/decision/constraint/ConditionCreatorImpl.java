package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;

import java.util.Optional;

class ConditionCreatorImpl implements ConditionCreator {

    @Override
    public ICondition createCondition(IDecisionModel decisionModel, FeatureModel featureModel, Constraint left) {
        return switch (left) {
            case NotConstraint notConstraint -> handleNot(decisionModel, featureModel, notConstraint);
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, featureModel, literalConstraint);
            case OrConstraint orConstraint -> handleOr(decisionModel, featureModel, orConstraint);
            case AndConstraint andConstraint -> handleAnd(decisionModel, featureModel, andConstraint);
            case null, default -> throw new UnexpectedTypeException(left);
        };
    }

    private ICondition handleNot(IDecisionModel decisionModel, FeatureModel featureModel, NotConstraint left) {
        return new Not(createCondition(decisionModel, featureModel, left.getContent()));
    }

    private ICondition handleAnd(IDecisionModel decisionModel, FeatureModel featureModel, AndConstraint left) {
        return new And(createCondition(decisionModel, featureModel, left.getLeft()),
                createCondition(decisionModel, featureModel, left.getRight()));
    }

    private ICondition handleOr(IDecisionModel decisionModel, FeatureModel featureModel, OrConstraint left) {
        return new Or(createCondition(decisionModel, featureModel, left.getLeft()),
                createCondition(decisionModel, featureModel, left.getRight()));
    }

    private static ICondition handleLiteral(IDecisionModel decisionModel, FeatureModel featureModel,
                                            LiteralConstraint literalConstraint) {

        Optional<LiteralConstraint> replaced = MyUtil.findFirstNonMandatoryParent(featureModel, literalConstraint);
        if(replaced.isEmpty()){
            return BooleanValue.getTrue();
        } else {
            literalConstraint = replaced.get();
        }

        ICondition condition;
        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            condition = new DecisionValueCondition(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            condition = new StringValue(literal);
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return condition;
    }
}
