package at.jku.cps.travart.dopler.decision.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.ICondition;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IEnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.AllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.And;
import at.jku.cps.travart.dopler.decision.model.impl.DisAllowAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumDecision;
import at.jku.cps.travart.dopler.decision.model.impl.IsSelectedFunction;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;

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

	// TODO: remove, should be done by the decision
	@Override
	public final void executeRules() throws ActionExecutionException {
		for (IDecision decision : getDecisions()) {
			if (decision.isSelected()) {
				decision.executeRules();
			}
		}
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
	public Set<EnumDecision> findWithRangeValue(final IDecision decision) {
		Set<EnumDecision> decisions = new HashSet<>();
		for (IDecision d : getDecisions()) {
			if (d.getType() == ADecision.DecisionType.ENUM) {
				EnumDecision enumDecision = (EnumDecision) d;
				ARangeValue value = enumDecision.getRangeValue(DecisionModelUtils.retriveFeatureName(decision, true));
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
			if (d.getVisiblity() instanceof IsSelectedFunction
					&& ((IsSelectedFunction) d.getVisiblity()).getParameters().get(0) == decision) {
				decisions.add(d);
			} else if (DecisionModelUtils.isMandatoryVisibilityCondition(d.getVisiblity())) {
				And and = (And) d.getVisiblity();
				IsSelectedFunction isTaken = (IsSelectedFunction) (and.getLeft() == ICondition.FALSE ? and.getRight()
						: and.getLeft());
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
			if (DecisionModelUtils.getSelectedDecisions(this).isEmpty()) {
				return false;
			}
			for (IDecision decision : getDecisions()) {
				if (decision.isSelected() && decision.getVisiblity() != ICondition.FALSE) {
					// if selected, the decision must be visible as well, as long the decision is no
					// mandatory feature or a decision based on a Enum value
					if (!decision.isVisible()
							&& !DecisionModelUtils.isMandatoryVisibilityCondition(decision.getVisiblity())
							|| DecisionModelUtils.isMandatoryVisibilityCondition(decision.getVisiblity())
									&& !DecisionModelUtils.retriveMandatoryVisibilityCondition(decision.getVisiblity())
											.isSelected()) {
						return false;
					}
					// enumeration decision always must have a value, if they are selected (if
					// optional they have the none
					// value); values set of enumerations isn't allowed to be empty.
					if (decision instanceof EnumDecision && ((EnumDecision) decision).getValues().isEmpty()) {
						return false;
					}
				}
				// if decision is a transformed constraint enumeration, it must be some sort of
				// selected if visible
				if (DecisionModelUtils.isEnumDecisionConstraint(decision) && decision.isVisible()) {
					if (!decision.isSelected()) {
						return false;
					}
					EnumDecision enumD = (EnumDecision) decision;
					if (enumD.getValues().isEmpty()) {
						return false;
					}
					for (ARangeValue<String> value : enumD.getValues()) {
						IDecision selected = get(value.getValue());
						if (!selected.isSelected()) {
							return false;
						}
					}
				}
				// if selected, all rules must be satisfied, if the rule condition holds
				for (Object o : decision.getRules()) {
					Rule rule = (Rule) o;
					if (rule.getCondition().evaluate()) {
						// test if the rule actions are satisfied
						// TODO: Review: A value may be allowed by setting another value, but it can be
						// disallowed by other conditions, so the only actions to be checked are all
						// others than AllowFunctions
						// TODO: Different interpretation of the AllowFunction and the DisAllowFunction.
						// As long the actual value is different the model is valid. Enabled flag for
						// configurators
						if (rule.getAction() instanceof AllowAction) {
							// nothing to do here
						} else if (rule.getAction() instanceof DisAllowAction) {
							DisAllowAction daf = (DisAllowAction) rule.getAction();
							if (daf.getVariable() instanceof IEnumerationDecision) {
								IEnumerationDecision variable = (IEnumerationDecision) daf.getVariable();
								if (variable.getValues().contains(daf.getValue())) {
									return false;
								}
							} else {
								IDecision variable = daf.getVariable();
								if (!variable.getValue().equals(daf.getValue())) {
									return false;
								}
							}
						} else if (!rule.getAction().isSatisfied()) {
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
		if (!name.equals(other.name)) {
			return false;
		}
		if (size() != other.size()) {
			return false;
		}
		if (!containsAll(other.getDecisions())) {
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
