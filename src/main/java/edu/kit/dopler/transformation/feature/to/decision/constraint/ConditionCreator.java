package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.transformation.exceptions.CanNotBeTranslatedException;

public interface ConditionCreator {

    IExpression createCondition(Dopler decisionModel, FeatureModel featureModel, Constraint left)
            throws CanNotBeTranslatedException;
}
