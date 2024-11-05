package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.Optional;

/** Set of utility methods for editing decision models */
public final class DecisionModelUtil {

    private DecisionModelUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(IDecisionModel decisionModel, String id) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    /**
     * Resolves the id of the given decision. The id changes, if a decision with this name already exists.
     */
    public static String resolveId(IDecisionModel decisionModel, Feature feature) {

        String id;
        if (findDecisionById(decisionModel, feature.getFeatureName()).isPresent()) {
            id = feature.getFeatureName() + "Type";
        } else {
            id = feature.getFeatureName();
        }
        return id;
    }

    /**
     * Resolves the visibility of the given decision. The visibility depends on the parents of the group from which the
     * decision originates.
     */
    public static ICondition resolveVisibility(FeatureModel featureModel, Feature feature,
                                               IDecisionModel decisionModel) {

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

    private static ICondition handleEnumDecision(Feature parent, IDecisionModel decisionModel) {
        Feature parentOfParent = parent.getParentFeature();

        //getValue(parentOfParent) == parentOfParent.parent
        Optional<IDecision<?>> parentOfParentDecision =
                findDecisionById(decisionModel, parentOfParent.getFeatureName());

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
