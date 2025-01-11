package edu.kit.dopler.transformation.feature.to.decision.constraint;

import com.google.inject.Inject;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.model.*;
import edu.kit.dopler.transformation.exceptions.*;
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

    @Inject
    ConstraintHandlerImpl(TreeToDnfConverter treeToDnfConverter, DnfToTreeConverter dnfToTreeConverter,
                          ActionCreator actionCreator, ConditionCreator conditionCreator,
                          DnfAlwaysTrueAndFalseRemover dnfAlwaysTrueAndFalseRemover) {
        this.treeToDnfConverter = treeToDnfConverter;
        this.dnfToTreeConverter = dnfToTreeConverter;
        this.actionCreator = actionCreator;
        this.conditionCreator = conditionCreator;
        this.dnfAlwaysTrueAndFalseRemover = dnfAlwaysTrueAndFalseRemover;
    }

    @Override
    public void handleConstraints(FeatureModel featureModel, Dopler decisionModel) {
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
            //TODO: ein Widerspruch sollte hier erstellt werden
            throw new RuntimeException(e);
        } catch (DnfAlwaysTrueException e) {
            //Constraint is always true. No rule should be generated. Return empty DNF.
            return new ArrayList<>();
        }
    }

    /** Converts the given constraint in DNF into a {@link Rule} from the decision model. */
    private Optional<Rule> createRuleFromDnf(FeatureModel featureModel, Dopler decisionModel,
                                             List<List<Constraint>> dnf) {

        //Dnf contains an ExpressionConstraint
        if (dnf.stream().flatMap(Collection::stream)
                .anyMatch(constraint -> constraint instanceof ExpressionConstraint)) {
            return handleDnfWithAttributeConstraints(featureModel, decisionModel, dnf);
        }

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

    /**
     * Special case if dnf contains attribute constraints Needed because there is no way to model a constraint like "A <
     * 10" as an action The action will be a contradiction and everything is modelled as a condition.
     */
    private Optional<Rule> handleDnfWithAttributeConstraints(FeatureModel featureModel, Dopler decisionModel,
                                                             List<List<Constraint>> dnf) {
        try {
            IExpression condition = conditionCreator.createCondition(decisionModel, featureModel,
                    new NotConstraint(dnfToTreeConverter.createDnfFromList(dnf)));
            Set<IAction> contradiction = createContradiction(decisionModel);
            return Optional.of(new Rule(condition, contradiction));
        } catch (CanNotBeTranslatedException e) {
            //The dnf to translate contains an expression.
            //Expressions can not be translated.
            //Skip the constraint.
            return Optional.empty();
        }
    }

    /** Converts the given left side of the DNF to a tree and removes double negations if needed. */
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

    /** Distribute the generated rule to a decision */
    private void distributeRule(Rule rule) {
        //Just take decision of first action
        ((ValueRestrictionAction) rule.getActions().stream().findFirst().orElseThrow()).getDecision().addRule(rule);
    }

    /** Creates a set of {@link IAction}s that contradict each other. */
    private Set<IAction> createContradiction(Dopler decisionModel) {
        //Use the first decision to create contradiction
        Optional<IDecision<?>> decision = decisionModel.getDecisions().stream().findFirst();
        if (decision.isEmpty()) {
            throw new DecisionNotPresentException("First");
        }

        return switch (decision.get()) {
            case BooleanDecision booleanDecision ->
                    Set.of(new BooleanEnforce(booleanDecision, new BooleanValue(Boolean.TRUE)),
                            new BooleanEnforce(booleanDecision, new BooleanValue(Boolean.FALSE)));
            case StringDecision stringDecision -> Set.of(new StringEnforce(stringDecision, new StringValue("true")),
                    new StringEnforce(stringDecision, new StringValue("false")));
            case NumberDecision numberDecision -> Set.of(new NumberEnforce(numberDecision, new DoubleValue(1.0)),
                    new NumberEnforce(numberDecision, new DoubleValue(-1.0)));
            default -> throw new UnexpectedTypeException(decision.get());
        };
    }
}
