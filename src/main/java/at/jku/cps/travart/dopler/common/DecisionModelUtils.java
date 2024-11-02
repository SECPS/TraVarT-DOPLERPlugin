/*******************************************************************************
 * TODO: explanation what the class does
 *
 * @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.common;

import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ConditionCreationException;
import at.jku.cps.travart.dopler.decision.impl.DMCSVHeader;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.*;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.impl.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DecisionModelUtils {

    private static final char DELIMITER = ';';

    private static final String ENUM_DECISION_CONSTRAINT_REGEX = "(and|or)#constr#([0-9]+)";
    private static final String ENUM_DECISION_TRANSFORMED_REGEX = "#([0-9]+)$";

    public static CSVFormat createCSVFormat(final boolean skipHeader) {
        Builder builder = CSVFormat.EXCEL.builder();
        builder.setDelimiter(DELIMITER);
        builder.setHeader(DMCSVHeader.stringArray());
        builder.setSkipHeaderRecord(skipHeader);
        return builder.build();
    }

    @SuppressWarnings("rawtypes")
    public static ICondition consumeToBinaryCondition(final List<IDecision> decisions,
                                                      final Class<? extends ABinaryCondition> clazz,
                                                      final boolean negative) throws ConditionCreationException {
        if (decisions.isEmpty()) {
            throw new ConditionCreationException(new IllegalArgumentException("Set of decisions is empty!"));
        }
        if (decisions.stream().anyMatch(d -> d == null)) {
            throw new ConditionCreationException(
                    new IllegalArgumentException("Set of decisions contains a null value"));
        }
        if (decisions.size() == 1) {
            if (decisions.get(0) == null) {
                throw new ConditionCreationException(new IllegalArgumentException("Only existing decision is null!"));
            }
            return negative ? new Not(new IsSelectedFunction(decisions.get(0))) :
                    new IsSelectedFunction(decisions.get(0));
        }
        try {
            // get the constructor
            Constructor<? extends ABinaryCondition> constructor =
                    clazz.getConstructor(ICondition.class, ICondition.class);
            // take the first two and create the first ABinaryCondition
            ICondition first = new IsSelectedFunction(decisions.remove(0));
            ICondition second = new IsSelectedFunction(decisions.remove(0));
            if (negative) {
                first = new Not(first);
                second = new Not(second);
            }
            ABinaryCondition condition = constructor.newInstance(first, second);
            while (!decisions.isEmpty()) {
                ICondition next = new IsSelectedFunction(decisions.remove(0));
                if (negative) {
                    next = new Not(next);
                }
                condition = constructor.newInstance(next, condition);
            }
            return condition;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new ConditionCreationException(e);
        }
    }

    public static boolean detectCircle(final ICondition condition, final ICondition toAdd) {
        if (toAdd == ICondition.TRUE || toAdd == ICondition.FALSE || condition == ICondition.TRUE ||
                condition == ICondition.FALSE) {
            return false;
        }
        if (condition == toAdd) {
            return true;
        }
        boolean ret = false;
        if (condition instanceof AUnaryCondition) {
            ret = ret || detectCircle(((AUnaryCondition) condition).getOperand(), toAdd);
        }
        if (condition instanceof ABinaryCondition) {
            ret = ret || detectCircle(((ABinaryCondition) condition).getLeft(), toAdd);
            ret = ret || detectCircle(((ABinaryCondition) condition).getRight(), toAdd);
        }
        return ret;
    }

    public static Set<BooleanDecision> getBooleanDecisions(final IDecisionModel dm) {
        return getDecisionType(dm, DecisionType.BOOLEAN, BooleanDecision.class);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends IDecision> Optional<T> getDecisionbyId(final Set<T> decisions, final String id) {
        return Objects.requireNonNull(decisions).stream().filter(d -> id.equals(d.getId())).findFirst();
    }

    private static <T> Set<T> getDecisionType(final IDecisionModel dm, final AbstractDecision.DecisionType type,
                                              final Class<T> clazz) {
        Set<T> decisions = new HashSet<>();
        for (IDecision<?> decision : dm.getDecisions()) {
            if (decision.getType() == type) {
                decisions.add(clazz.cast(decision));
            }
        }
        return decisions;
    }

    public static Set<String> getBooleanDecisionsAsNames(final IDecisionModel dm) {
        Set<String> names = new HashSet<>();
        for (IDecision<?> decision : dm.getDecisions()) {
            if (decision.getType() == AbstractDecision.DecisionType.BOOLEAN) {
                names.add(((BooleanDecision) decision).getId());
            }
        }
        return names;
    }

    public static Set<EnumerationDecision> getEnumDecisions(final IDecisionModel dm) {
        return getDecisionType(dm, DecisionType.ENUM, EnumerationDecision.class);
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEnumDecisionConstraint(final IDecision decision) {
        return decision.getId().matches(ENUM_DECISION_CONSTRAINT_REGEX);
    }

    public static boolean isTransformedEnumDecision(final EnumerationDecision decision,
                                                    final String decisionStartName) {
        return decision.getId().matches(Pattern.quote(decisionStartName) + ENUM_DECISION_TRANSFORMED_REGEX);
    }

    public static Set<String> getEnumDecisionsAsNames(final IDecisionModel dm) {
        return getEnumDecisions(dm).stream().map(EnumerationDecision::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<NumberDecision> getNumberDecisions(final IDecisionModel dm) {
        return getDecisionType(dm, DecisionType.NUMBER, NumberDecision.class);
    }

    public static Set<String> getNumberDecisionsAsNames(final IDecisionModel dm) {
        Set<String> names = new HashSet<>();
        for (IDecision<?> decision : dm.getDecisions()) {
            if (decision.getType() == AbstractDecision.DecisionType.NUMBER) {
                names.add(((NumberDecision) decision).getId());
            }
        }
        return names;
    }

    @SuppressWarnings("rawtypes")
    public static Set<IDecision> getReachableDecisions(final IDecisionModel dm) {
        Set<IDecision> decisions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            if (decision.isVisible() && !decision.isSelected()) {
                decisions.add(decision);
            }
        }
        return decisions;
    }

    @SuppressWarnings("rawtypes")
    public static Set<IDecision> getSelectableDecisions(final IDecisionModel dm) {
        Set<IDecision> decisions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            if (!(decision.getVisiblity() == ICondition.FALSE)) {
                decisions.add(decision);
            }
        }
        return decisions;
    }

    @SuppressWarnings("rawtypes")
    public static Set<IDecision> getVisibleDecisions(final IDecisionModel dm) {
        Set<IDecision> decisions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            if (decision.getVisiblity().evaluate()) {
                decisions.add(decision);
            }
        }
        return decisions;
    }

    /**
     * @param dm
     *
     * @return returns a list of visible decisions as names
     */
    public static Set<String> getSelectableDecisionsAsNames(final IDecisionModel dm) {
        return getSelectableDecisions(dm).stream().map(IDecision::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("rawtypes")
    public static Set<IDecision> getSelectedDecisions(final IDecisionModel dm) {
        Set<IDecision> decisions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            if (decision.isSelected()) {
                decisions.add(decision);
            }
        }
        return decisions;
    }

    public static List<String> getSelectedDecisionsAsNames(final IDecisionModel dm) {
        return getSelectedDecisions(dm).stream().map(IDecision::getId).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Set<StringDecision> getStringDecisions(final IDecisionModel dm) {
        return getDecisionType(dm, DecisionType.STRING, StringDecision.class);
    }

    public static Set<String> getStringDecisionsAsNames(final IDecisionModel dm) {
        return getStringDecisions(dm).stream().map(StringDecision::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean hasReachableDecisions(final IDecisionModel dm) {
        return !getReachableDecisions(dm).isEmpty();
    }

    public static boolean isBinaryCondition(final ICondition visiblity) {
        return visiblity instanceof ABinaryCondition;
    }

    public static boolean isComplexCondition(final ICondition condition) {
        return !isDecisionValueCondition(condition) &&
                (isNotCondition(condition) || condition instanceof ABinaryCondition);
    }

    public static boolean isComplexVisibilityCondition(final ICondition visibility) {
        return !(visibility instanceof IsSelectedFunction) && !(visibility instanceof IRangeValue) &&
                visibility != ICondition.TRUE && visibility != ICondition.FALSE;
    }

    public static boolean isDecisionValueCondition(ICondition condition) {
        if (condition instanceof Not) {
            condition = ((Not) condition).getOperand();
        }
        return condition instanceof DecisionValueCondition;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean isEnumNoneOption(final IDecision decision, final IValue value) {
        return decision.getType() == AbstractDecision.DecisionType.ENUM && value instanceof AbstractRangeValue &&
                ((EnumerationDecision) decision).isNoneOption((AbstractRangeValue) value);
    }

    /*
     * Identify rules which contain actions to itself. e.g., if
     * (isSelected(d_Storage)) { disAllow(d_Storage_0.None); }
     */
    @SuppressWarnings("rawtypes")
    public static boolean isInItSelfRule(final Rule rule) {
        ICondition condition = rule.getCondition();
        if (condition instanceof Not) {
            condition = ((Not) condition).getOperand();
        }
        if (!(condition instanceof IsSelectedFunction)) {
            return false;
        }
        IsSelectedFunction isSelected = (IsSelectedFunction) condition;
        IDecision actionVariable = rule.getAction().getVariable();
        return isSelected.getParameters().get(0) == actionVariable;
    }

    public static boolean isMandatoryVisibilityCondition(final ICondition visiblity) {
        if (!(visiblity instanceof And)) {
            return false;
        }
        And and = (And) visiblity;
        return and.getLeft() == ICondition.FALSE && and.getRight() instanceof IsSelectedFunction ||
                and.getLeft() instanceof IsSelectedFunction && and.getRight() == ICondition.FALSE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean isNoneAction(final IAction action) {
        return action.getVariable() instanceof IEnumerationDecision &&
                action.getValue() instanceof AbstractRangeValue &&
                ((IEnumerationDecision) action.getVariable()).isNoneOption((AbstractRangeValue) action.getValue());
    }

    @SuppressWarnings("rawtypes")
    public static boolean allDecisionsAreSelected(final Set<IDecision> decisions) {
        return decisions.stream().allMatch(IDecision::isSelected);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean isNoneCondition(ICondition condition) {
        if (!isDecisionValueCondition(condition)) {
            return false;
        }
        if (condition instanceof Not) {
            condition = ((Not) condition).getOperand();
        }
        IDecision decision = ((DecisionValueCondition) condition).getDecision();
        AbstractRangeValue value = ((DecisionValueCondition) condition).getValue();
        return decision instanceof IEnumerationDecision && ((IEnumerationDecision) decision).isNoneOption(value);
    }

    public static boolean isNot(final ICondition condition) {
        return condition instanceof Not;
    }

    public static boolean isNotCondition(final ICondition condition) {
        return condition instanceof Not;
    }

    public static <T> Collection<Collection<T>> powerSet(final Collection<T> originalSet) {
        Collection<Collection<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<>());
            return sets;
        }
        List<T> list = new ArrayList<>(originalSet);
        T head = list.get(0);
        Collection<T> rest = new HashSet<>(list.subList(1, list.size()));
        for (Collection<T> set : powerSet(rest)) {
            Collection<T> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static <T> Collection<Collection<T>> powerSetWithMinAndMax(final Collection<T> originalSet, final int min,
                                                                      final int max) {
        return powerSet(originalSet).stream().filter(set -> set.size() >= min && set.size() <= max)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("rawtypes")
    public static Set<IDecision> retriveConditionDecisions(final IDecisionModel decisionModel,
                                                           final ICondition condition) {
        Set<IDecision> decisions = new HashSet<>();
        retriveConditionDecisionsRec(decisionModel, decisions, condition);
        return decisions;
    }

    @SuppressWarnings("rawtypes")
    private static void retriveConditionDecisionsRec(final IDecisionModel decisionModel, final Set<IDecision> decisions,
                                                     final ICondition condition) {
        if (condition instanceof AUnaryCondition) {
            retriveConditionDecisionsRec(decisionModel, decisions, ((AUnaryCondition) condition).getOperand());
        } else if (condition instanceof ABinaryCondition) {
            retriveConditionDecisionsRec(decisionModel, decisions, ((ABinaryCondition) condition).getLeft());
            retriveConditionDecisionsRec(decisionModel, decisions, ((ABinaryCondition) condition).getRight());
        } else if (condition instanceof DecisionValueCondition) {
            decisions.add(((DecisionValueCondition) condition).getDecision());
        } else if (condition instanceof IsTakenFunction) {
            decisions.add(((IsTakenFunction) condition).getParameters().get(0));
        } else if (condition instanceof IsSelectedFunction) {
            decisions.add(((IsSelectedFunction) condition).getParameters().get(0));
        } else if (condition instanceof StringValue) {
            decisions.add(decisionModel.get(((StringValue) condition).getValue()));
        }
    }

    @SuppressWarnings("rawtypes")
    public static String retriveFeatureName(final IDecision decision, final boolean addEnumExtension) {
        int toIndex = addEnumExtension ? decision.getId().lastIndexOf('#', decision.getId().length() - 1) :
                decision.getId().length();
        // TODO: fix this magic number for more enumerations then just one
        if (toIndex < 0 || decision.getId().length() - 2 != toIndex) {
            toIndex = decision.getId().length();
        }
        return decision.getId().substring(0, toIndex);
    }

    @SuppressWarnings("rawtypes")
    public static IDecision toDecision(final ICondition decision) {
        return (IDecision) decision;
    }

    private DecisionModelUtils() {
    }

    public static boolean isCompareCondition(final ICondition condition) {
        return condition instanceof Equals || condition instanceof Greater || condition instanceof Less ||
                condition instanceof GreaterEquals || condition instanceof LessEquals;
    }

    @SuppressWarnings("rawtypes")
    public static IDecision retriveMandatoryVisibilityCondition(final ICondition visiblity) {
        if (!isMandatoryVisibilityCondition(visiblity)) {
            throw new IllegalArgumentException("Given visibility " + visiblity +
                    " is no mandatory visibility condition. Check with #isMandatoryVisibilityCondition first.");
        }
        And and = (And) visiblity;
        IsSelectedFunction isSelected =
                (IsSelectedFunction) (and.getLeft() == ICondition.FALSE ? and.getRight() : and.getLeft());
        return isSelected.getParameters().get(0);
    }

    public static void logModelStatistics(final Logger logger, final IDecisionModel dm) {
        logger.log(Level.INFO, "Name: {0}", dm.getName());
        logger.log(Level.INFO, "#Decisons: {0}", getNumberOfDecisions(dm));
        logger.log(Level.INFO, "#Rules: {0}", countRules(dm));
        logger.log(Level.INFO, "#Rule Conditions: {0}", getConditonsCount(dm));
        logger.log(Level.INFO, "#Actions: {0}", countActions(dm));
        logger.log(Level.INFO, "#Visibility Conditions: {0}", countComplexVisibilityConditions(dm));
    }

    public static String getDecisionModelName(final IDecisionModel dm) {
        return dm.getName();
    }

    public static int getNumberOfDecisions(final IDecisionModel dm) {
        return dm.size();
    }

    @SuppressWarnings("rawtypes")
    public static long countRules(final IDecisionModel dm) {
        long rulesCount = 0;
        for (IDecision decision : dm.getDecisions()) {
            rulesCount += decision.getRules().size();
        }
        return rulesCount;
    }

    public static long getNumberOfConditions(final IDecisionModel dm) {
        return getConditonsCount(dm);
    }

    @SuppressWarnings("rawtypes")
    private static int getConditonsCount(final IDecisionModel dm) {
        Set<ICondition> conditions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            for (Object o : decision.getRules()) {
                Rule rule = (Rule) o;
                conditions.add(rule.getCondition());
            }
        }
        return conditions.size();
    }

    public static long getNumberOfActions(final IDecisionModel dm) {
        return countActions(dm);
    }

    @SuppressWarnings("rawtypes")
    private static int countActions(final IDecisionModel dm) {
        Set<IAction> actions = new HashSet<>();
        for (IDecision decision : dm.getDecisions()) {
            for (Object o : decision.getRules()) {
                Rule rule = (Rule) o;
                actions.add(rule.getAction());
            }
        }
        return actions.size();
    }

    public static long getNumberOfComplexVisibilityConditions(final IDecisionModel dm) {
        return countComplexVisibilityConditions(dm);
    }

    private static long countComplexVisibilityConditions(final IDecisionModel dm) {
        int count = 0;
        for (IDecision<?> decision : dm.getDecisions()) {
            if (DecisionModelUtils.isComplexVisibilityCondition(decision.getVisiblity())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds a decision to the given decision model.
     *
     * @param dm       the decision model to which the given decision should be added.
     * @param decision the decision to add.
     */
    public static void addDecision(final DecisionModel dm, final BooleanDecision decision) {
        Objects.requireNonNull(dm).add(Objects.requireNonNull(decision));
    }

    @SuppressWarnings("rawtypes")
    public static boolean allVisisbleDecisionsAreSelected(final Collection<IDecision<?>> decisions) {
        boolean result = true;
        for (IDecision decision : decisions) {
            if (decision.isVisible()) {
                result = result && decision.isSelected();
            }
        }
        return result;
    }

    public static boolean definesMandatoryConstraint(final IDecision<?> decision) {
        for (Rule rule : decision.getRules()) {
            if (rule.getCondition() instanceof Not && rule.getAction() instanceof SelectDecisionAction) {
                Not condition = (Not) rule.getCondition();
                if (condition.getOperand() instanceof IsSelectedFunction) {
                    IsSelectedFunction function = (IsSelectedFunction) condition.getOperand();
                    SelectDecisionAction action = (SelectDecisionAction) rule.getAction();
                    if (decision.equals(function.getParameters().get(0)) && decision.equals(action.getVariable())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isRequiresRule(final IDecision<?> decision, final Rule rule) {
        return rule.getCondition() instanceof IsSelectedFunction && rule.getAction() instanceof SelectDecisionAction &&
                decision.equals(((IsSelectedFunction) rule.getCondition()).getParameters().get(0));
    }

    public static boolean definesExcludesConstraint(final IDecision<?> decision) {
        for (Rule rule : decision.getRules()) {
            if (isExcludesRule(decision, rule)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExcludesRule(final IDecision<?> decision, final Rule rule) {
        return rule.getCondition() instanceof IsSelectedFunction &&
                rule.getAction() instanceof DeSelectDecisionAction &&
                decision.equals(((IsSelectedFunction) rule.getCondition()).getParameters().get(0));
    }
}
