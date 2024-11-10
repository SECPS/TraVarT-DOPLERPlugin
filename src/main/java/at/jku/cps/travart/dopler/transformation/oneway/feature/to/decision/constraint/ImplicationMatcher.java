package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;

import java.util.Optional;

public class ImplicationMatcher extends Matcher<ImplicationConstraint> {

    private static final String NOT_PRESENT_MESSAGE = "One should be present";

    @Override
    public void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                             FeatureModel featureModel, ImplicationConstraint constraint) {
        //Left side
        ICondition condition = getCondition(decisionModel, constraint);

        //Right side
        IAction action = getAction(decisionModel, constraint);

        //Add the constraint to the left decision
        getDecision(decisionModel, constraint).addRule(new Rule(condition, action));
    }

    private static IDecision<?> getDecision(IDecisionModel decisionModel, ImplicationConstraint constraint) {

        if (constraint.getLeft() instanceof LiteralConstraint) {
            LiteralConstraint left = (LiteralConstraint) constraint.getLeft();
            Optional<IDecision<?>> decisionByIdLeft = DMUtil.findDecisionById(decisionModel, left.getLiteral());
            Optional<IDecision<?>> decisionByValueLeft = DMUtil.findDecisionByValue(decisionModel, left.getLiteral());

            if (decisionByValueLeft.isEmpty() && decisionByIdLeft.isEmpty()) {
                throw new RuntimeException(NOT_PRESENT_MESSAGE);
            }

            return decisionByValueLeft.or(() -> decisionByIdLeft).get();
        } else if (constraint.getLeft() instanceof NotConstraint &&
                ((NotConstraint) constraint.getLeft()).getContent() instanceof LiteralConstraint) {
            LiteralConstraint left = (LiteralConstraint) ((NotConstraint) constraint.getLeft()).getContent();
            Optional<IDecision<?>> decisionByIdLeft = DMUtil.findDecisionById(decisionModel, left.getLiteral());
            Optional<IDecision<?>> decisionByValueLeft = DMUtil.findDecisionByValue(decisionModel, left.getLiteral());

            if (decisionByValueLeft.isEmpty() && decisionByIdLeft.isEmpty()) {
                throw new RuntimeException(NOT_PRESENT_MESSAGE);
            }

            return decisionByValueLeft.or(() -> decisionByIdLeft).get();
        } else {
            throw new RuntimeException("Constraint on the left should be NotConstraint of LiteralConstraint");
        }
    }

    private static ICondition getCondition(IDecisionModel decisionModel, ImplicationConstraint constraint) {
        LiteralConstraint left = (LiteralConstraint) constraint.getLeft();
        Optional<IDecision<?>> decisionByIdLeft = DMUtil.findDecisionById(decisionModel, left.getLiteral());
        Optional<IDecision<?>> decisionByValueLeft = DMUtil.findDecisionByValue(decisionModel, left.getLiteral());

        ICondition condition;
        if (decisionByValueLeft.isPresent()) {
            condition = new DecisionValueCondition(decisionByValueLeft.get(), new StringValue(left.getLiteral()));
        } else if (decisionByIdLeft.isPresent()) {
            condition = new StringValue(left.getLiteral());
        } else {
            throw new RuntimeException(NOT_PRESENT_MESSAGE);
        }
        return condition;
    }

    private static IAction getAction(IDecisionModel decisionModel, ImplicationConstraint constraint) {
        LiteralConstraint right = (LiteralConstraint) constraint.getRight();
        Optional<IDecision<?>> decisionByIdRight = DMUtil.findDecisionById(decisionModel, right.getLiteral());
        Optional<IDecision<?>> decisionByValueRight = DMUtil.findDecisionByValue(decisionModel, right.getLiteral());

        IAction action;
        if (decisionByValueRight.isPresent()) {
            action = new SetValueAction(decisionByValueRight.get(), new StringValue(right.getLiteral()));
        } else if (decisionByIdRight.isPresent()) {
            //Boolean types get a true on the right side
            action = AbstractDecision.DecisionType.BOOLEAN == decisionByIdRight.get().getType() ?
                    new SetValueAction(decisionByIdRight.get(), BooleanValue.getTrue()) :
                    new SetValueAction(decisionByIdRight.get(), new StringValue(right.getLiteral()));
        } else {
            throw new RuntimeException(NOT_PRESENT_MESSAGE);
        }
        return action;
    }
}
