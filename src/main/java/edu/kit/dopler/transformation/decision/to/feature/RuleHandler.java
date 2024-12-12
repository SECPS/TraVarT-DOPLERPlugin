package edu.kit.dopler.transformation.decision.to.feature;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.Set;
import java.util.function.Supplier;

public class RuleHandler {

    public void handleRules(Dopler decisionModel, FeatureModel featureModel) {
        for (IDecision<?> decision : decisionModel.getDecisions()) {
            for (Rule rule : decision.getRules()) {
                handleRule(featureModel, rule);
            }
        }
    }

    private void handleRule(FeatureModel featureModel, Rule rule) {
        IExpression condition = rule.getCondition();
        Set<IAction> actions = rule.getActions();

        //TODO: Delete this. Actions should never be empty.
        if (actions.isEmpty()) {
            return;
        }

        Constraint left = createLeft(condition);
        Constraint right = createRight(actions);
        featureModel.getOwnConstraints().add(new ImplicationConstraint(left, right));
    }

    /** Create recursively a constraint from the actions */
    private Constraint createRight(Set<IAction> actions) {
        Supplier<RuntimeException> exception = () -> new RuntimeException("Actions set contains no item");
        IAction first = actions.stream().findFirst().orElseThrow(exception);

        if (1 == actions.size()) {
            return createRightLiteral(first);
        }

        actions.remove(first);
        return new AndConstraint(createRightLiteral(first), createRight(actions));
    }

    private Constraint createRightLiteral(IAction iAction) {
        return switch (iAction) {
            case BooleanEnforce booleanEnforce -> new LiteralConstraint(booleanEnforce.getDecision().getDisplayId());
            case EnumEnforce enumEnforce -> new LiteralConstraint(enumEnforce.getValue().toString());
            case DisAllows disAllows -> new NotConstraint(new LiteralConstraint("AAA"));
            case null, default -> throw new UnexpectedTypeException(iAction);
        };
    }

    private Constraint createLeft(IExpression condition) {

        if (condition instanceof NOT not) {
            return new NotConstraint(createLeft(not.getOperand()));
        } else if (condition instanceof Equals equals) {

            if (equals.getRightExpression() instanceof BooleanLiteralExpression booleanLiteralExpression &&
                    equals.getLeftExpression() instanceof DecisionValueCallExpression decisionValueCallExpression) {

                if (booleanLiteralExpression.getLiteral()) {
                    return new LiteralConstraint(decisionValueCallExpression.getDecision().getDisplayId());
                } else {
                    return new NotConstraint(
                            new LiteralConstraint(decisionValueCallExpression.getDecision().getDisplayId()));
                }
            }

            throw new RuntimeException("Could not translate condition");
        } else {
            throw new UnexpectedTypeException(condition);
        }
    }
}
