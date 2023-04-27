/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.sampling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.cps.travart.core.common.IConfigurable;
import at.jku.cps.travart.core.common.ISampler;
import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import at.jku.cps.travart.dopler.common.DecisionModelUtils;
import at.jku.cps.travart.dopler.decision.IDecisionModel;
import at.jku.cps.travart.dopler.decision.exc.ActionExecutionException;
import at.jku.cps.travart.dopler.decision.exc.RangeValueException;
import at.jku.cps.travart.dopler.decision.exc.UnsatisfiedCardinalityException;
import at.jku.cps.travart.dopler.decision.model.ADecision;
import at.jku.cps.travart.dopler.decision.model.ADecision.DecisionType;
import at.jku.cps.travart.dopler.decision.model.ARangeValue;
import at.jku.cps.travart.dopler.decision.model.IDecision;
import at.jku.cps.travart.dopler.decision.model.IValue;
import at.jku.cps.travart.dopler.decision.model.impl.DeSelectDecisionAction;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;
import at.jku.cps.travart.dopler.decision.model.impl.NumberDecision;
import at.jku.cps.travart.dopler.decision.model.impl.Rule;
import at.jku.cps.travart.dopler.decision.model.impl.SelectDecisionAction;
import at.jku.cps.travart.dopler.transformation.DefaultDecisionModelTransformationProperties;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DecisionModelSampler implements ISampler<IDecisionModel> {

	private static final long DEFAULT_NUMBER_OF_VALID_CONFIGS = 1_000_000L;

	private Set<Map<IConfigurable, Boolean>> validConfigs;
	private Set<Map<IConfigurable, Boolean>> invalidConfigs;
	private IDecisionModel lastDm;
	private long configcounter;
	private long maxNumberOfValidConfigs;

	public DecisionModelSampler(final long maxNumberOfValidConfigs) {
		this.maxNumberOfValidConfigs = maxNumberOfValidConfigs;
	}

	public DecisionModelSampler() {
		this(DEFAULT_NUMBER_OF_VALID_CONFIGS);
	}

	public long getMaxNumberOfValidConfigs() {
		return maxNumberOfValidConfigs;
	}

	public void setMaxNumberOfValidConfigs(final long maxNumberOfValidConfigs) {
		this.maxNumberOfValidConfigs = maxNumberOfValidConfigs;
	}

	@Override
	public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(final IDecisionModel dm)
			throws NotSupportedVariabilityTypeException {
		if (lastDm == null || lastDm != dm) {
			sample(dm);
		}
		return validConfigs;
	}

	@Override
	public Set<Map<IConfigurable, Boolean>> sampleValidConfigurations(final IDecisionModel dm, final long maxNumber)
			throws NotSupportedVariabilityTypeException {
		if (lastDm == null || lastDm != dm) {
			sample(dm);
		}
		return validConfigs.size() > maxNumber ? validConfigs.stream().limit(maxNumber).collect(Collectors.toSet())
				: validConfigs;
	}

	@Override
	public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(final IDecisionModel dm)
			throws NotSupportedVariabilityTypeException {
		if (lastDm == null || lastDm != dm) {
			sample(dm);
		}
		return invalidConfigs;
	}

	@Override
	public Set<Map<IConfigurable, Boolean>> sampleInvalidConfigurations(final IDecisionModel dm, final long maxNumber)
			throws NotSupportedVariabilityTypeException {
		if (lastDm == null || lastDm != dm) {
			sample(dm);
		}
		return invalidConfigs.size() > maxNumber ? invalidConfigs.stream().limit(maxNumber).collect(Collectors.toSet())
				: invalidConfigs;
	}

	private void sample(final IDecisionModel dm) throws NotSupportedVariabilityTypeException {
		try {
			lastDm = dm;
			configcounter = 0;
			validConfigs = new HashSet<>();
//			invalidConfigs = new HashSet<>();
			createConfigurations(dm, dm.getDecisions()); // .getSelectableDecisions(dm));
//			createConfigurations(dm, DecisionModelUtils.getVisibleDecisions(dm));
			System.out.println("Configurations generated: " + configcounter);
		} catch (NotSupportedVariabilityTypeException | RangeValueException | ActionExecutionException
				| UnsatisfiedCardinalityException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	private void createConfigurations(final IDecisionModel dm, final Collection<IDecision<?>> decisions)
			throws NotSupportedVariabilityTypeException, RangeValueException, ActionExecutionException,
			UnsatisfiedCardinalityException {
		// cancel if max number of valid configurations is reached
		if (validConfigs.size() >= maxNumberOfValidConfigs) {
			return;
		}
		configcounter++;
		if (dm.isValid()) { // && DecisionModelUtils.allVisisbleDecisionsAreSelected(decisions)) {
			Map<IConfigurable, Boolean> validConfig = getCurrentConfigurationInclValues(dm);
			validConfigs.add(validConfig);
//			deriveInvalidConfigs(validConfig);
		}
		Set<IDecision<?>> nextDecisions = new LinkedHashSet<>(decisions);
		Iterator<IDecision<?>> decisionIterator = nextDecisions.iterator();
		while (decisionIterator.hasNext()) {
			IDecision decision = decisionIterator.next();
			decisionIterator.remove();
//			if (decision.getType() != ADecision.DecisionType.ENUM) {
			for (Object o : decision.getRange()) {
				ARangeValue rangeValue = (ARangeValue) o;
				IValue prevValue = decision.getValue();
				decision.setValue(rangeValue.getValue());
				if (!canBeValid(decision)) {
					decision.setValue(prevValue);
					return;
				}
//					if (decision.getType() == ADecision.DecisionType.BOOLEAN && decision.isSelected()
//							&& !decision.isVisible()) {
//						decision.setValue(BooleanValue.getFalse().getValue());
//					} else if (decision.getType() == ADecision.DecisionType.NUMBER && decision.isSelected()
//							&& !decision.isVisible()) {
//						decision.reset();
//					} else {
//						afterValueSelection(dm, decision);
//					}
//					decision.executeRules();
				createConfigurations(dm, nextDecisions);
			}
//			} else {
//				EnumDecision enumDecision = (EnumDecision) decision;
//				int minSelectedValues = enumDecision.getCardinality().getMin();
//				int maxSelectedValues = enumDecision.getCardinality().getMax();
//				Set<Set<ARangeValue<String>>> valueSets = DecisionModelUtils
//						.powerSetWithMinAndMax(enumDecision.getRange(), minSelectedValues, maxSelectedValues);
//				for (Set<ARangeValue<String>> values : valueSets) {
//					enumDecision.setValues(values);
//					if (decision.isSelected() && !decision.isVisible()) {
//						decision.reset();
//					}
////					else {
////						decision.executeRules();
////					}
//					createConfigurations(dm, nextDecisions);
//				}
//			}
		}
	}

	private boolean canBeValid(final IDecision decision) {
		for (Object o : decision.getRules()) {
			Rule rule = (Rule) o;
			if (rule.getCondition().evaluate()) {
				if (rule.getAction() instanceof SelectDecisionAction) {
					SelectDecisionAction action = (SelectDecisionAction) rule.getAction();
					if (!action.getVariable().isSelected()) {
						return false;
					}
				} else if (rule.getAction() instanceof DeSelectDecisionAction) {
					DeSelectDecisionAction action = (DeSelectDecisionAction) rule.getAction();
					if (action.getVariable().isSelected()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void deriveInvalidConfigs(final Map<IConfigurable, Boolean> config)
			throws NotSupportedVariabilityTypeException {
		Random rand = new Random();
		for (int count = 0; count < 10; count++) {
			int decisionSwitch = rand.nextInt(config.size());
			Map<IConfigurable, Boolean> invalid = new HashMap<>();
			int i = 0;
			for (Entry<IConfigurable, Boolean> entry : config.entrySet()) {
				IConfigurable key = entry.getKey();
				Boolean value = entry.getValue();
				if (i == decisionSwitch) {
					value = !value;
					key.setSelected(value);
				}
				invalid.put(key, value);
				i++;
			}
			// the randomly changed configuration my be valid so do not add it to the
			// invalid samples
			if (!verifySampleAs(lastDm, invalid)) {
				invalidConfigs.add(invalid);
			}
		}
	}

	private static Map<IConfigurable, Boolean> getCurrentConfigurationInclValues(final IDecisionModel dm) {
		Map<IConfigurable, Boolean> configuration = new HashMap<>();
		for (IDecision decision : dm.getDecisions()) {
			// configuration.put(decision, decision.isSelected());
			// decision configurable
			IConfigurable decisionConfigurable = new IConfigurable() {
				boolean selected;
				String name = decision.getId();

				@Override
				public void setSelected(final boolean selected) {
					this.selected = selected;
				}

				@Override
				public boolean isSelected() {
					return selected;
				}

				@Override
				public String getName() {
					return name;
				}
			};
			decisionConfigurable.setSelected(decision.isSelected());
			configuration.put(decisionConfigurable, decision.isSelected());
			if (decision.getType() == ADecision.DecisionType.ENUM) {
				EnumerationDecision enumDecision = (EnumerationDecision) decision;
				for (ARangeValue value : enumDecision.getValues()) {
					IConfigurable configurable = new IConfigurable() {
						boolean selected;
						String name = value.toString();

						@Override
						public void setSelected(final boolean selected) {
							this.selected = selected;
						}

						@Override
						public boolean isSelected() {
							return selected;
						}

						@Override
						public String getName() {
							return name;
						}
					};
					configurable.setSelected(enumDecision.isSelected());
					configuration.put(configurable, enumDecision.isSelected());
				}
			}
			if (decision.getType() == ADecision.DecisionType.NUMBER) {
				NumberDecision numberDecision = (NumberDecision) decision;
				IConfigurable configurable = new IConfigurable() {
					boolean selected;
					String name = numberDecision.getId()
							+ DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR
							+ numberDecision.getValue();

					@Override
					public void setSelected(final boolean selected) {
						this.selected = selected;
					}

					@Override
					public boolean isSelected() {
						return selected;
					}

					@Override
					public String getName() {
						return name;
					}
				};
				configurable.setSelected(numberDecision.isSelected());
				configuration.put(configurable, numberDecision.isSelected());
			}
		}
		return configuration;
	}

	@Override
	public boolean verifySampleAs(final IDecisionModel dm, final Map<IConfigurable, Boolean> sample)
			throws NotSupportedVariabilityTypeException {
		try {
			dm.reset();
			for (Entry<IConfigurable, Boolean> entry : sample.entrySet()) {
				IDecision decision = dm.get(entry.getKey().getName());
				if (decision == null && entry.getValue()) {
					String name = entry.getKey().getName();
					if (name.contains(DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)) {
						decision = dm.get(name.substring(0, name
								.indexOf(DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)));
						ARangeValue value = decision.getRangeValue(name.substring(1 + name
								.indexOf(DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)));
						if (value != null) {
							decision.setValue(value.getValue());
							if (decision.isSelected()) {
								afterValueSelection(dm, decision);
							}
						}
					} else {
						setEnumValue(dm, name);
//						if (enumD != null && enumD.isSelected()) {
//							afterValueSelection(dm, enumD);
//						}
					}
				} else if (decision != null && decision.getType() != DecisionType.ENUM
						&& decision.getType() != DecisionType.NUMBER) {
					decision.setValue(entry.getValue());
					if (decision.isSelected()) {
						afterValueSelection(dm, decision);
					}
				}
			}
			return dm.isValid();
		} catch (RangeValueException | UnsatisfiedCardinalityException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	public static void configureDmWithSamples(final IDecisionModel dm, final Map<IConfigurable, Boolean> sample)
			throws at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException {
		try {
			dm.reset();
			for (Entry<IConfigurable, Boolean> entry : sample.entrySet()) {
				if (entry.getValue()) {
					IDecision decision = dm.get(entry.getKey().getName());
					if (decision == null) {
						String name = entry.getKey().getName();
						if (name.contains(DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)) {
							decision = dm.get(name.substring(0,
									name.indexOf(
											DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)
											- 1));
							ARangeValue value = decision.getRangeValue(name.substring(name.indexOf(
									DefaultDecisionModelTransformationProperties.CONFIGURATION_VALUE_SEPERATOR)));
							decision.setValue(value);
							afterValueSelection(dm, decision);
						}
					} else {
						decision.setValue(true);
						afterValueSelection(dm, decision);
					}
				}
			}
//			dm.executeRules();
		} catch (RangeValueException | UnsatisfiedCardinalityException e) {
			throw new NotSupportedVariabilityTypeException(e);
		}
	}

	private static void afterValueSelection(final IDecisionModel dm, final IDecision decision)
			throws RangeValueException, UnsatisfiedCardinalityException {
		for (EnumerationDecision enumDecision : DecisionModelUtils.getEnumDecisions(dm)) {
			// also select the enumeration decision if a boolean decision of the same name
			// is set
			if (DecisionModelUtils.isTransformedEnumDecision(enumDecision, decision.getId())) {
				enumDecision.setSelected(decision.isSelected());
			}
			// add the value for a enum decision
			for (ARangeValue<String> rangeValue : enumDecision.getRange()) {
				IDecision rangeDecision = dm.get(rangeValue.getValue());
				if (rangeDecision == decision) {
					if (enumDecision.getCardinality().isAlternative()) {
						enumDecision.setValue(rangeValue.getValue());
					} else if (enumDecision.getCardinality().isOr()) {
						Set<ARangeValue<String>> values = new HashSet<>(enumDecision.getValues());
						values.add(rangeValue);
						enumDecision.setValues(values);
					}
				}
			}
		}
	}

	private void setEnumValue(final IDecisionModel dm, final String name)
			throws RangeValueException, UnsatisfiedCardinalityException {
		for (EnumerationDecision enumDecision : DecisionModelUtils.getEnumDecisions(dm)) {
			// add the value for a enum decision
			for (ARangeValue<String> rangeValue : enumDecision.getRange()) {
				if (rangeValue.getValue().equals(name)) {
					Set<ARangeValue<String>> values = new HashSet<>(enumDecision.getValues());
					values.add(rangeValue);
					enumDecision.setValues(values);
				}
			}
		}
	}
}
