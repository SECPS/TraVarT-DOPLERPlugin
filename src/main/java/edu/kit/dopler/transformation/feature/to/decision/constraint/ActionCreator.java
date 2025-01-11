package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IAction;

/**
 * This interface is responsible for creating {@link IAction}s in the {@link Dopler} model from given
 * {@link Constraint}s from a {@link FeatureModel}.
 */
public interface ActionCreator {

    /**
     * Creates an {@link IAction} with the given {@link Constraint}.
     *
     * @param decisionModel {@link Dopler} model, in which the {@link IAction} is put later
     * @param constraint    {@link Constraint} that will be translated
     *
     * @return Created {@link IAction}
     */
    IAction createAction(Dopler decisionModel, Constraint constraint);
}
