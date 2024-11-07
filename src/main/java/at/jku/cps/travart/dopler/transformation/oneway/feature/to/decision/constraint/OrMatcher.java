package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.OrConstraint;

public class OrMatcher extends Matcher<OrConstraint> {

    @Override
    void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
                      OrConstraint constraint) {
        throw new RuntimeException("Or should not be root constraint");
    }

    @Override
    Class<OrConstraint> getConstraintClass() {
        return OrConstraint.class;
    }
}
