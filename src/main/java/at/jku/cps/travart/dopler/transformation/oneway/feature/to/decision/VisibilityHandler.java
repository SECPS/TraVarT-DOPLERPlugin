package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.MyUtil;
import at.jku.cps.travart.dopler.transformation.util.UnexpectedTypeException;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

import java.util.Optional;

class VisibilityHandler {

    /**
     * Resolves the visibility of the given decision. The visibility depends on the parents of the group from which the
     * decision originates.
     */
    ICondition resolveVisibility(FeatureModel featureModel, IDecisionModel decisionModel, Feature feature) {

        //Search for first non-mandatory parent
        Optional<Feature> parent = MyUtil.findFirstNonMandatoryParent(featureModel, feature);

        //Parent is root. Decision for root feature is always taken. Visibility must therefore be true.
        if (parent.isEmpty()) {
            return BooleanValue.getTrue();
        }

        //Parent is not root. Look at parent group of parent
        return switch (parent.get().getParentGroup().GROUPTYPE) {
            case OPTIONAL -> handleBooleanDecision(parent.get());
            case ALTERNATIVE -> handleAlternative(parent.get(), decisionModel);
            case OR -> handleOr(parent.get(), decisionModel);
            case GROUP_CARDINALITY, MANDATORY -> throw new UnexpectedTypeException(parent.get().getParentGroup());
        };
    }

    /** Returns {@code decision.value}, because you can choose several enums from the decision */
    private ICondition handleOr(Feature parent, IDecisionModel decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        Optional<IDecision<?>> parentOfParentDecision =
                MyUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return BooleanValue.getTrue();
        }

        IDecision<?> decision = parentOfParentDecision.get();
        AbstractRangeValue<String> value = new StringValue(parent.getFeatureName());
        return new DecisionValueCondition(decision, value);
    }

    /** Returns {@code getValue(decision) = value}, because you can only choose one enum from the decision */
    private ICondition handleAlternative(Feature parent, IDecisionModel decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        Optional<IDecision<?>> parentOfParentDecision =
                MyUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());
        if (parentOfParentDecision.isEmpty()) {
            return BooleanValue.getTrue();
        }

        ICondition left = new GetValueFunction(new StringDecision(parentOfParent.getFeatureName()));
        ICondition right =
                new DecisionValueCondition(parentOfParentDecision.get(), new StringValue(parent.getFeatureName()));

        return new Equals(left, right);
    }

    /** Returns {@code decision}, because you can only choose one enum from the decision */
    private static ICondition handleBooleanDecision(Feature parent) {
        return new StringValue(parent.getFeatureName());
    }
}
