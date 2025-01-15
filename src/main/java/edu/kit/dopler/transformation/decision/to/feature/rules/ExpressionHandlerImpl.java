package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.expression.Expression;
import de.vill.model.expression.StringExpression;
import edu.kit.dopler.model.DecisionValueCallExpression;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.LiteralExpression;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

/** Implementation of {@link ExpressionHandler}. */
public class ExpressionHandlerImpl implements ExpressionHandler {

    @Override
    public Optional<Expression> handleExpression(IExpression expression) {
        return Optional.of(switch (expression) {
            case LiteralExpression literalExpression -> new StringExpression(literalExpression.toString());
            case DecisionValueCallExpression decisionValueCallExpression ->
                    new StringExpression(decisionValueCallExpression.getValue().getValue().toString());
            default -> throw new UnexpectedTypeException(expression);
        });
    }
}
