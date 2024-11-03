package at.jku.cps.travart.dopler.transformation;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IEnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.BooleanValue;
import at.jku.cps.travart.dopler.decision.model.impl.StringValue;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;

import java.util.Optional;

/** Set of utility methods for editing decision models */
public final class DecisionModelUtil {

    private DecisionModelUtil() {
    }

    public static Optional<IDecision<?>> findDecisionById(String id, IDecisionModel decisionModel) {
        return decisionModel.getDecisions().stream().filter(iDecision -> iDecision.getId().equals(id)).findFirst();
    }

    public static void addToDecisionModel(IDecision<?> iDecision, IDecisionModel decisionModel) {
        if (findDecisionById(iDecision.getId(), decisionModel).isPresent()) {
            iDecision.setId(iDecision.getId() + "Type");
        }
        decisionModel.add(iDecision);
    }

    public static void resolveVisibilityBooleanDecision(IDecision<?> decision, Feature feature,
                                                        FeatureModel featureModel) {
        Feature parentFeature = feature.getParentFeature();
        ICondition visibility;
        if (featureModel.getRootFeature().equals(parentFeature)) {
            visibility = BooleanValue.getTrue();
        } else {
            visibility = new StringValue(parentFeature.getFeatureName());
        }

        decision.setVisibility(visibility);
    }

    public static void resolveVisibilityForEnumDecision(IEnumerationDecision<String> decision, Feature parentFeature,
                                                        FeatureModel featureModel) {
        ICondition visibility;
        if (featureModel.getRootFeature().equals(parentFeature)) {
            visibility = BooleanValue.getTrue();
        } else {
            visibility = new StringValue(parentFeature.getFeatureName());
        }
        decision.setVisibility(visibility);
    }
}
