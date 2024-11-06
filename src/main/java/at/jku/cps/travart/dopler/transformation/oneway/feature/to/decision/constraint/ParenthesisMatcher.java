package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ParenthesisConstraint;

import java.util.Optional;

public class ParenthesisMatcher implements Matcher<ParenthesisConstraint> {

    @Override
    public Optional<ParenthesisConstraint> match(Constraint constraint) {
        if (constraint instanceof ParenthesisConstraint) {
            return Optional.of((ParenthesisConstraint) constraint);
        }
        return Optional.empty();
    }

    @Override
    public void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                             FeatureModel featureModel, ParenthesisConstraint constraint) {
        constraintHandler.handleConstraint(constraint.getContent());
    }
}
