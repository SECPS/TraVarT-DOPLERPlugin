package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

abstract class Matcher<K extends Constraint> {

    /** Try to match. If matched then start routine. */
    boolean match(ConstraintHandler constraintHandler, IDecisionModel decisionModel, FeatureModel featureModel,
                  Constraint constraint) {
        if (isMatching(constraint)) {
            startRoutine(constraintHandler, decisionModel, featureModel, getConstraintClass().cast(constraint));
            return true;
        }
        return false;
    }

    /** Override if child class should match more complex constraints */
    protected boolean isMatching(Constraint constraint) {
        return getConstraintClass().isInstance(constraint);
    }

    abstract void startRoutine(ConstraintHandler constraintHandler, IDecisionModel decisionModel,
                               FeatureModel featureModel, K constraint);

    @SuppressWarnings("unchecked")
    private Class<K> getConstraintClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<K>) parameterizedType.getActualTypeArguments()[0];
    }
}
