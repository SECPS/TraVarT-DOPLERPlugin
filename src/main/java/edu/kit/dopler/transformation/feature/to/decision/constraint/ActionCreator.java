package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IAction;

public interface ActionCreator {

    IAction createAction(Dopler decisionModel, Constraint constraint);
}
