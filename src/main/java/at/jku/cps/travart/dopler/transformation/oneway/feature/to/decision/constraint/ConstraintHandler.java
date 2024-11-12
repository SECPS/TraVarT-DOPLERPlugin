package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf.DnfConverter;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the constraints from the feature model to the decision model.
 */
public class ConstraintHandler {

    /**
     * Temporary variable to save current decision model
     */
    private IDecisionModel decisionModel;

    /**
     * Temporary variable to save feature decision model
     */
    private FeatureModel featureModel;

    private final List<Matcher<?>> matchers;

    private final DnfConverter dnfConverter;

    public ConstraintHandler() {
        decisionModel = null;
        featureModel = null;

        matchers = new ArrayList<>();

        //Order is important
        matchers.add(new AndMatcher());
        //matchers.add(new OrMatcher());
        matchers.add(new ParenthesisMatcher());
        matchers.add(new ImplicationMatcher());
        dnfConverter = new DnfConverter();
    }

    public void handleOwnConstraints(FeatureModel featureModel, IDecisionModel decisionModel) {
        this.decisionModel = decisionModel;
        this.featureModel = featureModel;

        for (Constraint constraint : featureModel.getConstraints()) {
            dnfConverter.convertToDnf(constraint);

        }
    }

    void handleConstraint(Constraint constraint) {
        for (Matcher<? extends Constraint> matcher : matchers) {
            boolean matched = matcher.match(this, decisionModel, featureModel, constraint);
            if (matched) {
                break;
            }
        }
    }
}
