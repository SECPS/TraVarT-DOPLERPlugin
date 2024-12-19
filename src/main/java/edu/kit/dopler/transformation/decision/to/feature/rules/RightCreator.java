package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.Feature;
import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.IAction;

import java.util.Collection;
import java.util.Set;

public interface RightCreator {

    Constraint createRight(Feature root, Set<IAction> actions);
}
