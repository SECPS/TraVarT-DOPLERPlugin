package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DecisionModelUtil;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Optional;

public class SimpleImplicationConstraint extends Matcher<ImplicationConstraint> {

    @Override
    public void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                             FeatureModel featureModel, ImplicationConstraint constraint) {

        //Only for simple implicationConstraints
        if (!(constraint.getLeft() instanceof LiteralConstraint &&
                constraint.getRight() instanceof LiteralConstraint)) {
            return;
        }

        LiteralConstraint left = (LiteralConstraint) constraint.getLeft();
        LiteralConstraint right = (LiteralConstraint) constraint.getRight();

        //Left side
        Optional<IDecision<?>> decisionLeft =
                DecisionModelUtil.findDecisionById(decisionModel, left.getFeature().getFeatureName());
        Optional<IDecision<?>> valueLeft = DecisionModelUtil.findDecisionByValue(decisionModel, left.getLiteral());

        ICondition condition;
        if (valueLeft.isPresent()) {
            condition = new DecisionValueCondition(valueLeft.get(), new StringValue(left.getLiteral()));
        } else {
            condition = new StringValue(left.getLiteral());
        }

        //Right side
        Optional<IDecision<?>> decisionRight =
                DecisionModelUtil.findDecisionById(decisionModel, right.getFeature().getFeatureName());
        Optional<IDecision<?>> valueRight = DecisionModelUtil.findDecisionByValue(decisionModel, right.getLiteral());
        IAction action;
        if (valueRight.isPresent()) {
            action = new SetValueAction(valueRight.get(), new StringValue(right.getLiteral()));
        } else {
            if (AbstractDecision.DecisionType.BOOLEAN == decisionRight.get().getType()) {
                action = new SetValueAction(decisionRight.get(), BooleanValue.getTrue());
            } else {
                action = new SetValueAction(decisionRight.get(), new StringValue(right.getLiteral()));
            }
        }

        //Add the constraint to the left decision
        valueLeft.or(() -> decisionLeft).get().addRule(new Rule(condition, action));
    }

    @Override
    Class<ImplicationConstraint> getConstraintClass() {
        return ImplicationConstraint.class;
    }
}
