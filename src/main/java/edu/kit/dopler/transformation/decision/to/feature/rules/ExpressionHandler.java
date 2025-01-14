package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.expression.Expression;
import edu.kit.dopler.model.IExpression;

import java.util.Optional;

public interface ExpressionHandler {

    Optional<Expression> handleExpression(IExpression expression);
}
