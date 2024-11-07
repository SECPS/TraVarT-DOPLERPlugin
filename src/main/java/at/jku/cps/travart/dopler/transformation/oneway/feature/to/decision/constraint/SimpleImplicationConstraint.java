package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DecisionModelUtil;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Optional;

public class SimpleImplicationConstraint extends Matcher<ImplicationConstraint> {

    private static final String NOT_PRESENT_MESSAGE = "One should be present";

    @Override
    public void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                             FeatureModel featureModel, ImplicationConstraint constraint) {
        //Left side
        LiteralConstraint left = (LiteralConstraint) constraint.getLeft();
        Optional<IDecision<?>> decisionByIdLeft = DecisionModelUtil.findDecisionById(decisionModel, left.getLiteral());
        Optional<IDecision<?>> decisionByValueLeft =
                DecisionModelUtil.findDecisionByValue(decisionModel, left.getLiteral());

        ICondition condition;
        if (decisionByValueLeft.isPresent()) {
            condition = new DecisionValueCondition(decisionByValueLeft.get(), new StringValue(left.getLiteral()));
        } else if (decisionByIdLeft.isPresent()) {
            condition = new StringValue(left.getLiteral());
        } else {
            throw new RuntimeException(NOT_PRESENT_MESSAGE);
        }
        IDecision<?> leftDecision = decisionByValueLeft.or(() -> decisionByIdLeft).get();

        //Right side
        LiteralConstraint right = (LiteralConstraint) constraint.getRight();
        Optional<IDecision<?>> decisionByIdRight =
                DecisionModelUtil.findDecisionById(decisionModel, right.getLiteral());
        Optional<IDecision<?>> decisionByValueRight =
                DecisionModelUtil.findDecisionByValue(decisionModel, right.getLiteral());

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

        //Add the constraint to the left decision
        leftDecision.addRule(new Rule(condition, action));
    }

    @Override
    protected boolean isMatching(Constraint constraint) {
        if (constraint instanceof ImplicationConstraint) {
            ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
            boolean leftIsLiteral = implicationConstraint.getLeft() instanceof LiteralConstraint;
            boolean rightIsLiteral = implicationConstraint.getRight() instanceof LiteralConstraint;
            return leftIsLiteral && rightIsLiteral;
        }
        return false;
    }

    @Override
    Class<ImplicationConstraint> getConstraintClass() {
        return ImplicationConstraint.class;
    }
}
