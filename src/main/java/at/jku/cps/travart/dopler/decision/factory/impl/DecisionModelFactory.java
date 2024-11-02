/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.factory.impl;

import at.jku.cps.travart.dopler.decision.factory.IDecisionModelFactory;
import at.jku.cps.travart.dopler.decision.impl.DecisionModel;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.IAction;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.*;

import java.util.Objects;

public class DecisionModelFactory implements IDecisionModelFactory {

    public static final String ID = "at.jku.cps.travart.dopler.decision.factory.DecisionModelFactory";

    private DecisionModelFactory() {

    }

    private static DecisionModelFactory factory;

    public static DecisionModelFactory getInstance() {
        if (factory == null) {
            factory = new DecisionModelFactory();
        }
        return factory;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public DecisionModel create() {
        return new DecisionModel(ID);
    }

    @Override
    public BooleanDecision createBooleanDecision(final String id) {
        BooleanDecision decision = new BooleanDecision(id);
        decision.setQuestion(id.concat("?"));
        decision.setDescription(id.concat(" description"));
        return decision;
    }

    @Override
    public Cardinality createCardinality(final int min, final int max) {
        return new Cardinality(min, max);
    }

    @Override
    public EnumerationDecision createEnumDecision(final String id) {
        EnumerationDecision decision = new EnumerationDecision(id);
        decision.setQuestion(id.concat("?"));
        decision.setDescription(id.concat(" description"));
        return decision;
    }

    @Override
    public Range<String> createEnumValueOptions(final String[] options) {
        Range<String> range = new Range<>();
        for (String o : options) {
            range.add(new StringValue(o));
        }
        return range;
    }

    @Override
    public NumberDecision createNumberDecision(final String id) {
        NumberDecision decision = new NumberDecision(id);
        decision.setQuestion(id.concat("?"));
        decision.setDescription(id.concat(" description"));
        return decision;
    }

    @Override
    public Range<Double> createNumberValueRange(final String[] ranges) {
        if (ranges.length % 2 != 0) {
            throw new IllegalArgumentException("Range does not have even number of range values");
        }
        Range<Double> range = new Range<>();
        for (int i = 0; i < ranges.length; i += 2) {
            double min = parseDouble(ranges[i]);
            double max = parseDouble(ranges[i + 1]);
            for (double value = min; Math.round(value) <= Math.round(max); value++) {
                DoubleValue v = new DoubleValue(value);
                range.add(v);
            }
        }
        return range;
    }

    private double parseDouble(final String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Given String " + str + " is not a number.");
        }
    }

    @Override
    public StringDecision createStringDecision(final String id) {
        StringDecision decision = new StringDecision(id);
        decision.setQuestion(id.concat("?"));
        decision.setDescription(id.concat(" description"));
        return decision;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IsTakenFunction createIsTakenFunction(final IDecision decision) {
        return new IsTakenFunction(Objects.requireNonNull(decision));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IsSelectedFunction createIsSelectedFunction(final IDecision decision) {
        return new IsSelectedFunction(Objects.requireNonNull(decision));
    }

    @Override
    public And createAndCondition(final ICondition left, final ICondition right) {
        return new And(left, right);
    }

    @Override
    public Not createNotCondition(final ICondition value) {
        return new Not(value);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public GetValueFunction createGetValueFunction(final IDecision decision) {
        return new GetValueFunction(decision);
    }

    @Override
    public ICondition createEquals(final ICondition getValue, final AbstractRangeValue<?> value) {
        return new Equals(getValue, value);
    }

    @Override
    public IAction createSelectDecisionAction(final BooleanDecision decision) {
        return new SelectDecisionAction(decision);
    }

    @Override
    public IAction createDeSelectDecisionAction(final BooleanDecision decision) {
        return new DeSelectDecisionAction(decision);
    }

    @Override
    public Rule createRule(final ICondition condition, final IAction action) {
        return new Rule(condition, action);
    }

    @Override
    public DisAllowAction createDisAllowAction(final EnumerationDecision userManagement, final String value) {
        return new DisAllowAction(userManagement, userManagement.getRangeValue(value));
    }

    @Override
    public AllowAction createAllowAction(final EnumerationDecision userManagement, final String value) {
        return new AllowAction(userManagement, userManagement.getRangeValue(value));
    }

    @Override
    public SetValueAction setValueAction(final EnumerationDecision userManagement, final String value) {
        return new SetValueAction(userManagement, userManagement.getRangeValue(value));
    }
}
