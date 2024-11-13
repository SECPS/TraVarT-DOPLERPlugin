package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.Optional;

class ActionCreatorImpl implements ActionCreator {

    @Override
    public IAction createAction(IDecisionModel decisionModel, Constraint right) {
        return switch (right) {
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, literalConstraint);
            case NotConstraint notConstraint -> handleNot(decisionModel, notConstraint);
            case ExpressionConstraint expressionConstraint -> {
                //TODO
                throw new UnexpectedTypeException(right);
            }
            case null, default -> throw new UnexpectedTypeException(right);
        };
    }

    private static IAction handleNot(IDecisionModel decisionModel, NotConstraint right) {
        IAction action;
        if (!(right.getContent() instanceof LiteralConstraint literalConstraint)) {
            throw new UnexpectedTypeException(right.getContent());
        }

        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionByIdRight = DMUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValueRight = DMUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValueRight.isPresent()) {
            action = new DisAllowAction(decisionByValueRight.get(), new StringValue(literal));
        } else if (decisionByIdRight.isPresent()) {
            //Boolean types get a true on the right side
            action = AbstractDecision.DecisionType.BOOLEAN == decisionByIdRight.get().getType() ?
                    new SetValueAction(decisionByIdRight.get(), BooleanValue.getFalse()) :
                    new DisAllowAction(decisionByIdRight.get(), new StringValue(literal));
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return action;
    }

    private static IAction handleLiteral(IDecisionModel decisionModel, LiteralConstraint right) {
        IAction action;
        String literal = right.getLiteral();
        Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            action = new SetValueAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            //Boolean types get a true on the right side
            boolean isBoolean = AbstractDecision.DecisionType.BOOLEAN == decisionById.get().getType();
            action = new SetValueAction(decisionById.get(),
                    isBoolean ? BooleanValue.getTrue() : new StringValue(literal));
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return action;
    }
}
