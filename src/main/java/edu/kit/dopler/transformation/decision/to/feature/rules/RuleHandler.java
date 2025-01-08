package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.Rule;

import java.util.List;

/**
 * This interface is responsible for translating {@link Rule}s inside a {@link Dopler} model to constraints inside a
 * {@link FeatureModel}.
 */
public interface RuleHandler {

    /**
     * Converts the given {@link Rule}s to {@link Constraint}s and adds them to the given {@link FeatureModel}.
     *
     * @param featureModel {@link FeatureModel} to which the {@link Constraint}s are added
     * @param allRules     {@link Rule}s to convert
     */
    void handleRules(FeatureModel featureModel, List<Rule> allRules);
}
