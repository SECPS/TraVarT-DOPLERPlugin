/*******************************************************************************
 * SPDX-License-Identifier: MPL-2.0
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 * <p>
 * Contributors:
 *    @author Yannick Kraml
 *    @author Kevin Feichtinger
 * <p>
 * Copyright 2024 Karlsruhe Institute of Technology (KIT)
 * KASTEL - Dependability of Software-intensive Systems
 *******************************************************************************/
package edu.kit.travart.dopler.transformation.feature.to.decision.constraint;

import de.vill.model.FeatureModel;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.NotConstraint;
import edu.kit.dopler.model.AND;
import edu.kit.dopler.model.BooleanDecision;
import edu.kit.dopler.model.BooleanEnforce;
import edu.kit.dopler.model.BooleanLiteralExpression;
import edu.kit.dopler.model.BooleanValue;
import edu.kit.dopler.model.Decision;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.DoubleValue;
import edu.kit.dopler.model.Equals;
import edu.kit.dopler.model.GreatherThan;
import edu.kit.dopler.model.IAction;
import edu.kit.dopler.model.IDecision;
import edu.kit.dopler.model.IExpression;
import edu.kit.dopler.model.LessThan;
import edu.kit.dopler.model.NOT;
import edu.kit.dopler.model.NumberDecision;
import edu.kit.dopler.model.NumberEnforce;
import edu.kit.dopler.model.OR;
import edu.kit.dopler.model.Rule;
import edu.kit.dopler.model.StringDecision;
import edu.kit.dopler.model.StringEnforce;
import edu.kit.dopler.model.StringLiteralExpression;
import edu.kit.dopler.model.StringValue;
import edu.kit.dopler.model.ValueRestrictionAction;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfAlwaysTrueAndFalseRemover;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.DnfToTreeConverter;
import edu.kit.travart.dopler.transformation.feature.to.decision.constraint.dnf.TreeToDnfConverter;
import edu.kit.travart.dopler.transformation.util.DecisionFinder;
import edu.kit.travart.dopler.transformation.util.DecisionFinderImpl;
import edu.kit.travart.dopler.transformation.exceptions.CanNotBeTranslatedException;
import edu.kit.travart.dopler.transformation.exceptions.DecisionNotPresentException;
import edu.kit.travart.dopler.transformation.exceptions.DnfAlwaysFalseException;
import edu.kit.travart.dopler.transformation.exceptions.DnfAlwaysTrueException;
import edu.kit.travart.dopler.transformation.exceptions.ModelInvalidException;
import edu.kit.travart.dopler.transformation.exceptions.UnexpectedTypeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ConstraintHandler}.
 */
public class ConstraintHandlerImpl implements ConstraintHandler {

    private final TreeToDnfConverter treeToDnfConverter;
    private final DnfToTreeConverter dnfToTreeConverter;
    private final ActionCreator actionCreator;
    private final ConditionCreator conditionCreator;
    private final DnfAlwaysTrueAndFalseRemover dnfAlwaysTrueAndFalseRemover;

