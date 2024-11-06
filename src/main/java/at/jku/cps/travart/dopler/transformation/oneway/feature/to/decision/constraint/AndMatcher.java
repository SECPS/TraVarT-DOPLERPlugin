package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;

import java.util.List;
import java.util.Optional;

public class AndMatcher implements Matcher<AndConstraint> {

    @Override
    public Optional<AndConstraint> match(Constraint constraint) {

        if (constraint instanceof AndConstraint) {
            return Optional.of((AndConstraint) constraint);
        }

        return Optional.empty();
    }

    @Override
    public void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                             FeatureModel featureModel, AndConstraint constraint) {
        List.of(constraint.getLeft(), constraint.getRight()).forEach(constraintHandler::handleConstraint);
    }
}
