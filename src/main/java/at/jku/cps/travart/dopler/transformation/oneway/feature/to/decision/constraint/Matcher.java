package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

abstract class Matcher<K extends Constraint> {

    void match(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
               Constraint constraint) {
        Class<K> constraintClass = getConstraintClass();
        if (constraintClass.isInstance(constraint)) {
            startRoutine(constraintHandler, decisionModel, featureModel, constraintClass.cast(constraint));
        }
    }

    abstract void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                               FeatureModel featureModel, K constraint);

    abstract Class<K> getConstraintClass();
}
