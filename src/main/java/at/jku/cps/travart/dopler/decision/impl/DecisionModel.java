/*******************************************************************************
 * TODO: explanation what the class does
 *
 *  @author Kevin Feichtinger
 *
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.impl;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.AbstractDecision;
import at.jku.cps.travart.dopler.decision.model.AbstractRangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;

import java.util.*;

@SuppressWarnings("rawtypes")
public final class DecisionModel implements IDecisionModel {

    public static final String DEFAULT_NAME = "DecisionModel";
    private final String factoryId;
    private String sourceFile;
    private String name;
    private final HashMap<String, IDecision<?>> decisions;

    public DecisionModel(final String factoryId) {
        this(factoryId, DEFAULT_NAME);
    }

    public DecisionModel(final String factoryId, final String name) {
        this.factoryId = Objects.requireNonNull(factoryId);
        this.name = Objects.requireNonNull(name);
        decisions = new HashMap<>();
    }

    @Override
    public IDecision get(final String id) {
        for (IDecision d : getDecisions()) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public void add(final IDecision decision) {
        decisions.put(decision.getId(), decision);
    }

    @Override
    public void addAll(final Collection<IDecision<?>> decisions) {
        for (IDecision decision : decisions) {
            this.decisions.put(decision.getId(), decision);
        }
    }

    @Override
    public boolean remove(final IDecision decision) {
        IDecision prev = decisions.remove(decision.getId());
        return prev.equals(decision);
    }

    @Override
    public void clear() {
        decisions.clear();
    }

    @Override
    public int size() {
        return decisions.size();
    }

    @Override
    public Collection<String> getDecisionNames() {
        return decisions.keySet();
    }

    @Override
    public Collection<IDecision<?>> getDecisions() {
        return decisions.values();
    }

    @Override
    public boolean contains(final IDecision decision) {
        return decisions.containsKey(decision.getId());
    }

    @Override
    public boolean containsAll(final Collection<IDecision<?>> decisions) {
        for (IDecision decision : decisions) {
            if (!this.decisions.containsKey(decision.getId())) {
                return false;
            }
        }
        return true;
    }

    // TODO: remove from here - nothing to do with decision model per see
    public Set<EnumerationDecision> findWithRangeValue(final IDecision decision) {
        Set<EnumerationDecision> decisions = new HashSet<>();
        for (IDecision d : getDecisions()) {
            if (d.getType() == AbstractDecision.DecisionType.ENUM) {
                EnumerationDecision enumDecision = (EnumerationDecision) d;
                AbstractRangeValue value =
                        enumDecision.getRangeValue(DecisionModelUtils.retriveFeatureName(decision, true));
                if (value != null) {
                    decisions.add(enumDecision);
                }
            }
        }
        return decisions;
    }

    // TODO: remove from here - nothing to do with decision model per see
    public Set<IDecision> findWithVisibility(final IDecision decision) {
        Set<IDecision> decisions = new HashSet<>();
        for (IDecision d : getDecisions()) {
            if (d.getVisiblity() instanceof IsSelectedFunction &&
                    ((IsSelectedFunction) d.getVisiblity()).getParameters().get(0) == decision) {
                decisions.add(d);
            } else if (DecisionModelUtils.isMandatoryVisibilityCondition(d.getVisiblity())) {
                And and = (And) d.getVisiblity();
                IsSelectedFunction isTaken =
                        (IsSelectedFunction) (and.getLeft() == ICondition.FALSE ? and.getRight() : and.getLeft());
                IDecision mandatory = isTaken.getParameters().get(0);
                if (mandatory == decision) {
                    decisions.add(d);
                }
            }
        }
        return decisions;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSourceFile() {
        return sourceFile;
    }

    @Override
    public boolean isValid() {
        try {
            for (IDecision decision : getDecisions()) {
                // when a decision is visible it must be taken too
                if (decision.isVisible() && !decision.isTaken()) {
                    return false;
                }
                // if selected, all rules must be satisfied, if the rule condition holds
                if (decision.isTaken()) {
                    for (Object o : decision.getRules()) {
                        Rule rule = (Rule) o;
                        if (rule.getCondition().evaluate() && !rule.getAction().isSatisfied()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (ActionExecutionException e) {
            return false;
        }
    }

    @Override
    public void reset() throws RangeValueException {
        for (IDecision decision : getDecisions()) {
            decision.reset();
        }
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void setSourceFile(final String sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DecisionModel");
        builder.append(name);
        builder.append("[");
        for (IDecision decision : getDecisions()) {
            builder.append(decision);
            builder.append("[selected=");
            builder.append(decision.isSelected());
            builder.append("; value=");
            builder.append(decision.getValue());
            builder.append("]");
            builder.append("; ");
        }
        if (builder.lastIndexOf(";") >= 0) {
            builder.deleteCharAt(builder.lastIndexOf(";"));
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Map<IConfigurable, Boolean> getCurrentConfiguration() {
        Map<IConfigurable, Boolean> configuration = new HashMap<>();
        for (IDecision decision : getDecisions()) {
            configuration.put(decision, decision.isSelected());
        }
        return configuration;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DecisionModel)) {
            return false;
        }
        DecisionModel other = (DecisionModel) o;
        if (!name.equals(other.name) || size() != other.size() || !containsAll(other.getDecisions())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (IDecision d : getDecisions()) {
            hash += d.hashCode();
        }
        return hash * 14851;
    }
}
