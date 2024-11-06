package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DecisionModelUtil;
import at.jku.cps.travart.dopler.transformation.util.Pair;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds the constraints from the feature model to the decision model.
 */
class ConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel = null;

    /**
     * Temporary variable to save feature decision model
     */
    private FeatureModel featureModel = null;

    final void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;
        this.featureModel = featureModel;

        List<Constraint> simplifiedConstraints = new ArrayList<>();
        for (Constraint constraint : featureModel.getConstraints()) {
            List<Constraint> temp = simplifyConstraint(constraint);
            simplifiedConstraints.addAll(temp);
        }

        for (Constraint constraint : simplifiedConstraints) {

            Optional<Pair<LiteralConstraint>> optional = isSimpleImplication(constraint);
            optional.ifPresent(literalConstraintPair -> handleSimpleImplication(decisionModel, literalConstraintPair));
        }
    }

    private static void handleSimpleImplication(IDecisionModel decisionModel,
                                                Pair<LiteralConstraint> literalConstraintPair) {
        LiteralConstraint left = literalConstraintPair.getFirst();
        LiteralConstraint right = literalConstraintPair.getSecond();

        //Left side
        Optional<IDecision<?>> decisionLeft =
                DecisionModelUtil.findDecisionById(decisionModel, left.getFeature().getFeatureName());
        Optional<IDecision<?>> valueLeft = DecisionModelUtil.findDecisionByValue(decisionModel, left.getLiteral());

        ICondition condition;
        if (decisionLeft.isPresent()) {
            condition = new StringValue(left.getLiteral());
        } else {
            condition = new DecisionValueCondition(valueLeft.get(), new StringValue(left.getLiteral()));
        }

        //Right side
        Optional<IDecision<?>> decisionRight =
                DecisionModelUtil.findDecisionById(decisionModel, right.getFeature().getFeatureName());
        Optional<IDecision<?>> valueRight = DecisionModelUtil.findDecisionByValue(decisionModel, right.getLiteral());
        IAction action;
        if (decisionRight.isPresent()) {

            if (AbstractDecision.DecisionType.BOOLEAN == decisionRight.get().getType()) {
                action = new SetValueAction(decisionRight.get(), BooleanValue.getTrue());
            } else {
                action = new SetValueAction(decisionRight.get(), new StringValue(right.getLiteral()));
            }
        } else {
            action = new SetValueAction(valueRight.get(), new StringValue(right.getLiteral()));
        }

        //Add the constraint to the left decision
        decisionLeft.or(() -> valueLeft).get().addRule(new Rule(condition, action));
    }

    private Optional<Pair<LiteralConstraint>> isSimpleImplication(Constraint constraint) {

        Constraint innerConstraint = constraint;

        if(constraint instanceof ParenthesisConstraint){
            innerConstraint = ((ParenthesisConstraint)constraint).getContent();
        }

        if (innerConstraint instanceof ImplicationConstraint) {

            ImplicationConstraint implicationConstraint = (ImplicationConstraint) innerConstraint;

            Constraint left = implicationConstraint.getLeft();
            Constraint right = implicationConstraint.getRight();

            if (left instanceof LiteralConstraint && right instanceof LiteralConstraint) {
                return Optional.of(new Pair<>((LiteralConstraint) left, (LiteralConstraint) right));
            }
        }

        return Optional.empty();
    }

    /** Decomposes AND constraints constraint into several simpler ones. */
    private List<Constraint> simplifyConstraint(Constraint constraint) {

        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            return List.of(andConstraint.getLeft(), andConstraint.getRight());
        }

        return List.of(constraint);
    }

    private void handleImplicationConstraint(ImplicationConstraint ownConstraint) {
        LiteralConstraint left = (LiteralConstraint) ownConstraint.getLeft();
        LiteralConstraint right = (LiteralConstraint) ownConstraint.getRight();

        Feature leftFeature = left.getFeature();
        Feature rightFeature = right.getFeature();

        //Find decision depending on left feature
        Optional<IDecision<?>> first = decisionModel.getDecisions().stream()
                .filter(iDecision -> iDecision.getId().equals(leftFeature.getParentFeature().getFeatureName()))
                .findFirst();

        System.out.println();
    }
}
