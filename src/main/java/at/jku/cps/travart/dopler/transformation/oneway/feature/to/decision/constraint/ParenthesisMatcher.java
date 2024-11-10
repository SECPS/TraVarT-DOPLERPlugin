package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.ParenthesisConstraint;

class ParenthesisMatcher extends Matcher<ParenthesisConstraint> {

    @Override
    void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
                      ParenthesisConstraint constraint) {
        constraintHandler.handleConstraint(constraint.getContent());
    }
}
