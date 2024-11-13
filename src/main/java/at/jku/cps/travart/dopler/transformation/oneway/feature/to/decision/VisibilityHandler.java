package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import at.jku.cps.travart.dopler.transformation.util.DMUtil;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.Optional;

class VisibilityHandler {

    /**
     * Resolves the visibility of the given decision. The visibility depends on the parents of the group from which the
     * decision originates.
     */
    ICondition resolveVisibility(FeatureModel featureModel, Feature feature, IDecisionModel decisionModel) {

        //Search for non-mandatory parent
        Feature parent = feature;
        while (null != parent.getParentGroup() && Group.GroupType.MANDATORY == parent.getParentGroup().GROUPTYPE) {
            parent = parent.getParentFeature();
        }

        //Parent is root
        boolean isRootFeature = featureModel.getRootFeature().equals(parent);
        if (isRootFeature) {
            //Decision for root feature is always taken. Visibility must therefore be true.
            return BooleanValue.getTrue();
        }

        //Parent is not root. Look at parent group of parent
        switch (parent.getParentGroup().GROUPTYPE) {
            case OR:
                throw new RuntimeException(
                        "Komplizierter als 'alternative', da mehrere möglichkeiten gewählt werden dürfen");
            case OPTIONAL:
                return handleBooleanDecision(parent);
            case ALTERNATIVE:
            case MANDATORY:
                return handleEnumDecision(parent, decisionModel);
            case GROUP_CARDINALITY:
                throw new IllegalStateException("Unexpected value: " + parent.getParentGroup().GROUPTYPE);
            default:
                throw new IllegalStateException("Unexpected value: " + parent.getParentGroup().GROUPTYPE);
        }
    }

    private ICondition handleEnumDecision(Feature parent, IDecisionModel decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        //getValue(parentOfParent) == parentOfParent.parent
        Optional<IDecision<?>> parentOfParentDecision =
                DMUtil.findDecisionById(decisionModel, parentOfParent.getFeatureName());

        if (parentOfParentDecision.isEmpty()) {
            throw new RuntimeException("Decision should exist");
        }

        ICondition left = new GetValueFunction(new StringDecision(parentOfParent.getFeatureName()));
        ICondition right =
                new DecisionValueCondition(parentOfParentDecision.get(), new StringValue(parent.getFeatureName()));

        return new Equals(left, right);
    }

    private static ICondition handleBooleanDecision(Feature parent) {
        return new StringValue(parent.getFeatureName());
    }
}
