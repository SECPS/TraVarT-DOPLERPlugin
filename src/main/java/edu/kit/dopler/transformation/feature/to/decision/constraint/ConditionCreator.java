package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IExpression;

/**
 * This interface is responsible for creating conditions as {@link IExpression}s in the {@link Dopler} model from given
 * {@link Constraint}s from a {@link FeatureModel}.
 */
public interface ConditionCreator {

    /**
     * Creates a condition with the given {@link Constraint}.
     *
     * @param decisionModel {@link Dopler} model, in which the condition is put later
     * @param featureModel  {@link FeatureModel} that contains the given {@link Constraint}
     * @param constraint    Constraint to translate
     *
     * @return Created condition as {@link IExpression}
     */
    IExpression createCondition(Dopler decisionModel, FeatureModel featureModel, Constraint constraint);
}
