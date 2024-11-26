package edu.kit.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.DnfAlwaysFalseException;
import edu.kit.dopler.transformation.exceptions.DnfAlwaysTrueException;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.DnfAlwaysTrueAndFalseRemover;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.DnfToTreeConverter;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.TreeToDnfConverter;

import java.util.*;

/**
 * Implementation of {@link ConstraintHandler}.
 */
public class ConstraintHandlerImpl implements ConstraintHandler {

    private final TreeToDnfConverter treeToDnfConverter;
    private final DnfToTreeConverter dnfToTreeConverter;
    private final ActionCreator actionCreator;
    private final ConditionCreator conditionCreator;
    private final DnfAlwaysTrueAndFalseRemover dnfAlwaysTrueAndFalseRemover;

    /** Constructor of {@link ConstraintHandlerImpl} */
    public ConstraintHandlerImpl(TreeToDnfConverter treeToDnfConverter, DnfToTreeConverter dnfToTreeConverter,
                                 ActionCreator actionCreator, ConditionCreator conditionCreator,
                                 DnfAlwaysTrueAndFalseRemover dnfAlwaysTrueAndFalseRemover) {
        this.treeToDnfConverter = treeToDnfConverter;
        this.dnfToTreeConverter = dnfToTreeConverter;
        this.actionCreator = actionCreator;
        this.conditionCreator = conditionCreator;
        this.dnfAlwaysTrueAndFalseRemover = dnfAlwaysTrueAndFalseRemover;
    }

    @Override
    public void handleOwnConstraints(FeatureModel featureModel, Dopler decisionModel) {
        for (Constraint constraint : getSanitizedConstraints(featureModel)) {
            List<List<Constraint>> dnf = convertConstraintIntoDnf(featureModel, constraint);
            Optional<Rule> rule = createRuleFromDnf(featureModel, decisionModel, dnf);
            rule.ifPresent(this::distributeRule);
        }
    }

    /** Converts the given constraint into DNF. */
    private List<List<Constraint>> convertConstraintIntoDnf(FeatureModel featureModel, Constraint constraint) {
        try {
            List<List<Constraint>> dnf = treeToDnfConverter.convertToDnf(constraint);
            return dnfAlwaysTrueAndFalseRemover.removeAlwaysTruOrFalseConstraints(featureModel, dnf);
        } catch (DnfAlwaysFalseException e) {
            //Constraint is always false. Model is invalid.
            throw new RuntimeException(e);
        } catch (DnfAlwaysTrueException e) {
            //Constraint is always true. No rule should be generated. Return empty DNF.
            return new ArrayList<>();
        }
    }

    /** Converts the given constraint in DNF into a {@link Rule} from the decision model. */
    private Optional<Rule> createRuleFromDnf(FeatureModel featureModel, Dopler decisionModel,
                                             List<List<Constraint>> dnf) {
        // DNF contains no OR
        if (1 == dnf.size()) {
            Set<IAction> actions = new LinkedHashSet<>();
            for (Constraint constraint : dnf.getFirst()) {
                actions.add(actionCreator.createAction(decisionModel, constraint));
            }
            return Optional.of(new Rule(new BooleanLiteralExpression(true), actions));
        }

        //DNF contains at least one OR
        if (1 < dnf.size()) {
            //Outer right conjunction contains the actions
            Set<IAction> actions = new LinkedHashSet<>();
            for (Constraint constraint : dnf.removeLast()) {
                actions.add(actionCreator.createAction(decisionModel, constraint));
            }

            //All the other conjunctions create the condition
            IExpression condition = conditionCreator.createCondition(decisionModel, featureModel, createLeft(dnf));
            return Optional.of(new Rule(condition, actions));
        }
        return Optional.empty();
    }

    /** Converts the given left side of the DNF to a tree and removes double negation if needed. */
    private Constraint createLeft(List<List<Constraint>> leftSide) {
        Constraint left = new NotConstraint(dnfToTreeConverter.createDnfFromList(leftSide));
        if (((NotConstraint) left).getContent() instanceof NotConstraint) {
            left = ((NotConstraint) ((NotConstraint) left).getContent()).getContent();
        }
        return left;
    }

    /**
     * If the {@link Constraint}s in the given  {@link FeatureModel} has {@link AndConstraint}s as roots, then split
     * them up into several {@link Constraint}s. E.g.: {@code (A => B) & (C => D) ~> A => B, C => D}
     *
     * @return {@link List} of all constraints in the given {@link FeatureModel} where no root {@link Constraint} is a
     * {@link AndConstraint}
     */
    private static List<Constraint> getSanitizedConstraints(FeatureModel featureModel) {
        Stack<Constraint> stack = new Stack<>();
        featureModel.getConstraints().reversed().forEach(stack::push);
        List<Constraint> sanitisedConstrains = new ArrayList<>();
        while (!stack.empty()) {
            Constraint current = stack.pop();
            if (current instanceof AndConstraint andConstraint) {
                stack.push(andConstraint.getLeft());
                stack.push(andConstraint.getRight());
            } else {
                sanitisedConstrains.add(current);
            }
        }
        return sanitisedConstrains;
    }

    /** Distribute the generated rules to the decisions */
    private void distributeRule(Rule rule) {
        Optional<IAction> firstAction = rule.getActions().stream().findFirst();
        if (firstAction.isPresent()) {
            ((ValueRestrictionAction) firstAction.get()).getDecision().addRule(rule);
        } else {
            throw new RuntimeException("Actions are empty but should contain at least one element.");
        }
    }
}
