package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.FeatureModel;
import edu.kit.dopler.model.Dopler;

public interface RuleHandler {

    void handleRules(Dopler decisionModel, FeatureModel featureModel);
}
