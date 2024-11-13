package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;

public interface ConstraintHandler {

    void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel);
}
