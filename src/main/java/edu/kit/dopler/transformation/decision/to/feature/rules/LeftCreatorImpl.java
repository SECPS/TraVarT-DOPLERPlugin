package edu.kit.dopler.transformation.decision.to.feature.rules;

import de.vill.model.constraint.*;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Optional;

public class LeftCreatorImpl implements LeftCreator {

    @Override
    public Optional<Constraint> handleCondition(IExpression condition) {

        return switch (condition) {
            case NOT not -> handleNot(not);
            case BooleanLiteralExpression booleanLiteralExpression ->
                    handleBooleanLiteralExpression(booleanLiteralExpression);
            case Equals equals -> handleEquals(equals);
            case AND and -> handleAnd(and);
            case OR or -> handleOr(or);
            case null, default -> {
                System.out.println(condition);
                throw new UnexpectedTypeException(condition);
            }
        };
    }

    private Optional<Constraint> handleOr(OR or) {
        Optional<Constraint> left = handleCondition(or.getLeftExpression());
        Optional<Constraint> right = handleCondition(or.getRightExpression());

        if (right.isEmpty() || left.isEmpty()) {
            throw new RuntimeException("Inner constraint of `AND` could not be computed");
        }

        return Optional.of(new ParenthesisConstraint(new OrConstraint(left.get(), right.get())));
    }

    private Optional<Constraint> handleAnd(AND and) {
        Optional<Constraint> left = handleCondition(and.getLeftExpression());
        Optional<Constraint> right = handleCondition(and.getRightExpression());

        if (right.isEmpty() || left.isEmpty()) {
            throw new RuntimeException("Inner constraint of `AND` could not be computed");
        }

        return Optional.of(new ParenthesisConstraint(new AndConstraint(left.get(), right.get())));
    }

    private static Optional<Constraint> handleEquals(Equals equals) {
        if (equals.getLeftExpression() instanceof DecisionValueCallExpression decisionValueCallExpression) {

            // Covers: `getValue(someDecision) = true` or `getValue(someDecision) = false`
            if (equals.getRightExpression() instanceof BooleanLiteralExpression booleanLiteralExpression) {
                if (booleanLiteralExpression.getLiteral()) {
                    return Optional.of(new LiteralConstraint(decisionValueCallExpression.getDecision().getDisplayId()));
                } else {
                    return Optional.of(new NotConstraint(
                            new LiteralConstraint(decisionValueCallExpression.getDecision().getDisplayId())));
                }
            }

            // Covers: `getValue(someDecision) = someEnum`
            if (equals.getRightExpression() instanceof EnumeratorLiteralExpression enumeratorLiteralExpression) {
                return Optional.of(new LiteralConstraint(enumeratorLiteralExpression.toString()));
            }
        }

        throw new RuntimeException("Could not translate condition: " + equals);
    }

    private Optional<Constraint> handleNot(NOT not) {
        Optional<Constraint> innerConstraint = handleCondition(not.getOperand());

        if (innerConstraint.isEmpty()) {
            throw new RuntimeException("Inner constraint of `NOT` could not be computed");
        }

        return Optional.of(new NotConstraint(innerConstraint.get()));
    }

    private static Optional<Constraint> handleBooleanLiteralExpression(
            BooleanLiteralExpression booleanLiteralExpression) {
        if (booleanLiteralExpression.getLiteral()) {
            return Optional.empty();
        } else {
            throw new RuntimeException("Left side is FALSE");
        }
    }
}