    /**
     * Constructor of {@link ConstraintHandlerImpl}.
     *
     * @param treeToDnfConverter           {@link TreeToDnfConverter}
     * @param dnfToTreeConverter           {@link DnfToTreeConverter}
     * @param actionCreator                {@link ActionCreator}
     * @param conditionCreator             {@link ConditionCreator}
     * @param dnfAlwaysTrueAndFalseRemover {@link DnfAlwaysTrueAndFalseRemover}
     */
    public ConstraintHandlerImpl(TreeToDnfConverter treeToDnfConverter, DnfToTreeConverter dnfToTreeConverter,
                                 ActionCreator actionCreator, ConditionCreator conditionCreator,
                                 DnfAlwaysTrueAndFalseRemover dnfAlwaysTrueAndFalseRemover) {
        this.treeToDnfConverter = treeToDnfConverter;
        this.dnfToTreeConverter = dnfToTreeConverter;
        this.actionCreator = actionCreator;
        this.conditionCreator = conditionCreator;
        this.dnfAlwaysTrueAndFalseRemover = dnfAlwaysTrueAndFalseRemover;
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

    @Override
    public void handleConstraints(FeatureModel featureModel, Dopler decisionModel) {
        for (Constraint constraint : getSanitizedConstraints(featureModel)) {
            List<List<Constraint>> dnf = convertConstraintIntoDnf(featureModel, constraint);
            Optional<Rule> rule = createRuleFromDnf(featureModel, decisionModel, dnf);
            rule.ifPresent(otherRule -> distributeRule(otherRule, decisionModel));
        }
    }

    /** Converts the given constraint into DNF. */
    private List<List<Constraint>> convertConstraintIntoDnf(FeatureModel featureModel, Constraint constraint) {
        try {
            List<List<Constraint>> dnf = treeToDnfConverter.convertToDnf(constraint);
            return dnfAlwaysTrueAndFalseRemover.removeAlwaysTruOrFalseConstraints(featureModel, dnf);
        } catch (DnfAlwaysFalseException e) {
            //Constraint is always false. Model is invalid.
            throw new ModelInvalidException("Model is invalid. One of the constraints is always false.", e);
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
            List<IAction> contradiction = createContradiction(decisionModel);
            return Optional.of(new Rule(condition, new LinkedHashSet<>(contradiction)));
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

    /** Distribute the generated rule to a decision */
    private void distributeRule(Rule rule, Dopler decisionModel) {
        // Add rule to the decisions of the condition
        DecisionFinder decisionFinder = new DecisionFinderImpl();
        // To keep rules like if(true) {decision=true}, they are need to restore to fm 
        if (rule.getCondition() instanceof BooleanLiteralExpression) {
            rule.getActions().stream()
                .sorted(Comparator.comparing(Object::toString))
                .filter(action -> action instanceof ValueRestrictionAction)
                .map(action -> (ValueRestrictionAction) action).forEach(action -> action.getDecision().addRule(rule));
        } else {
            Set<String> decisionIds = getDecisionIds(rule.getCondition());
            for(String decisionId : decisionIds) {
                Optional<IDecision<?>> decisionOptional = decisionFinder.findDecisionById(decisionModel, decisionId);
                decisionOptional.ifPresent(decision -> decision.addRule(rule));
            }
        }
    }

private Set<String> getDecisionIds(IExpression condition) {
    Set<String> decisionIds = new LinkedHashSet<>();
    // Recursive lookUp for decisionIds
    if (condition instanceof StringLiteralExpression stringLiteralExpression) {
        decisionIds.add(stringLiteralExpression.getLiteral());
    } else if (condition instanceof Equals equalsExpression) {
        decisionIds.addAll(getDecisionIds(equalsExpression.getLeftExpression()));
        decisionIds.addAll(getDecisionIds(equalsExpression.getRightExpression()));
    } else if (condition instanceof NOT notExpression) {
        decisionIds.addAll(getDecisionIds(notExpression.getOperand()));
    } else if (condition instanceof AND andExpression) {
        decisionIds.addAll(getDecisionIds(andExpression.getLeftExpression()));
        decisionIds.addAll(getDecisionIds(andExpression.getRightExpression()));
    } else if (condition instanceof OR orExpression) {
        decisionIds.addAll(getDecisionIds(orExpression.getLeftExpression()));
        decisionIds.addAll(getDecisionIds(orExpression.getRightExpression()));
    } else if (condition instanceof GreatherThan greaterThanExpression) {
        decisionIds.addAll(getDecisionIds(greaterThanExpression.getLeftExpression()));
        decisionIds.addAll(getDecisionIds(greaterThanExpression.getRightExpression()));
    } else if (condition instanceof LessThan lessThanExpression) {
        decisionIds.addAll(getDecisionIds(lessThanExpression.getLeftExpression()));
        decisionIds.addAll(getDecisionIds(lessThanExpression.getRightExpression()));
    }
    // Other Expression types are ignored (e.g. Boolean, Double) and these do not contain decision IDs.

    // Strip part after last dot to get the correct ID
    decisionIds =  decisionIds.stream()
        .map(id -> {
            int lastIndex = id.lastIndexOf('.');
            return (lastIndex != -1) ? id.substring(0, lastIndex) : id;
        })
        .collect(Collectors.toSet());

    return decisionIds;
}

    /** Creates a set of {@link IAction}s that contradict each other. */
    private List<IAction> createContradiction(Dopler decisionModel) {
        //Use always the same decision to create contradiction. The decision cannot be an enum decision.
        Optional<IDecision<?>> decisionOptional = decisionModel.getDecisions().stream()
                .filter(decision -> Decision.DecisionType.ENUM != decision.getDecisionType())
                .min(Comparator.comparing(IDecision::getDisplayId));
        if (decisionOptional.isEmpty()) {
            throw new DecisionNotPresentException("First");
        }

        return switch (decisionOptional.get()) {
            case BooleanDecision booleanDecision ->
                    List.of(new BooleanEnforce(booleanDecision, new BooleanValue(Boolean.TRUE)),
                            new BooleanEnforce(booleanDecision, new BooleanValue(Boolean.FALSE)));
            case StringDecision stringDecision -> List.of(new StringEnforce(stringDecision, new StringValue("A")),
                    new StringEnforce(stringDecision, new StringValue("B")));
            case NumberDecision numberDecision -> List.of(new NumberEnforce(numberDecision, new DoubleValue(1.0)),
                    new NumberEnforce(numberDecision, new DoubleValue(-1.0)));
            default -> throw new UnexpectedTypeException(decisionOptional.get());
        };
    }
}
