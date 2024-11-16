package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.SetValueAction;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import at.jku.cps.travart.dopler.transformation.util.DecisionNotPresentException;
import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.Optional;

class ActionCreatorImpl implements ActionCreator {

    @Override
    public IAction createAction(IDecisionModel decisionModel, Constraint constraint) {
        return switch (constraint) {
            case LiteralConstraint literalConstraint -> handleLiteral(decisionModel, literalConstraint);
            case NotConstraint notConstraint -> handleNot(decisionModel, notConstraint);
            case ExpressionConstraint expressionConstraint -> {
                //TODO
                throw new UnexpectedTypeException(constraint);
            }
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }

    private IAction handleLiteral(IDecisionModel decisionModel, LiteralConstraint literalConstraint) {
        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        IAction action;
        if (decisionByValue.isPresent()) {
            action = new SetValueAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            action = switch (decisionById.get().getType()) {
                case BOOLEAN -> new SetValueAction(decisionById.get(), BooleanValue.getTrue());
                case ENUM -> new SetValueAction(decisionById.get(), new StringValue(literal));
                case null, default -> throw new UnexpectedTypeException(decisionById.get().getType());
            };
        } else {
            throw new DecisionNotPresentException(literal);
        }

        return action;
    }

    private IAction handleNot(IDecisionModel decisionModel, NotConstraint notConstraint) {

        //NotConstraint should contain a literal (thanks to working on DNFs)
        if (!(notConstraint.getContent() instanceof LiteralConstraint literalConstraint)) {
            throw new UnexpectedTypeException(notConstraint.getContent());
        }

        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = MyUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = MyUtil.findDecisionByValue(decisionModel, literal);

        IAction action;
        if (decisionByValue.isPresent()) {
            action = new DisAllowAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            action = switch (decisionById.get().getType()) {
                case BOOLEAN -> new SetValueAction(decisionById.get(), BooleanValue.getFalse());
                case null, default -> throw new UnexpectedTypeException(decisionById.get().getType());
            };
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return action;
    }
}
