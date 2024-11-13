package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.*;

import java.util.Optional;

class ConditionCreatorImpl implements ConditionCreator {

    @Override
    public ICondition createCondition(IDecisionModel decisionModel, Constraint left) {
        ICondition condition;
        if (left instanceof NotConstraint) {
            condition = handleNot(decisionModel, (NotConstraint) left);
        } else if (left instanceof LiteralConstraint) {
            condition = handleLiteral(decisionModel, (LiteralConstraint) left);
        } else if (left instanceof OrConstraint) {
            condition = handleOr(decisionModel, (OrConstraint) left);
        } else if (left instanceof AndConstraint) {
            condition = handleAnd(decisionModel, (AndConstraint) left);
        } else {
            throw new UnexpectedTypeException(left);
        }
        return condition;
    }

    private ICondition handleNot(IDecisionModel decisionModel, NotConstraint left) {
        return new Not(createCondition(decisionModel, left.getContent()));
    }

    private ICondition handleAnd(IDecisionModel decisionModel, AndConstraint left) {
        return new And(createCondition(decisionModel, left.getLeft()), createCondition(decisionModel, left.getRight()));
    }

    private ICondition handleOr(IDecisionModel decisionModel, OrConstraint left) {
        return new Or(createCondition(decisionModel, left.getLeft()), createCondition(decisionModel, left.getRight()));
    }

    private static ICondition handleLiteral(IDecisionModel decisionModel, LiteralConstraint left) {
        ICondition condition;
        String literal = left.getLiteral();
        Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

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
