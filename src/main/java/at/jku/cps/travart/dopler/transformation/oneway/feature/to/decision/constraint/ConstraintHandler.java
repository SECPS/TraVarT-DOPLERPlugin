package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds the constraints from the feature model to the decision model.
 */
public class ConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel = null;

    /**
     * Temporary variable to save feature decision model
     */
    private FeatureModel featureModel = null;

    private final Set<Matcher<?>> matchers;

    public ConstraintHandler() {
        matchers = new HashSet<>();
        matchers.add(new AndMatcher());
        matchers.add(new ParenthesisMatcher());
        matchers.add(new SimpleImplicationConstraint());
    }

    public void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;
        this.featureModel = featureModel;

        for (Constraint constraint : featureModel.getConstraints()) {
            handleConstraint(constraint);
        }
    }

    void handleConstraint(Constraint constraint) {
        for (Matcher<? extends Constraint> matcher : matchers) {
            matcher.match(this, decisionModel, featureModel, constraint);
        }
    }
}
