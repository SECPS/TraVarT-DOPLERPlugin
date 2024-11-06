/*******************************************************************************
 * An abstract implementation of a decision for a decision model.
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model;

import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import com.google.common.annotations.VisibleForTesting;

import java.util.*;

@SuppressWarnings("rawtypes")
public abstract class AbstractDecision<T> implements IDecision<T> {

    public enum DecisionType {
        BOOLEAN("Boolean"), NUMBER("Double"), STRING("String"), ENUM("Enumeration");

        private String type;

        DecisionType(final String type) {
            this.type = type;
        }

        public boolean equalString(final String type) {
            return this.type.equals(type);
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private boolean select;
    private boolean taken;
    private String id;
    private String question;
    private String description;
    private final DecisionType type;
    private Set<Rule> rules;
    private ICondition visibility;

    protected AbstractDecision(final String id, final DecisionType type) {
        this.id = Objects.requireNonNull(id);
        this.description = "";
        this.question = "";
        this.type = Objects.requireNonNull(type);
        rules = new LinkedHashSet<>(); //Keeps order
        select = false;
        visibility = ICondition.TRUE;
    }

    @Override
    public final void setSelected(final boolean select) {
        this.select = select;
        setIsTaken(true);
    }

    @Override
    public final void executeRules() {
        try {
            for (Rule rule : getRules()) {
                if (rule.getCondition().evaluate() && !rule.getAction().isSatisfied()) {
                    rule.getAction().execute();
                }
            }
        } catch (ActionExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final boolean isSelected() {
        return select;
    }

    @Override
    public boolean isTaken() {
        return taken;
    }

    @Override
    public void setIsTaken(final boolean isTaken) {
        taken = isTaken;
    }

    @Override
    public Set<Rule> getRules() {
        return rules;
    }

    @Override
    public void addRule(final Rule rule) {
        rules.add(rule);
    }

    @Override
    @VisibleForTesting
    public void setRules(final Set<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean removeRule(final Rule rule) {
        return rules.remove(rule);
    }

    @Override
    public void addRules(final Collection<Rule> rules) {
        this.rules.addAll(rules);
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public void setQuestion(final String question) {
        this.question = question;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public DecisionType getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public ICondition getVisiblity() {
        return visibility;
    }

    @Override
    public void setVisibility(final ICondition visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public final boolean isVisible() {
        return visibility.evaluate();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, this.getClass());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !this.getClass().equals(obj.getClass())) {
            return false;
        }
        AbstractDecision other = (AbstractDecision) obj;
        return Objects.equals(id, other.id) && Objects.equals(type, other.type) && Objects.equals(rules, other.rules) &&
                Objects.equals(visibility, other.visibility);
    }
}
