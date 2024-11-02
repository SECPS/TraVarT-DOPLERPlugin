/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.impl.Range;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;

import java.util.Collection;
import java.util.Set;

public interface IDecision<T> extends IConfigurable {

    String getId();

    void setId(String id);

    String getQuestion();

    void setQuestion(String question);

    String getDescription();

    void setDescription(String description);

    DecisionType getType();

    Range<T> getRange();

    AbstractRangeValue<T> getRangeValue(T value);

    AbstractRangeValue<T> getRangeValue(String value);

    void setRange(Range<T> range);

    IValue<T> getValue();

    void setValue(T value) throws RangeValueException;

    void executeRules();

    Set<Rule> getRules();

    void addRule(Rule rule);

    void addRules(Collection<Rule> rules);

    boolean removeRule(Rule rule);

    void setRules(Set<Rule> rules);

    ICondition getVisiblity();

    void setVisibility(ICondition visiblity);

    boolean isVisible();

    boolean isTaken();

    void setIsTaken(boolean isTaken);

    // is only supposed to reset Values of decisions, not removing rules from a
    // decision
    void reset() throws RangeValueException;
}
