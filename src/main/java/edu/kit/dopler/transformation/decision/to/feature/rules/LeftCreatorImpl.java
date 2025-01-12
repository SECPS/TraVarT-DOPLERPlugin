package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;
import edu.kit.dopler.model.AND;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.DecisionValueCallExpression;
import edu.kit.dopler.model.EnumeratorLiteralExpression;
import edu.kit.dopler.model.Equals;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.NOT;
import edu.kit.dopler.model.OR;
import edu.kit.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

/** Implementation of {@link LeftCreator}. */
public class LeftCreatorImpl implements LeftCreator {

    private static Optional<Constraint> handleEquals(Equals equals) {
        //Todo: There are probably a lot of other cases here.

        //Covers 'getValue(someDecision) = ...'
        if (equals.getLeftExpression() instanceof DecisionValueCallExpression decisionValueCallExpression) {

            // Covers: 'getValue(someDecision) = true' or 'getValue(someDecision) = false'
            if (equals.getRightExpression() instanceof BooleanLiteralExpression booleanLiteralExpression) {
                String decisionId = decisionValueCallExpression.getDecision().getDisplayId();
                if (booleanLiteralExpression.getLiteral()) {
                    return Optional.of(new LiteralConstraint(decisionId));
                } else {
                    return Optional.of(new NotConstraint(new LiteralConstraint(decisionId)));
                }
            }

            // Covers: 'getValue(someDecision) = someEnum'
            if (equals.getRightExpression() instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                return Optional.of(new LiteralConstraint(enumeratorLiteralExpression.toString()));
            }
        }

        throw new CanNotBeTranslatedException(equals);
    }

    private static Optional<Constraint> handleBooleanLiteralExpression(
            BooleanLiteralExpression booleanLiteralExpression) {
        if (booleanLiteralExpression.getLiteral()) {
            //Value is TRUE
            return Optional.empty();
        } else {
            //Value is FALSE
            throw new UnexpectedTypeException(booleanLiteralExpression);
        }
    }

    @Override
    public Optional<Constraint> handleCondition(IExpression condition) {
        //TODO: A lot of cases a missing here.
        return switch (condition) {
            case NOT not -> handleNot(not);
            case BooleanLiteralExpression booleanLiteralExpression ->
                    handleBooleanLiteralExpression(booleanLiteralExpression);
            case Equals equals -> handleEquals(equals);
            case AND and -> handleAnd(and);
            case OR or -> handleOr(or);
            case null, default -> throw new UnexpectedTypeException(condition);
        };
    }

    private Optional<Constraint> handleOr(OR or) {
        Optional<Constraint> left = handleCondition(or.getLeftExpression());
        Optional<Constraint> right = handleCondition(or.getRightExpression());

        if (right.isPresent() && left.isPresent()) {
            return Optional.of(new ParenthesisConstraint(new OrConstraint(left.get(), right.get())));
        }

        return Optional.empty();
    }

    private Optional<Constraint> handleAnd(AND and) {
        Optional<Constraint> left = handleCondition(and.getLeftExpression());
        Optional<Constraint> right = handleCondition(and.getRightExpression());

        if (right.isPresent() && left.isPresent()) {
            return Optional.of(new ParenthesisConstraint(new AndConstraint(left.get(), right.get())));
        }

        return Optional.empty();
    }

    private Optional<Constraint> handleNot(NOT not) {
        return handleCondition(not.getOperand()).map(NotConstraint::new);
    }
}
