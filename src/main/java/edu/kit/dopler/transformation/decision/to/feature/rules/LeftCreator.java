package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.Constraint;
import edu.kit.dopler.model.IExpression;

import java.util.Optional;

public interface LeftCreator {

    Optional<Constraint> handleCondition(IExpression condition);
}
