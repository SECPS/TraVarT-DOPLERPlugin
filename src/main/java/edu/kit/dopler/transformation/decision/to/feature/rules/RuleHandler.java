package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.FeatureModel;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.Rule;

import java.util.List;

public interface RuleHandler {

    void handleRules( FeatureModel featureModel, List<Rule> allRules);
}
