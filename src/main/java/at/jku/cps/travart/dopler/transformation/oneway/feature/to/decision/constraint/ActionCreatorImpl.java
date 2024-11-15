package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
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

    private static IAction handleLiteral(IDecisionModel decisionModel, LiteralConstraint right) {
        IAction action;
        String literal = right.getLiteral();
        Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            action = new SetValueAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            //Boolean types get a true on the right side
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

    private static IAction handleNot(IDecisionModel decisionModel, NotConstraint right) {
        IAction action;

        //NotConstraint should contain a literal (thanks to working on DNFs)
        if (!(right.getContent() instanceof LiteralConstraint literalConstraint)) {
            throw new UnexpectedTypeException(right.getContent());
        }

        String literal = literalConstraint.getLiteral();
        Optional<IDecision<?>> decisionById = DMUtil.findDecisionById(decisionModel, literal);
        Optional<IDecision<?>> decisionByValue = DMUtil.findDecisionByValue(decisionModel, literal);

        if (decisionByValue.isPresent()) {
            action = new DisAllowAction(decisionByValue.get(), new StringValue(literal));
        } else if (decisionById.isPresent()) {
            action = switch (decisionById.get().getType()) {
                case BOOLEAN -> new SetValueAction(decisionById.get(), BooleanValue.getFalse());
                case ENUM -> throw new RuntimeException("Cant disallow complete decisions atm");
                case null, default -> throw new UnexpectedTypeException(decisionById.get().getType());

                //TODO
                //Problem in branch ENUM: there is no action to disallow taking enum decisions. Only boolean decisions.
                //Should look something like:
                //action = new DisallowAction(decisionById.get())
            };
        } else {
            throw new DecisionNotPresentException(literal);
        }
        return action;
    }
}
