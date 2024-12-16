package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.IAction;

import java.util.Set;

public interface RightCreator {

    Constraint createRight(Set<IAction> actions);
}
