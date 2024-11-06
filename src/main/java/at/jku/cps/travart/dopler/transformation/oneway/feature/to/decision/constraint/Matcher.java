package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.util.Optional;

public interface Matcher<K extends Constraint> {

    Optional<K> match(Constraint constraint);

    void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
                      K constraint);
}
