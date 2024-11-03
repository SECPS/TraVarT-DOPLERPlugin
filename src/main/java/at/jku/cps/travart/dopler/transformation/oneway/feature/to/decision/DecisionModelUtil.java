package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision;

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
final class DecisionModelUtil {

    private DecisionModelUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(String id, IDecisionModel decisionModel) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    public static void addToDecisionModel(IDecision<?> iDecision, IDecisionModel decisionModel) {

        String id;
        if (findDecisionById(iDecision.getId(), decisionModel).isPresent()) {
            id = iDecision.getId() + "Type";
        } else {
            id = iDecision.getId();
        }
        iDecision.setId(id);

        decisionModel.add(iDecision);
    }

    public static void resolveVisibility(IDecision<?> decision, FeatureModel featureModel, Feature parentFeature) {

        //Search for non-mandatory parent
        Feature importantParent = parentFeature;
        while (null != importantParent.getParentGroup() &&
                Group.GroupType.MANDATORY == importantParent.getParentGroup().GROUPTYPE) {
            importantParent = importantParent.getParentFeature();
        }

        ICondition visibility;
        boolean isRootFeature = featureModel.getRootFeature().equals(importantParent);
        if (isRootFeature) {
            //Decision for root feature is always taken. Visibility must therefore be true.
            visibility = BooleanValue.getTrue();
        } else {
            visibility = new StringValue(importantParent.getFeatureName());
        }

        decision.setVisibility(visibility);
    }
}
