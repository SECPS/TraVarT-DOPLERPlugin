package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;

import java.util.List;

class AndMatcher extends Matcher<AndConstraint> {

    @Override
    void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
                      AndConstraint constraint) {
        List.of(constraint.getLeft(), constraint.getRight()).forEach(constraintHandler::handleConstraint);
    }

}
