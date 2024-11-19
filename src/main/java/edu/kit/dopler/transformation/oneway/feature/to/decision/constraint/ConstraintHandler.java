package edu.kit.dopler.transformation.oneway.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

public interface ConstraintHandler {

    void handleOwnConstraints(FeatureModel featureModel, Dopler decisionModel);
}
