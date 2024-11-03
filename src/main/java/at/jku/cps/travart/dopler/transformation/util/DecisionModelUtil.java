package at.jku.cps.travart.dopler.transformation.util;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.Optional;

/** Set of utility methods for editing decision models */
public final class DecisionModelUtil {

    private DecisionModelUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(String id, IDecisionModel decisionModel) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    /**
     * Resolves the id of the given decision. The id changes, if a decision with this name already exists.
     */
    public static String resolveId(IDecisionModel decisionModel, Feature feature) {

        String id;
        if (findDecisionById(feature.getFeatureName(), decisionModel).isPresent()) {
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
    public static ICondition resolveVisibility(FeatureModel featureModel, Feature feature) {

        //Search for non-mandatory parent
        Feature parent = feature;
        while (null != parent.getParentGroup() && Group.GroupType.MANDATORY == parent.getParentGroup().GROUPTYPE) {
            parent = parent.getParentFeature();
        }

        ICondition visibility;
        boolean isRootFeature = featureModel.getRootFeature().equals(parent);
        if (isRootFeature) {
            //Decision for root feature is always taken. Visibility must therefore be true.
            visibility = BooleanValue.getTrue();
        } else {
            visibility = new StringValue(parent.getFeatureName());
        }

        return visibility;
    }
}
